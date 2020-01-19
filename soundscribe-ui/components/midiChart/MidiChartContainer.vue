<template>
  <div class="app">
    <v-snackbar
      v-model="snackbar.display"
      :bottom="true"
      :color="snackbar.success === true ? 'success' : 'error'"
      :timeout="snackbar.timeout"
    >
      {{ snackbar.text }}
      <v-btn
        dark
        text
        @click="snackbar.display = false"
      >
      </v-btn>
    </v-snackbar>
    <div id="buttons-container">
      <ImgTooltip src="/buttons/undo.png" tooltip="Undo last move" @clicked="undoMove" />
      <ImgTooltip src="/buttons/redo.png" tooltip="Redo last move" @clicked="redoMove" />
      <ImgTooltip src="/buttons/plus.png" tooltip="Add new midi" @clicked="addMidi" />
      <ImgTooltip src="/buttons/minus.png" tooltip="Remove seleted Midi" @clicked="removeMidi" />
      <ImgTooltip src="/buttons/mergeWP.png" tooltip="Merge current Midi with previous" @clicked="mergeWP" />
      <ImgTooltip src="/buttons/mergeWN.png" tooltip="Merge current Midi with next" @clicked="mergeWN" />
      <ImgTooltip src="/buttons/split.png" tooltip="Split midi into two" @clicked="split" />
      <ImgTooltip src="/buttons/zoomIn.png" tooltip="Zoom in" @clicked="zoomIn" />
      <ImgTooltip src="/buttons/zoomOut.png" tooltip="Zoom out" @clicked="zoomOut" />
      <ImgTooltip src="/buttons/update.png" tooltip="Update file one server" @clicked="updateMidiOnServer" />
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
      <div id="overview-container" class="overviewContainer" />
    </div>
    <div id="audio-controls-container" class="audioControlsContainer">
      <audio controls="controls" style="width: 50%;">
        <source :src="mp3Url" type="audio/mpeg">
      </audio>
      <div id="audio-controls">
        <input id="original-audio" v-model="soundOptionsSelected" type="checkbox" value="original">
        <label for="original-audio">Original</label>
        <input id="midi-audio" v-model="soundOptionsSelected" type="checkbox" value="midi">
        <label for="midi-audio">Midi</label>
      </div>
    </div>
  </div>
</template>

<script>
import WaveformData from 'waveform-data'
import Peaks from 'peaks.js'
import ImgTooltip from '../ImgTooltip';
import { getNoteFromMidi } from './midiNotes.js'
import MidiChart from './midiChart'
import 'chartjs-plugin-dragdata'

export default {
  name: 'MidiChartContainer',
  components: {
    MidiChart,
    ImgTooltip
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
        endTime: 6
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
      snackbar: {
        text: '',
        display: false,
        timeout: 5000,
        success: true
      },
      currentlySelectedData: {
        index: -1,
        startTime: -1,
        endTime: -1,
        value: -1
      },
      movesToUndo: [],
      movesToUndoLimit: 50,
      movesToRedo: [],
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
              stepSize: 1,
              callback: (value) => {
                return this.getNoteSymbolByMidiValue(value) + ' - ' + value;
              }
            }
          }],
          xAxes: [{
            ticks: {
              stepSize: 1
            }
          }]
        },
        animation: {
          duration: 0,
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
          // round and move midi as a line (whole dataset)
          const xValue = value.x.toFixed(2);
          value.x = typeof xValue === 'string' ? parseFloat(xValue) : xValue;
          value.y = Math.round(value.y);
          this._data.chartData.datasets[datasetIndex].data[1 - index].y = value.y;
        },
        onDragEnd: (e, datasetIndex, index, value) => {
          this.updateDraggedMidiEvent()
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
        this.saveChartContext()
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
          const oneSecondZoom = audioSampleRate / imageWidth;

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
            zoomLevels: [Math.round(2 * oneSecondZoom), Math.round(4 * oneSecondZoom), Math.round(6 * oneSecondZoom),
              Math.round(8 * oneSecondZoom), Math.round(10 * oneSecondZoom), Math.round(15 * oneSecondZoom),
              Math.round(20 * oneSecondZoom), Math.round(30 * oneSecondZoom), Math.round(45 * oneSecondZoom),
              Math.round(60 * oneSecondZoom), (90 * oneSecondZoom), (120 * oneSecondZoom),
              Math.round(180 * oneSecondZoom), Math.round(240 * oneSecondZoom), (300 * oneSecondZoom),
              Math.round(1200 * oneSecondZoom)],
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
      if (vm.$children[1]) {
        const c = vm.$children[1].$refs.canvas;
        if (c) {
          const ctx = c.getContext('2d');
          vm.saveChartContext();
          vm.drawVerticalLineId = setInterval(function () {
            if (!vm.dataLoading) {
              vm.playerTime = vm.peaksInstance.player.getCurrentTime();
              const xposition = vm.$children[1].$data._chart.scales['x-axis-1'].getPixelForValue(vm.playerTime);
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
      if (this.$children[1]) {
        const c = this.$children[1].$refs.canvas;
        if (c) {
          const ctx = c.getContext('2d');
          this.contextImageData = ctx.getImageData(0, 0, c.width, c.height)
        }
      }
    },
    reloadChartContext () {
      if (this.$children[1]) {
        const c = this.$children[1].$refs.canvas;
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
    getNoteSymbolByMidiValue (value) {
      if (value < 21) {
        return '';
      } else if (value > 127) {
        return '';
      } else {
        return getNoteFromMidi(value);
      }
    },
    setCurrentSelection (element) {
      const dataSetIndex = element.length > 1 ? element[0]._datasetIndex : element._datasetIndex;
      if (dataSetIndex !== undefined && this._data.chartData.datasets[dataSetIndex].data.length === 2) {
        if (this._data.currentlySelectedData.index !== -1 && this._data.currentlySelectedData.index < this._data.chartData.datasets.length) {
          this._data.chartData.datasets[this._data.currentlySelectedData.index].borderColor = '#ac2b2b';
        }
        this._data.chartData.datasets[dataSetIndex].borderColor = '#0afc2a';
        this.$refs.chart._data._chart.update(0);
        this._data.currentlySelectedData.index = dataSetIndex;
        this._data.currentlySelectedData.startTime = this._data.chartData.datasets[dataSetIndex].data[0].x
        this._data.currentlySelectedData.endTime = this._data.chartData.datasets[dataSetIndex].data[1].x
        this._data.currentlySelectedData.value = this._data.chartData.datasets[dataSetIndex].data[0].y
      }
    },
    updateDraggedMidiEvent () {
      if (this.currentlySelectedData.startTime !== this.chartData.datasets[this.currentlySelectedData.index].data[0].x ||
          this.currentlySelectedData.endTime !== this.chartData.datasets[this.currentlySelectedData.index].data[1].x ||
          this.currentlySelectedData.value !== this.chartData.datasets[this.currentlySelectedData.index].data[0].y) {
        let selectedEvent = this.midiChartData.find((midiEvent) => {
          return midiEvent[0].x === this.currentlySelectedData.startTime &&
            midiEvent[1].x === this.currentlySelectedData.endTime
        });
        if (selectedEvent) {
          selectedEvent[0].x = this.chartData.datasets[this.currentlySelectedData.index].data[0].x;
          selectedEvent[1].x = this.chartData.datasets[this.currentlySelectedData.index].data[1].x;
          selectedEvent[0].y = this.chartData.datasets[this.currentlySelectedData.index].data[0].y;
          selectedEvent[1].y = this.chartData.datasets[this.currentlySelectedData.index].data[1].y;
          if (selectedEvent[0].x > selectedEvent[1].x) {
            const tmp = selectedEvent[0].x;
            selectedEvent[0].x = selectedEvent[1].x;
            selectedEvent[1].x = tmp;
          }
          this.addMoveToUndoList(this.currentlySelectedData, selectedEvent);
          this.currentlySelectedData.startTime = selectedEvent[0].x;
          this.currentlySelectedData.endTime = selectedEvent[1].x;
          this.currentlySelectedData.value = selectedEvent[0].y;
        } else {
          selectedEvent = this.midiChartData.find((midiEvent) => {
            return midiEvent[0].y === this.currentlySelectedData.value &&
              ((midiEvent[0].x === this.currentlySelectedData.startTime && midiEvent[1].x >= this.chartPosition.endTime) ||
              (midiEvent[0].x <= this.chartPosition.startTime && midiEvent[1].x === this.currentlySelectedData.endTime))
          });
          if (selectedEvent) {
            selectedEvent[0].y = this.chartData.datasets[this.currentlySelectedData.index].data[0].y;
            selectedEvent[1].y = this.chartData.datasets[this.currentlySelectedData.index].data[1].y;
            if (selectedEvent[0].x === this.currentlySelectedData.startTime && selectedEvent[1].x >= this.chartPosition.endTime) {
              selectedEvent[0].x = this.chartData.datasets[this.currentlySelectedData.index].data[0].x;
            } else {
              selectedEvent[1].x = this.chartData.datasets[this.currentlySelectedData.index].data[1].x;
            }
            this.addMoveToUndoList(this.currentlySelectedData, selectedEvent);
            this.currentlySelectedData.startTime = selectedEvent[0].x;
            this.currentlySelectedData.endTime = selectedEvent[1].x;
            this.currentlySelectedData.value = selectedEvent[0].y;
          }
        }
      }
    },
    addMoveToUndoList (oldData, newData) {
      this.movesToUndo.push({
        fromStartTime: oldData.startTime,
        fromEndTime: oldData.endTime,
        fromValue: oldData.value,
        toStartTime: newData[0].x,
        toEndTime: newData[1].x,
        toValue: newData[0].y
      });
      if (this.movesToRedo.length > 0) {
        this.movesToRedo.length = 0;
      }
      if (this.movesToUndo.length > this.movesToUndoLimit) {
        this.movesToUndo.shift();
      }
    },
    addMoveToRedoList (move) {
      this.movesToRedo.push(move);
      if (this.movesToRedo.length > this.movesToUndoLimit) {
        this.movesToRedo.shift()
      }
    },
    undoMove () {
      const moveToApply = this.movesToUndo.pop();
      if (moveToApply && moveToApply.fromStartTime !== null && moveToApply.toStartTime !== null) {
        this.addMoveToRedoList(moveToApply);
        const dataToChange = this.midiChartData.find((midiEvent) => {
          return midiEvent[0].x === moveToApply.toStartTime &&
              midiEvent[1].x === moveToApply.toEndTime &&
              midiEvent[0].y === moveToApply.toValue
        });
        if (dataToChange) {
          dataToChange[0].x = moveToApply.fromStartTime;
          dataToChange[1].x = moveToApply.fromEndTime;
          dataToChange[0].y = moveToApply.fromValue;
          dataToChange[1].y = moveToApply.fromValue;
          if (moveToApply.toStartTime >= this.chartPosition.startTime && moveToApply.toEndTime <= this.chartPosition.endTime) {
            const chartValueToChange = this.chartData.datasets.find((midiEvent) => {
              return midiEvent.data.length === 2 &&
                midiEvent.data[0].x === moveToApply.toStartTime &&
                midiEvent.data[1].x === moveToApply.toEndTime &&
                midiEvent.data[0].y === moveToApply.toValue
            })
            if (chartValueToChange) {
              chartValueToChange.data[0].x = moveToApply.fromStartTime;
              chartValueToChange.data[1].x = moveToApply.fromEndTime;
              chartValueToChange.data[0].y = moveToApply.fromValue;
              chartValueToChange.data[1].y = moveToApply.fromValue;
              this.$refs.chart._data._chart.update(0);
            }
          }
        }
      } else if (moveToApply && (moveToApply.fromStartTime === null || moveToApply.toStartTime === null)) {
        this.addMoveToRedoList(moveToApply);
        // handle add or midi delete
        if (moveToApply.toStartTime === null) {
          this.addMidiFromData({
            startTime: moveToApply.fromStartTime,
            endTime: moveToApply.fromEndTime,
            value: moveToApply.fromValue,
            index: this.chartData.datasets.length
          })
        } else {
          const chartValueToChange = this.chartData.datasets.findIndex((midiEvent) => {
            return midiEvent.data.length === 2 &&
              midiEvent.data[0].x === moveToApply.toStartTime &&
              midiEvent.data[1].x === moveToApply.toEndTime &&
              midiEvent.data[0].y === moveToApply.toValue
          })
          this.removeMidiFromData({
            startTime: moveToApply.toStartTime,
            endTime: moveToApply.toEndTime,
            value: moveToApply.toValue,
            index: chartValueToChange
          })
        }
      }
    },
    redoMove () {
      const moveToApply = this.movesToRedo.pop();
      if (moveToApply && moveToApply.fromStartTime !== null && moveToApply.toStartTime !== null) {
        this.movesToUndo.push(moveToApply);
        const dataToChange = this.midiChartData.find((midiEvent) => {
          return midiEvent[0].x === moveToApply.fromStartTime &&
            midiEvent[1].x === moveToApply.fromEndTime &&
            midiEvent[0].y === moveToApply.fromValue
        });
        if (dataToChange) {
          dataToChange[0].x = moveToApply.toStartTime;
          dataToChange[1].x = moveToApply.toEndTime;
          dataToChange[0].y = moveToApply.toValue;
          dataToChange[1].y = moveToApply.toValue;
          if (moveToApply.toStartTime >= this.chartPosition.startTime && moveToApply.toEndTime <= this.chartPosition.endTime) {
            const chartValueToChange = this.chartData.datasets.find((midiEvent) => {
              return midiEvent.data.length === 2 &&
                midiEvent.data[0].x === moveToApply.fromStartTime &&
                midiEvent.data[1].x === moveToApply.fromEndTime &&
                midiEvent.data[0].y === moveToApply.fromValue
            });
            if (chartValueToChange) {
              chartValueToChange.data[0].x = moveToApply.toStartTime;
              chartValueToChange.data[1].x = moveToApply.toEndTime;
              chartValueToChange.data[0].y = moveToApply.toValue;
              chartValueToChange.data[1].y = moveToApply.toValue;
              this.$refs.chart._data._chart.update(0);
            }
          }
        }
      } else if (moveToApply && (moveToApply.fromStartTime === null || moveToApply.toStartTime === null)) {
        // handle add or midi delete
        this.movesToUndo.push(moveToApply);
        if (moveToApply.fromStartTime === null) {
          this.addMidiFromData({
            startTime: moveToApply.toStartTime,
            endTime: moveToApply.toEndTime,
            value: moveToApply.toValue,
            index: this.chartData.datasets.length
          })
        } else {
          const chartValueToChange = this.chartData.datasets.findIndex((midiEvent) => {
            return midiEvent.data.length === 2 &&
              midiEvent.data[0].x === moveToApply.fromStartTime &&
              midiEvent.data[1].x === moveToApply.fromEndTime &&
              midiEvent.data[0].y === moveToApply.fromValue
          });
          this.removeMidiFromData({
            startTime: moveToApply.fromStartTime,
            endTime: moveToApply.fromEndTime,
            value: moveToApply.fromValue,
            index: chartValueToChange
          })
        }
      }
    },
    addMidi () {
      if (this.currentlySelectedData.endTime + 0.3 < this.chartPosition.endTime) {
        this.currentlySelectedData.startTime = this.currentlySelectedData.endTime + 0.1;
        this.currentlySelectedData.endTime = this.currentlySelectedData.endTime + 0.3;
        this.movesToUndo.push({
          fromStartTime: null,
          fromEndTime: null,
          fromValue: null,
          toStartTime: this.currentlySelectedData.startTime,
          toEndTime: this.currentlySelectedData.endTime,
          toValue: this.currentlySelectedData.value
        });
        if (this.movesToRedo.length > 0) {
          this.movesToRedo.length = 0;
        }
        if (this.movesToUndo.length > this.movesToUndoLimit) {
          this.movesToUndo.shift();
        }
        this.addMidiFromData({ ...this.currentlySelectedData });
      }
    },
    addMidiFromData (data) {
      const midiEvent = [];
      midiEvent.push({
        x: data.startTime,
        y: data.value
      });
      midiEvent.push({
        x: data.endTime,
        y: data.value
      });
      this.midiChartData.push([{
        x: data.startTime,
        y: data.value
      },
      {
        x: data.endTime,
        y: data.value
      }
      ]);
      this.chartData.datasets.push({
        data: midiEvent,
        borderColor: '#0afc2a',
        fill: false,
        radius: 5,
        showLine: true,
        isDummyData: false })
      if (data.index > -1) {
        this.chartData.datasets[data.index].borderColor = '#ac2b2b';
        this.$refs.chart._data._chart.update(0);
        this.currentlySelectedData.index = this.chartData.datasets.length - 1;
      }
    },
    removeMidi () {
      if (this.currentlySelectedData.index > -1) {
        this.movesToUndo.push({
          fromStartTime: this.currentlySelectedData.startTime,
          fromEndTime: this.currentlySelectedData.endTime,
          fromValue: this.currentlySelectedData.value,
          toStartTime: null,
          toEndTime: null,
          toValue: null
        });
        if (this.movesToRedo.length > 0) {
          this.movesToRedo.length = 0;
        }
        if (this.movesToUndo.length > this.movesToUndoLimit) {
          this.movesToUndo.shift();
        }
        this.removeMidiFromData(this.currentlySelectedData);
      }
    },
    removeMidiFromData (data) {
      if (data.index > -1) {
        this.chartData.datasets.splice(data.index, 1);
      }
      const selectedMidiIndex = this.midiChartData.findIndex((midiEvent) => {
        return midiEvent[0].x === data.startTime &&
          midiEvent[1].x === data.endTime &&
          midiEvent[0].y === data.value
      });
      this.midiChartData.splice(selectedMidiIndex, 1);
      this.$refs.chart._data._chart.update(0);
    },
    mergeWP () {
      if (this.currentlySelectedData.index > 0) {
        this.movesToUndo.push({
          fromStartTime: this.currentlySelectedData.startTime,
          fromEndTime: this.currentlySelectedData.endTime,
          fromValue: this.currentlySelectedData.value,
          toStartTime: null,
          toEndTime: null,
          toValue: null
        });
        this.removeMidiFromData({
          startTime: this.currentlySelectedData.startTime,
          endTime: this.currentlySelectedData.endTime,
          value: this.currentlySelectedData.value,
          index: this.currentlySelectedData.index
        })
        let min = 100;
        let idx = 0;
        for (let i = 0; i < this.chartData.datasets.length; i++) {
          if (this.chartData.datasets[i].data.length === 2) {
            const value = this.currentlySelectedData.startTime - this.chartData.datasets[i].data[1].x;
            if (value > 0 && value < min) {
              min = value;
              idx = i;
            }
          }
        }
        this.movesToUndo.push({
          fromStartTime: this.chartData.datasets[idx].data[0].x,
          fromEndTime: this.chartData.datasets[idx].data[1].x,
          fromValue: this.chartData.datasets[idx].data[0].y,
          toStartTime: this.chartData.datasets[idx].data[0].x,
          toEndTime: this.currentlySelectedData.endTime,
          toValue: this.chartData.datasets[idx].data[0].y
        });
        const dataToChange = this.midiChartData.find((midiEvent) => {
          return midiEvent[0].x === this.chartData.datasets[idx].data[0].x &&
            midiEvent[1].x === this.chartData.datasets[idx].data[1].x &&
            midiEvent[0].y === this.chartData.datasets[idx].data[0].y
        });
        if (dataToChange) {
          this.chartData.datasets[idx].data[1].x = this.currentlySelectedData.endTime;
          dataToChange[1].x = this.currentlySelectedData.endTime;
          this.currentlySelectedData.startTime = dataToChange[0].x;
          this.currentlySelectedData.endTime = dataToChange[1].x;
          this.currentlySelectedData.value = dataToChange[0].y;
          this.currentlySelectedData.index = idx
          this.chartData.datasets[idx].borderColor = '#0afc2a';
          this.$refs.chart._data._chart.update(0);
        }
      }
    },
    mergeWN () {
      if (this.currentlySelectedData.index !== -1 && this.currentlySelectedData.index < this.chartData.datasets.length - 1) {
        this.movesToUndo.push({
          fromStartTime: this.currentlySelectedData.startTime,
          fromEndTime: this.currentlySelectedData.endTime,
          fromValue: this.currentlySelectedData.value,
          toStartTime: null,
          toEndTime: null,
          toValue: null
        });
        this.removeMidiFromData({
          startTime: this.currentlySelectedData.startTime,
          endTime: this.currentlySelectedData.endTime,
          value: this.currentlySelectedData.value,
          index: this.currentlySelectedData.index
        })
        // due to deletion of previous element
        let min = 100;
        let idx = 0;
        for (let i = 0; i < this.chartData.datasets.length; i++) {
          if (this.chartData.datasets[i].data.length === 2) {
            const value = this.chartData.datasets[i].data[0].x - this.currentlySelectedData.endTime;
            if (value > 0 && value < min) {
              min = value;
              idx = i;
            }
          }
        }
        this.movesToUndo.push({
          fromStartTime: this.chartData.datasets[idx].data[0].x,
          fromEndTime: this.chartData.datasets[idx].data[1].x,
          fromValue: this.chartData.datasets[idx].data[0].y,
          toStartTime: this.currentlySelectedData.startTime,
          toEndTime: this.chartData.datasets[idx].data[1].x,
          toValue: this.chartData.datasets[idx].data[0].y
        });
        const dataToChange = this.midiChartData.find((midiEvent) => {
          return midiEvent[0].x === this.chartData.datasets[idx].data[0].x &&
            midiEvent[1].x === this.chartData.datasets[idx].data[1].x &&
            midiEvent[0].y === this.chartData.datasets[idx].data[0].y
        });
        if (dataToChange) {
          this.chartData.datasets[idx].data[0].x = this.currentlySelectedData.startTime;
          dataToChange[1].x = this.currentlySelectedData.endTime;
          this.currentlySelectedData.startTime = dataToChange[0].x;
          this.currentlySelectedData.endTime = dataToChange[1].x;
          this.currentlySelectedData.value = dataToChange[0].y;
          this.currentlySelectedData.index = idx
          this.chartData.datasets[idx].borderColor = '#0afc2a';
          this.$refs.chart._data._chart.update(0);
        }
      }
    },
    split () {
      if (this.currentlySelectedData.index !== -1) {
        const oldEndTime = this.currentlySelectedData.endTime;
        const newMidTime = this.currentlySelectedData.startTime - 0.05 + (this.currentlySelectedData.endTime - this.currentlySelectedData.startTime) / 2;
        this.movesToUndo.push({
          fromStartTime: this.currentlySelectedData.startTime,
          fromEndTime: this.currentlySelectedData.startTime,
          fromValue: this.currentlySelectedData.value,
          toStartTime: this.currentlySelectedData.startTime,
          toEndTime: newMidTime,
          toValue: this.currentlySelectedData.value
        });
        const dataToChange = this.midiChartData.find((midiEvent) => {
          return midiEvent[0].x === this.chartData.datasets[this.currentlySelectedData.index].data[0].x &&
            midiEvent[1].x === this.chartData.datasets[this.currentlySelectedData.index].data[1].x &&
            midiEvent[0].y === this.chartData.datasets[this.currentlySelectedData.index].data[0].y
        });
        if (dataToChange) {
          this.chartData.datasets[this.currentlySelectedData.index].data[1].x = newMidTime;
          dataToChange[1].x = newMidTime;
          this.currentlySelectedData.startTime = dataToChange[0].x;
          this.currentlySelectedData.endTime = dataToChange[1].x;
          this.currentlySelectedData.value = dataToChange[0].y;
          this.chartData.datasets[this.currentlySelectedData.index].borderColor = '#0afc2a';
          this.movesToUndo.push({
            fromStartTime: null,
            fromEndTime: null,
            fromValue: null,
            toStartTime: newMidTime + 0.1,
            toEndTime: oldEndTime,
            toValue: this.currentlySelectedData.value
          });
          this.addMidiFromData({
            startTime: newMidTime + 0.1,
            endTime: oldEndTime,
            value: this.currentlySelectedData.value,
            index: -1
          })
          this.$refs.chart._data._chart.update(0);
        }
      }
    },
    async updateMidiOnServer () {
      const chartData = [];
      const requestBody = {};
      requestBody.song_name = this.fileNameFormatted;
      requestBody.bpm = this.midiFileContent.getElementsByTagName(this.fileNameFormatted)[0].getElementsByTagName('bpm')[0].textContent;
      requestBody.divisions = this.midiFileContent.getElementsByTagName(this.fileNameFormatted)[0].getElementsByTagName('divisions')[0].textContent;
      for (let i = 0; i < this.midiChartData.length; i++) {
        const midiEvent = {}
        midiEvent.begin = {
          timestamp: this.midiChartData[i][0].x,
          midiValue: this.midiChartData[i][0].y
        }
        midiEvent.end = {
          timestamp: this.midiChartData[i][1].x,
          midiValue: this.midiChartData[i][1].y
        }
        chartData.push(midiEvent)
      }
      requestBody.midi_elements = chartData;
      await this.$axios.post(`convert/update-file-midi`,
        requestBody)
        .then((r) => {
          this.snackbar.text = 'Plik został zaaktualizowany';
          this.snackbar.success = true;
          this.snackbar.display = true;
        }).catch((r) => {
          this.snackbar.text = 'Nie udało się zaaktualizować pliku';
          this.snackbar.success = false;
          this.snackbar.display = true;
        });
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
    margin-left: 3.9em;
  }
  .overviewContainer {
    margin-left: 3.9em;
  }
  .controlButtons {
    width: 50px;
    height: 50px;
  }
  .audioControlsContainer {
    width: 100%;
    overflow-x: hidden;
  }
  #buttons-container {
    background-color: #f7f7f7;
  }
</style>
