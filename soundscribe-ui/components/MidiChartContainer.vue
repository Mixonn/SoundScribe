<template>
  <div>
    <div ref="containerId" class="chartContainer">
      <midi-chart
        v-if="loaded"
        :chart-data="chartData"
        :options="options"
        :style="chartStyles"
      />
      <div id="zoomview-container" />
    </div>
    <button class="btn" @click="zoomIn">
      +
    </button>
    <button @click="zoomOut">
      -
    </button>
    <div id="overview-container" />
    <audio controls="controls">
      <source src="https://raw.githubusercontent.com/Fehu4/Kik/master/TP0052B_01.mp3" type="audio/mpeg">
    </audio>
  </div>
</template>

<script>
import WaveformData from 'waveform-data'
import Peaks from 'peaks.js'
import MidiChart from '~/components/midiChart.vue'

export default {
  name: 'MidiChartContainer',
  components: {
    MidiChart
  },
  data () {
    return {
      loaded: false,
      chartData: {
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
        endTime: 10
      },
      fileNameFormatted: '',
      midiDataFormatted: [],
      maxTime: 0,
      secondsOnChart: 10,
      peaksInstance: {},
      dataLoading: false
    }
  },
  computed: {
    chartStyles () {
      return {
        height: '10%',
        width: '100%',
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
      this.loadFiles()
      this.buildWaveform()
    },
    loadFiles () {
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
      if (!this.dataLoading) {
        this.dataLoading = true
        const midiChartData = this.prepareMidiChartData()
        const baseFrequencyChartData = this.prepareBaseFrequencyChartData()
        const chartData = this.prepareCombinedChartData(midiChartData, baseFrequencyChartData)
        this.chartData = {
          datasets: chartData
        }
        this.loaded = true
        this.dataLoading = false
      }
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
      if (chartData.length > 0) {
        const maxMidiTime = parseInt(chartData[chartData.length - 1][1].x)
        this.maxTime = maxMidiTime > this.maxTime ? maxMidiTime : this.maxTime
      }
      return chartData
    },
    prepareBaseFrequencyChartData () {
      const chartData = []
      let tempData = []
      const data = this.baseFrequencyFileContent.split('\n')
      for (let i = 0; i < data.length; i++) {
        const values = data[i].trim().split(/[ ,]+/)
        const timeValue = parseFloat(values[0])
        const frequencyValue = parseFloat(values[1])
        if (!isNaN(timeValue) && !isNaN(frequencyValue) && this.chartPosition.startTime < timeValue && this.chartPosition.endTime > timeValue) {
          if (tempData.length > 0 && timeValue - tempData[tempData.length - 1].x > 0.1) {
            chartData.push(tempData)
            tempData = []
          }
          tempData.push({
            x: timeValue,
            y: frequencyValue
          })
        }
      }
      if (tempData.length > 0) {
        chartData.push(tempData)
      }
      if (chartData.length > 0) {
        const maxFrequencyTime = parseInt(chartData[chartData.length - 1].x)
        this.maxTime = maxFrequencyTime > this.maxTime ? maxFrequencyTime : this.maxTime
      }
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
      for (let i = 0; i < baseFrequencyData.length; i++) {
        chartData.push({
          data: baseFrequencyData[i],
          radius: 1,
          borderColor: '#3e95cd',
          fill: false,
          showLine: true
        })
      }
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
    buildWaveform () {
      const vm = this
      const audioContext = new AudioContext()
      fetch('https://raw.githubusercontent.com/Fehu4/Kik/master/TP0052B_01.mp3')
        .then(response => response.arrayBuffer())
        .then((buffer) => {
          const options = {
            audio_context: audioContext,
            array_buffer: buffer,
            scale: 128
          }

          return new Promise((resolve, reject) => {
            WaveformData.createFromAudio(options, (err, waveform) => {
              if (err) {
                reject(err)
              } else {
                resolve(waveform)
              }
            })
          })
        })
        .then((waveform) => {
          const options = {
            containers: {
              overview: document.getElementById('overview-container'),
              zoomview: document.getElementById('zoomview-container')
            },
            mediaElement: document.querySelector('audio'),
            webAudio: {
              audioContext
            },
            height: 100,
            showPlayheadTime: true,
            zoomLevels: [512, 1024, 2048, 4096],
            emitCueEvents: true
          }
          // eslint-disable-next-line handle-callback-err
          vm.peaksInstance = Peaks.init(options, function (err, peaks) {
          })
          vm.peaksInstance.on('zoomview.displaying', function (startTime, endTime) {
            if (vm.chartPosition.startTime !== startTime && vm.chartPosition.endTime !== endTime) {
              vm.chartPosition.startTime = startTime
              vm.chartPosition.endTime = endTime
              vm.generateChartDataSet()
            }
          })
        })
    }
  }
}
</script>

<style>
  .chartContainer {
    width: 100%;
    overflow-x: hidden;
  }
</style>
