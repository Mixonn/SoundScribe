<template>
  <div class="container">
    {{ text }}
    <midi-chart
      v-if="loaded"
      :chartdata="chartdata"
      :options="options"
      :styles="chartStyles"
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
      text: 'xDDDDD',
      loaded: false,
      chartdata: null,
      options: {
        maintainAspectRatio: false
      },
      midiFileContent: '',
      baseFrequencyFileContent: '',
      chartPosition: {
        startTime: 0,
        endTime: 10
      },
      fileNameFormatted: '',
      midiDataFormatted: []
    }
  },
  computed: {
    chartStyles () {
      return {
        height: '10%',
        width: '100%'
      }
    }
  },
  mounted () {
    this.loaded = true
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
      //  const midiChartData = this.prepareMidiChartData()
      const baseFrequencyChartData = this.prepareBaseFrequencyChartData()
      console.log(baseFrequencyChartData)
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
          continue
        } else {
          this.midiDataFormatted.push(midiWrapper)
          chartData.push({
            x: midiWrapper.startTime,
            y: midiWrapper.midiValue
          })
          chartData.push({
            x: midiWrapper.endTime,
            y: midiWrapper.midiValue
          })
        }
      }
      return chartData
    },
    prepareBaseFrequencyChartData () {
      const chartData = []
      const data = this.baseFrequencyFileContent.split('\n')
      for (let i = 0; i < data.length; i++) {
        const values = data[i].trim().split(/[ ,]+/)
        if (this.chartPosition.startTime > parseFloat(values[0]) || this.chartPosition.endTime < parseFloat(values[1])) {
          chartData.push({
            x: parseFloat(values[0]),
            y: parseFloat(values[1])
          })
        }
      }
      return chartData
    }
  }
}
</script>

<style>
</style>
