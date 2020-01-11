<template>
  <div class="app">
    <div id="buttons-container">
      <img alt="Select" class="controlButtons" src="/buttons/select.png">
      <img alt="Undo" class="controlButtons" src="/buttons/undo.png">
      <img alt="Redo" class="controlButtons" src="/buttons/redo.png">
      <img alt="Elevate" class="controlButtons" src="/buttons/elevate.png">
      <img alt="Lower" class="controlButtons" src="/buttons/lower.png">
      <img alt="Lengthen" class="controlButtons" src="/buttons/lengthen.png">
      <img alt="Shorten" class="controlButtons" src="/buttons/shorten.png">
      <img alt="Add" class="controlButtons" src="/buttons/plus.png">
      <img alt="Remove" class="controlButtons" src="/buttons/minus.png">
      <img alt="Update" class="controlButtons" src="/buttons/update.png">
    </div>
    <div ref="containerId" class="chartContainer">
      <midi-chart
        v-if="loaded"
        ref="chart"
        :chart-data="chartData"
        :options="chartOptions"
        :style="chartStyles"
      />
      <div id="zoomview-container" class="zoomviewContainer" />
    </div>
    <div id="overview-container" />
    <button class="btn" @click="zoomIn">
      Zoom in
    </button>
    <button @click="zoomOut">
      Zoom out
    </button>
    <audio controls="controls">
      <source :src="mp3Url" type="audio/mpeg">
    </audio>
    <div id="audio-controls">
      <input id="original-audio" v-model="soundOptionsSelected" type="checkbox" value="original">
      <label for="original-audio">Original</label>
      <input id="midi-audio" v-model="soundOptionsSelected" type="checkbox" value="midi">
      <label for="midi-audio">Midi</label>
      <input id="f0-audio" v-model="soundOptionsSelected" type="checkbox" value="f0">
      <label for="f0-audio">F0</label>
    </div>
  </div>
</template>

<script>
import WaveformData from 'waveform-data'
import Peaks from 'peaks.js'
import MidiChart from './midiChart'
import 'chartjs-plugin-dragdata'

export default {
  name: 'MidiChartContainer',
  components: {
    MidiChart
  },
  data () {
    return {
      loaded: false,
      chartData: {},
      midiFileContent: '',
      baseFrequencyFileContent: '',
      midiChartData: '',
      baseFrequencyChartData: '',
      chartPosition: {
        startTime: 0,
        endTime: 10
      },
      fileNameFormatted: '',
      peaksInstance: {},
      dataLoading: false,
      playerTime: 0,
      contextImageData: {},
      drawVerticalLineId: null,
      chartRendered: false,
      soundOptionsSelected: ['original'],
      midiAudioContext: null,
      midiPlaying: false,
      mp3Url: '',
      midiUrl: '',
      f0Url: '',
      currentlySelectedData: {
        index: -1,
        startTime: -1,
        endTime: -1,
        value: -1
      },
      chartOptions: {
        maintainAspectRatio: false,
        showLines: true,
        responsive: true,
        legend: {
          display: false
        },
        scales: {
          yAxes: [{
            ticks: {
              stepSize: 1
            }
          }],
          xAxes: [{
            ticks: {
              stepSize: 1
            }
          }]
        },
        animation: {
          onComplete: () => {
            this.saveChartContext()
          }
        },
        dragData: true,
        dragX: true,
        onClick: (e, element) => {
          this.setCurrentSelection(element)
        },
        onDragStart: (e, element) => {
          this.setCurrentSelection(element)
        },
        onDrag: (e, datasetIndex, index, value) => {
          value.x = value.x.toFixed(2);
          value.y = Math.round(value.y)
          this._data.chartData.datasets[datasetIndex].data[1 - index].y = value.y;
        },
        onDragEnd: (e, datasetIndex, index, value) => {
          this.updateCurrentMidiChartData()
        }
      }
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
    // prepare files urls
    const baseUrl = this.$axios.defaults.baseURL + '/download/' + this.$route.params.song.substr(0, this.$route.params.song.lastIndexOf('.abc'));
    this.abcUrl = this.$axios.defaults.baseURL + '/download/' + this.$route.params.song;
    this.mp3Url = baseUrl + '.mp3';
    this.midiUrl = baseUrl + '.xml';
    this.f0Url = baseUrl + '.txt';
    this.fileNameFormatted = this.$route.params.song.substr(0, this.$route.params.song.lastIndexOf('.abc'));
    this.init()
  },
  methods: {
    init () {
      // build waveform view + logic
      this.buildWaveform();
      // load midi + f0 files and load into chart
      this.loadFiles();
    },
    loadFiles () {
      const baseFrequencyFilePromise = fetch(this.f0Url).then((element) => {
        return element.text()
      });
      const parser = new DOMParser();
      const midiFilePromise = fetch(this.midiUrl).then((element) => {
        return element.text().then((text) => {
          return parser.parseFromString(text, 'text/xml')
        })
      });
      Promise.all([baseFrequencyFilePromise, midiFilePromise]).then((files) => {
        this.baseFrequencyFileContent = files[0];
        this.midiFileContent = files[1];
        this.parseFilesData()
      })
    },
    parseFilesData () {
      this.midiChartData = this.prepareMidiChartData();
      this.baseFrequencyChartData = this.prepareBaseFrequencyChartData();
      this.prepareCombinedChartData();
    },
    prepareMidiChartData () {
      const notes = this.midiFileContent.getElementsByTagName(this.fileNameFormatted)[0].getElementsByTagName('note');
      const chartData = [];
      for (let i = 0; i < notes.length; i++) {
        const midiWrapper = {
          startTime: parseFloat(notes[i].children[0].textContent),
          endTime: parseFloat(notes[i].children[0].textContent) + parseFloat(notes[i].children[1].textContent),
          duration: parseFloat(notes[i].children[1].textContent),
          value: parseFloat(notes[i].children[2].textContent),
          midiValue: parseInt(notes[i].children[3].textContent),
          letterNote: notes[i].children[4].textContent
        };
        const midiEvent = [];
        midiEvent.push({
          x: midiWrapper.startTime,
          y: midiWrapper.midiValue
        });
        midiEvent.push({
          x: midiWrapper.endTime,
          y: midiWrapper.midiValue
        });
        chartData.push(midiEvent)
      }
      return chartData
    },
    prepareBaseFrequencyChartData () {
      const chartData = [];
      let tempData = [];
      const data = this.baseFrequencyFileContent.split('\n');
      for (let i = 0; i < data.length; i++) {
        const values = data[i].trim().split(/[ ,]+/);
        const timeValue = parseFloat(values[0]);
        const frequencyValue = parseFloat(values[1]);
        if (!isNaN(timeValue) && !isNaN(frequencyValue)) {
          if (tempData.length > 0 && timeValue - tempData[tempData.length - 1].x > 0.1) {
            chartData.push(tempData);
            tempData = []
          }
          tempData.push({
            x: timeValue,
            y: this.frequencyToMidi(frequencyValue)
          })
        }
      }
      if (tempData.length > 0) {
        chartData.push(tempData)
      }
      return chartData
    },
    prepareCombinedChartData () {
      if (!this.dataLoading) {
        this.dataLoading = true;
        const chartData = [];
        for (let i = 0; i < this.midiChartData.length; i++) {
          const tempData = []
          tempData[0] = { ...this.midiChartData[i][0] };
          tempData[1] = { ...this.midiChartData[i][1] };
          if (tempData[0].x >= this.chartPosition.startTime && tempData[1].x <= this.chartPosition.endTime) {
            chartData.push({
              data: tempData,
              borderColor: '#ac2b2b',
              fill: false,
              radius: 5,
              showLine: true,
              isDummyData: false })
          } else if (tempData[0].x <= this.chartPosition.startTime && tempData[1].x >= this.chartPosition.startTime) {
            tempData[0].x = this.chartPosition.startTime;
            chartData.push({
              data: tempData,
              borderColor: '#ac2b2b',
              fill: false,
              radius: 5,
              showLine: true,
              isDummyData: false })
          } else if (tempData[0].x <= this.chartPosition.endTime && tempData[1].x >= this.chartPosition.endTime) {
            tempData[1].x = this.chartPosition.endTime;
            chartData.push({
              data: tempData,
              borderColor: '#ac2b2b',
              fill: false,
              radius: 5,
              showLine: true,
              isDummyData: false })
          }
        }
        for (let i = 0; i < this.baseFrequencyChartData.length; i++) {
          const tempData = [];
          for (let j = 0; j < this.baseFrequencyChartData[i].length; j++) {
            if (this.chartPosition.startTime <= this.baseFrequencyChartData[i][j].x && this.chartPosition.endTime >= this.baseFrequencyChartData[i][j].x) {
              tempData.push(this.baseFrequencyChartData[i][j]);
            }
          }
          if (tempData.length > 0) {
            chartData.push({
              data: tempData,
              radius: 1,
              borderColor: '#3e95cd',
              fill: false,
              showLine: true,
              dragData: false,
              isDummyData: false
            })
          }
        }
        // due to chartjs + peaks.js compatibility problems,
        // we add dummy, invisible data at the start and end of chart currently showing data to provide proper handling of data range
        const dummyDataForProperDisplaying = []
        const dummyValue = chartData.length > 0 ? chartData[0].data[0].y : 60;
        dummyDataForProperDisplaying.push({
          x: this.chartPosition.startTime,
          y: dummyValue
        })
        dummyDataForProperDisplaying.push({
          x: this.chartPosition.endTime,
          y: dummyValue
        })
        chartData.push({
          data: dummyDataForProperDisplaying,
          radius: 0,
          fill: false,
          showLine: false,
          dragData: false,
          isDummyData: true
        })
        this.chartData = {
          datasets: chartData
        };
        this.loaded = true;
        this.dataLoading = false
      }
    },
    zoomIn () {
      if (this.peaksInstance) {
        this.peaksInstance.zoom.zoomIn();
      }
    },
    zoomOut () {
      if (this.peaksInstance) {
        this.peaksInstance.zoom.zoomOut();
      }
    },
    buildWaveform () {
      const vm = this;
      const audioContext = new AudioContext();
      fetch(vm.mp3Url)
        .then(response => response.arrayBuffer())
        .then((buffer) => {
          const options = {
            audio_context: audioContext,
            array_buffer: buffer,
            scale: 128
          };
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
          // calculate pixels per one second in zoomview
          const audioSampleRate = audioContext.sampleRate;
          const imageWidth = document.getElementById('zoomview-container').clientWidth;
          const oneSecondZoom = Math.round(audioSampleRate / imageWidth);

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
            zoomLevels: [2 * oneSecondZoom, 4 * oneSecondZoom, 6 * oneSecondZoom, 8 * oneSecondZoom, 10 * oneSecondZoom],
            emitCueEvents: true,
            logger: console.error.bind(console),
            interactive: false
          };
          // eslint-disable-next-line handle-callback-err
          vm.peaksInstance = Peaks.init(options, function (err, peaks) {
          });
          vm.peaksInstance.zoom.setZoom(2);
          // zoomview.displaying event is fired when zoomview range changes, so then we update chart data to match new range
          vm.peaksInstance.on('zoomview.displaying', function (startTime, endTime) {
            startTime = Math.round(startTime);
            endTime = Math.round(endTime);
            if (vm.chartPosition.startTime !== startTime || vm.chartPosition.endTime !== endTime) {
              vm.chartPosition.startTime = startTime;
              vm.chartPosition.endTime = endTime;
              // update zoomview to start at full seconds
              const zoomview = vm.peaksInstance.views.getView('zoomview');
              if (zoomview) {
                const newPixels = zoomview.timeToPixels(vm.chartPosition.startTime);
                zoomview._updateWaveform(newPixels)
              }
              vm.prepareCombinedChartData()
              vm.currentlySelectedData.index = -1
            }
          });
          vm.peaksInstance.on('player_play', function (time) {
            // draw chart vertical line
            vm.reloadChartContext();
            vm.drawChartPlayingLine();
            if (!vm.soundOptionsSelected.includes('original')) {
              document.querySelector('audio').muted = true
            }
            if (vm.soundOptionsSelected.includes('midi')) {
              vm.midiPlaying = true;
              vm.playMidi(time)
            }
          });
          vm.peaksInstance.on('player_pause', function () {
            clearInterval(vm.drawVerticalLineId);
            vm.midiPlaying = false;
            if (vm.midiAudioContext) {
              vm.midiAudioContext.close();
            }
          });
          vm.peaksInstance.on('player_seek', function (time) {
            if (vm.soundOptionsSelected.includes('midi') && vm.midiPlaying) {
              if (vm.midiAudioContext) {
                vm.midiAudioContext.close();
              }
              vm.playMidi(time);
            }
          });
        })
    },
    drawChartPlayingLine () {
      const vm = this;
      if (vm.$children[0]) {
        const c = vm.$children[0].$refs.canvas;
        if (c) {
          const ctx = c.getContext('2d');
          vm.saveChartContext();
          vm.drawVerticalLineId = setInterval(function () {
            if (!vm.dataLoading) {
              vm.playerTime = vm.peaksInstance.player.getCurrentTime();
              const xposition = vm.$children[0].$data._chart.scales['x-axis-1'].getPixelForValue(vm.playerTime);
              ctx.fillStyle = '#ff0000';
              vm.reloadChartContext();
              ctx.beginPath();
              ctx.moveTo(xposition, 0);
              ctx.strokeStyle = '#ff0000';
              ctx.lineTo(xposition, c.height + 200);
              ctx.stroke()
            }
          }, 10)
        }
      }
    },
    saveChartContext () {
      if (this.$children[0]) {
        const c = this.$children[0].$refs.canvas;
        if (c) {
          const ctx = c.getContext('2d');
          this.contextImageData = ctx.getImageData(0, 0, c.width, c.height)
        }
      }
    },
    reloadChartContext () {
      if (this.$children[0]) {
        const c = this.$children[0].$refs.canvas;
        if (c) {
          const ctx = c.getContext('2d');
          ctx.putImageData(this.contextImageData, 0, 0)
        }
      }
    },
    playMidi (playerTime) {
      const vm = this;
      vm.midiAudioContext = new AudioContext();
      for (let i = 0; i < vm.midiChartData.length; i++) {
        if (vm.midiChartData[i] && vm.midiChartData[i][0] && vm.midiChartData[i][0].x >= playerTime && vm.midiPlaying) {
          const o = vm.midiAudioContext.createOscillator();
          o.frequency.setTargetAtTime(vm.midiToFrequency(vm.midiChartData[i][0].y), vm.midiAudioContext.currentTime, 0);
          o.connect(vm.midiAudioContext.destination);
          o.start(vm.midiChartData[i][0].x - playerTime);
          o.stop(vm.midiChartData[i][1].x - playerTime);
        }
      }
    },
    frequencyToMidi (frequency) {
      return Math.log(frequency / 440.0) / Math.log(2) * 12 + 69;
    },
    midiToFrequency (midiValue) {
      return 440.0 * 2.0 ** ((midiValue - 69) / 12);
    },
    setCurrentSelection (element) {
      const dataSetIndex = element.length > 1 ? element[0]._datasetIndex : element._datasetIndex;
      if (dataSetIndex !== undefined && this._data.chartData.datasets[dataSetIndex].data.length === 2) {
        if (this._data.currentlySelectedData.index !== dataSetIndex) {
          // deselect last value
          if (this._data.currentlySelectedData.index !== -1) {
            this._data.chartData.datasets[this._data.currentlySelectedData.index].borderColor = '#ac2b2b';
          }
          this._data.chartData.datasets[dataSetIndex].borderColor = '#0afc2a';
          this.$refs.chart._data._chart.update(0);
        }
        this._data.currentlySelectedData.index = dataSetIndex;
        this._data.currentlySelectedData.startTime = this._data.chartData.datasets[dataSetIndex].data[0].x
        this._data.currentlySelectedData.endTime = this._data.chartData.datasets[dataSetIndex].data[1].x
        this._data.currentlySelectedData.value = this._data.chartData.datasets[dataSetIndex].data[0].y
      }
    },
    updateCurrentMidiChartData () {
    }
  }
}
</script>

<style>
  .chartContainer {
    width: 100%;
    overflow-x: hidden;
  }
  .zoomviewContainer {
    width: calc(100% - 2.5em);
    margin-left: 1.5em;
  }
  .controlButtons {
    width: 50px;
    height: 50px;
  }
  #buttons-container {
    background-color: #f7f7f7;
  }
</style>
