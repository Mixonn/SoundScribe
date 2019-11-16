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
import axios from 'axios';
const fs = require('fs');
const $ = require('jquery');
const verovio = require('verovio').init(512);

function str2ab (str) {
  const buf = new ArrayBuffer(str.length * 2); // 2 bytes for each char
  const bufView = new Uint8Array(buf);
  for (let i = 0, strLen = str.length; i < strLen; i++) {
    bufView[i] = str.charCodeAt(i);
  }
  return buf;
}

function ab2str (buf) {
  return String.fromCharCode.apply(null, new Uint16Array(buf));
}

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
      $('.note').click(function () {
        const id = $(this).attr('id');
        const time = this.vrvToolkit.getTimeForElement(id);
        $('#midi-player').midiPlayer.seek(time);
      });
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
    playMidi () {
      if (this.isPlaying === false) {
        const base64midi = this.vrvToolkit.renderToMIDI();
        const decodec = atob(base64midi);
        const song1 = 'data:audio/midi;base64,' + base64midi;
        console.log(base64midi);
        console.log(decodec);
        if (decodec.startsWith('MThd')) {
          console.log('EEEE');
        }
        const x = str2ab(decodec);

        const AudioContext = window.AudioContext || window.webkitAudioContext || false;
        const ac = new AudioContext();

        Soundfont.instrument(ac, `https://raw.githubusercontent.com/gleitz/midi-js-soundfonts/gh-pages/MusyngKite/acoustic_guitar_nylon-mp3.js`).then((instrument) => {
          this.player = new Player((event) => {
            if (!this.isPlaying) {
              this.player.stop();
            }
            if (event.name === 'Note on' && event.velocity > 0) {
              instrument.play(event.noteName, ac.currentTime, {
                gain: event.velocity / 100
              });
            }
          });

          axios.get(`/songs/ff.mid`, {
            responseType: 'arraybuffer'
          }).then(({ data: song }) => {
            console.log(ab2str(song));
            console.log(ab2str(x));
            console.log(song);
            console.log(x);
            console.log(song.constructor.name);
            console.log(x.constructor.name);
            this.player.loadArrayBuffer(x);
            // we have access to each track, and each track has events.
            // console.log(player.tracks);
            console.log('start playing');
            this.player.play();
            this.isPlaying = true;
          });
        });
        // this.player.loadArrayBuffer(str2ab(base64midi));
      }
    },
    midiUpdate (time) {
      // time needs to - 400 for adjustment
      const vrvTime = Math.max(0, time - 400);
      const elementsattime = this.vrvToolkit.getElementsAtTime(vrvTime);
      if (elementsattime.page > 0) {
        if (elementsattime.page !== this.page) {
          this.page = elementsattime.page;
          this.loadPage();
        }
        if ((elementsattime.notes.length > 0) && (this.ids !== elementsattime.notes)) {
          this.ids.forEach(function (noteid) {
            if ($.inArray(noteid, elementsattime.notes) === -1) {
              $('#' + noteid).attr('fill', '#000').attr('stroke', '#000');
            }
          });
          this.ids = elementsattime.notes;
          this.ids.forEach(function (noteid) {
            if ($.inArray(noteid, elementsattime.notes) !== -1) {
              $('#' + noteid).attr('fill', '#c00').attr('stroke', '#c00');
            }
          });
        }
      }
    },
    midiStop () {
      this.ids.forEach(function (noteid) {
        $('#' + noteid).attr('fill', '#000').attr('stroke', '#000');
      });
      this.isPlaying = false;
    }
  }
}
</script>

<style scoped>
</style>
