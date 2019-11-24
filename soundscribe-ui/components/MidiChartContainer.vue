<template>
  <div>
    <div ref="containerId" class="chartContainer">
      <midi-chart
        v-if="loaded"
        :chartdata="chartdata"
        :options="options"
        :style="chartStyles"
      />
    </div>
    <button class="btn" @click="zoomIn">
      +
    </button>
    <button @click="zoomOut">
      -
    </button>
    <av-waveform
      audio-src='https://raw.githubusercontent.com/Fehu4/Kik/master/TP0052B_01.mp3'
    />
  </div>
</template>

<script>
import MidiChart from '~/components/midiChart.vue'

export default {
  name: 'MidiChartContainer',
  components: {
    MidiChart
  },
  data () {
    return {
      loaded: false,
      chartdata: {
      },
      options: {
        maintainAspectRatio: false,
        showLines: true,
        responsive: true,
        legend: {
          display: false
        },
        scales: {
          yAxes: [{
            ticks: {
              max: 600,
              min: 200,
              stepSize: 20
            }
          }]
        }
      },
      midiFileContent: '',
      baseFrequencyFileContent: '',
      chartPosition: {
        startTime: 0,
        endTime: 200
      },
      fileNameFormatted: '',
      midiDataFormatted: [],
      maxTime: 0,
      secondsOnChart: 10
    }
  },
  computed: {
    chartStyles () {
      return {
        height: '10%',
        width: ((this.maxTime / this.secondsOnChart) > 1 ? (this.maxTime / this.secondsOnChart) * 100 : 100) + '%',
        position: 'relative'
      }
    }
  },
  mounted () {
    this.loaded = false
    this.init()
  },
  methods: {
    init () {
      this.loadBaseFrequencyFile()
    },
    loadBaseFrequencyFile () {
      const contentUrl = 'https://raw.githubusercontent.com/Fehu4/Kik/master/TP0052B_01.mp3'
      const root = contentUrl.split('/')
      const fileName = root[root.length - 1]
      this.fileNameFormatted = 'Song_' + fileName.replace('.mp3', '')
      const fileTXTAddr = contentUrl.replace('.mp3', '.txt')
      const fileXMLAddr = contentUrl.replace('.mp3', '.xml')
      const baseFrequencyFilePromise = fetch(fileTXTAddr).then((element) => {
        return element.text()
      })
      const parser = new DOMParser()
      const midiFilePromise = fetch(fileXMLAddr).then((element) => {
        return element.text().then((text) => {
          return parser.parseFromString(text, 'text/xml')
        })
      })
      Promise.all([baseFrequencyFilePromise, midiFilePromise]).then((files) => {
        this.baseFrequencyFileContent = files[0]
        this.midiFileContent = files[1]
        this.generateChartDataSet()
      })
    },
    generateChartDataSet () {
      const midiChartData = this.prepareMidiChartData()
      const baseFrequencyChartData = this.prepareBaseFrequencyChartData()
      const chartData = this.prepareCombinedChartData(midiChartData, baseFrequencyChartData)
      this.chartdata = {
        datasets: chartData
      }
      this.loaded = true
    },
    prepareMidiChartData () {
      const notes = this.midiFileContent.getElementsByTagName(this.fileNameFormatted)[0].childNodes
      const chartData = []
      for (let i = 0; i < notes.length; i++) {
        const midiWrapper = {
          startTime: parseFloat(notes[i].children[0].textContent),
          endTime: parseFloat(notes[i].children[0].textContent) + parseFloat(notes[i].children[1].textContent),
          duration: parseFloat(notes[i].children[1].textContent),
          value: parseFloat(notes[i].children[2].textContent),
          midiValue: parseInt(notes[i].children[3].textContent),
          letterNote: notes[i].children[4].textContent
        }
        if (midiWrapper.startTime > this.chartPosition.endTime || midiWrapper.endTime < this.chartPosition.startTime) {
        } else {
          this.midiDataFormatted.push(midiWrapper)
          const midiEvent = []
          midiEvent.push({
            x: midiWrapper.startTime,
            y: midiWrapper.value
          })
          midiEvent.push({
            x: midiWrapper.endTime,
            y: midiWrapper.value
          })
          chartData.push(midiEvent)
        }
      }
      const maxMidiTime = parseInt(chartData[chartData.length - 1][1].x)
      this.maxTime = maxMidiTime > this.maxTime ? maxMidiTime : this.maxTime
      return chartData
    },
    prepareBaseFrequencyChartData () {
      const chartData = []
      const data = this.baseFrequencyFileContent.split('\n')
      for (let i = 0; i < data.length; i++) {
        const values = data[i].trim().split(/[ ,]+/)
        const timeValue = parseFloat(values[0])
        const frequencyValue = parseFloat(values[1])
        if (!isNaN(timeValue) && !isNaN(frequencyValue) && this.chartPosition.startTime < timeValue && this.chartPosition.endTime > timeValue) {
          chartData.push({
            x: timeValue,
            y: frequencyValue
          })
        }
      }
      const maxFrequencyTime = parseInt(chartData[chartData.length - 1].x)
      this.maxTime = maxFrequencyTime > this.maxTime ? maxFrequencyTime : this.maxTime
      return chartData
    },
    prepareCombinedChartData (midiData, baseFrequencyData) {
      const chartData = []
      for (let i = 0; i < midiData.length; i++) {
        chartData.push({
          data: midiData[i],
          borderColor: '#ac2b2b',
          fill: false,
          showLine: true })
      }
      chartData.push({
        data: baseFrequencyData,
        radius: 1,
        borderColor: '#3e95cd',
        fill: false,
        showLine: true
      })
      return chartData
    },
    zoomIn () {
      if (this.secondsOnChart > 2) {
        this.secondsOnChart -= 1
      }
    },
    zoomOut () {
      if (this.secondsOnChart < this.maxTime) {
        this.secondsOnChart += 1
      }
    },
    async scrollRight () {
      const scroll = this.$refs.containerId
      const pixelsPerSecond = scroll.scrollWidth / this.maxTime
      for (let i = 0; i < this.maxTime - this.secondsOnChart; i++) {
        scroll.scrollLeft = i * pixelsPerSecond
        await this.sleep(1000)
      }
    },
    sleep (ms) {
      return new Promise(resolve => setTimeout(resolve, ms))
    }
  }
}
</script>

<style>
  .chartContainer {
    width: 100%;
    overflow-x: auto;
  }
</style>
