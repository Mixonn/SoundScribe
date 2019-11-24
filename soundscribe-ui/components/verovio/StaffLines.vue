<template>
  <div>
    <p>Some text</p>
    <button @click="prevPage">
      Previous
    </button>
    <button @click="nextPage">
      Next
    </button>
    <button @click="zoomIn">
      Zoom in
    </button>
    <button @click="zoomOut">
      Zoom out
    </button>
    <button @click="playMidi">
      Play
    </button>
    <button @click="midiStop">
      Stop
    </button>
    <div
      id="svg_output"
    />
  </div>
</template>

<script>
import { Player } from 'midi-player-js';
import Soundfont from 'soundfont-player';
const fs = require('fs');
const $ = require('jquery');
const verovio = require('verovio').init(512);

export default {
  name: 'StaffLines',
  head () {
    return {
    }
  },
  data () {
    return {
      vrvToolkit: new verovio.toolkit(),
      zoom: 50,
      pageHeight: 2970,
      pageWidth: 2100,
      page: 1,
      ids: [],
      isPlaying: false,
      player: null
    }
  },
  async mounted () {
    const file = '/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    // const file = 'https://raw.githubusercontent.com/rism-ch/verovio-tutorial/gh-pages/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    const data = await this.$axios.get(this.$axios.defaults.baseURL + file).then(res => res.data);
    console.log(this.$axios.defaults.baseURL + file);
    this.loadData(data);
    const self = this;
    $('.note').on('click', function () {
      const id = $(this).attr('id');
      const time = self.vrvToolkit.getTimeForElement(id);
      if (self.isPlaying) {
        self.player.pause();
        self.player.skipToPercent((time / 1000) / self.player.getSongTime() * 100);
        self.player.play();
      } else {
        self.playMidi(time);
      }
    });
    this.player = new Player((this.midiUpdate));
    this.player.on('endOfFile', this.midiStop)
  },
  methods: {
    setOptions () {
      this.pageHeight = $(document).height() * 100 / this.zoom;
      this.pageWidth = $(window).width() * 100 / this.zoom;
      const options = {
        pageHeight: this.pageHeight,
        pageWidth: this.pageWidth,
        scale: this.zoom,
        adjustPageHeight: true
      };
      this.vrvToolkit.setOptions(options);
    },
    readFile () {
      // does not work for now
      return fs.readFileSync('/mei/Beethoven_StringQuartet_op.18_no.2.mei', 'utf-8')
    },
    loadPage () {
      const svg = this.vrvToolkit.renderToSVG(this.page, {});
      $('#svg_output').html(svg);
    },
    loadData (data) {
      this.setOptions();
      this.vrvToolkit.loadData(data);
      this.page = 1;
      this.loadPage();
    },
    refresh () {
      this.setOptions();
      this.vrvToolkit.redoLayout();
      this.page = 1;
      this.loadPage();
    },
    nextPage () {
      if (this.page >= this.vrvToolkit.getPageCount()) {
        return;
      }
      this.page = this.page + 1;
      this.loadPage();
    },
    prevPage () {
      if (this.page <= 1) {
        return;
      }
      this.page = this.page - 1;
      this.loadPage();
    },
    zoomOut () {
      if (this.zoom < 20) {
        return;
      }
      this.zoom = this.zoom / 2;
      this.refresh();
    },
    zoomIn () {
      if (this.zoom > 80) {
        return;
      }
      this.zoom = this.zoom * 2;
      this.refresh();
    },
    playMidi (startTime = 0) {
      if (this.isPlaying === false) {
        const base64midi = this.vrvToolkit.renderToMIDI();
        const song1 = 'data:audio/midi;base64,' + base64midi;
        const AudioContext = window.AudioContext || window.webkitAudioContext || false;
        const ac = new AudioContext();

        // Soundfont.instrument(ac, `https://gleitz.github.io/midi-js-soundfonts/MusyngKite/acoustic_guitar_nylon-mp3.js`).then((instrument) => {
        Soundfont.instrument(ac, 'acoustic_grand_piano', { soundfont: 'FluidR3_GM' }).then((instrument) => {
          this.player = new Player((event) => {
            this.midiUpdate(event, instrument, ac);
          });
          this.player.loadDataUri(song1);
          this.player.skipToPercent((startTime / 1000) / this.player.getSongTime() * 100);
          this.player.play();
          this.isPlaying = true;
        });
      }
    },
    midiUpdate (event, instrument, ac) {
      if (!this.isPlaying) {
        this.player.stop();
      }
      if (event.name === 'Note on' && event.velocity > 0) {
        instrument.play(event.noteName, ac.currentTime, {
          gain: event.velocity / 100
        });
      }
      const currentTime = this.player.getSongTime() - (this.player.totalTicks - this.player.getCurrentTick()) / this.player.division / this.player.tempo * 60;
      const elementsattime = this.vrvToolkit.getElementsAtTime(currentTime * 1000);
      // console.clear();
      // console.log(`Song time: ${this.player.getSongTime()}`);
      // console.log(`Song my time2: ${currentTime}`);
      // console.log(`Song time remaining: ${this.player.getSongTimeRemaining()}`);
      // console.log(elementsattime);
      if (elementsattime.page > 0) {
        if (elementsattime.page !== this.page) {
          this.page = elementsattime.page;
          this.loadPage();
        }
        if ((elementsattime.notes.length > 0) && (this.ids !== elementsattime.notes)) {
          this.ids.forEach(function (noteid) {
            if ($.inArray(noteid, elementsattime.notes) === -1) {
              $(`#${noteid}`).attr('fill', '#000').attr('stroke', '#000');
            }
          });
          this.ids = elementsattime.notes;
          this.ids.forEach(function (noteid) {
            if ($.inArray(noteid, elementsattime.notes) !== -1) {
              $(`#${noteid}`).attr('fill', '#c00').attr('stroke', '#c00');
            }
          });
        }
      }
    },
    midiStop () {
      this.isPlaying = false;
      this.ids.forEach(function (noteid) {
        $('#' + noteid).attr('fill', '#000').attr('stroke', '#000');
      });
    }
  }
}
</script>

<style scoped>
</style>
