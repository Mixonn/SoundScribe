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
    <div style="height: 30px;">
      <div id="player" />
    </div>
    <div
      id="svg_output"
    />
  </div>
</template>

<script>
import './wildwebmidi.js'

const fs = require('fs');
const $ = require('jquery');
const verovio = require('verovio').init(512);
const midiPlayer = require('./midiplayer');

export default {
  name: 'StaffLines',
  data () {
    return {
      vrvToolkit: new verovio.toolkit(),
      zoom: 50,
      pageHeight: 2970,
      pageWidth: 2100,
      page: 1,
      ids: [],
      isPlaying: false
    }
  },
  async mounted () {
    const file = '/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    // const file = 'https://raw.githubusercontent.com/rism-ch/verovio-tutorial/gh-pages/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    const data = await this.$axios.get(this.$axios.defaults.baseURL + file).then(res => res.data);
    console.log(this.$axios.defaults.baseURL + file);
    this.loadData(data);

    // $(window).resize(function () {
    //   this.refresh();
    // });
    //
    // $('#player').midiPlayer({
    //   color: '#c00',
    //   onUpdate: this.midiUpdate,
    //   onStop: this.midiStop,
    //   width: 250
    // });
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
    play_midi () {
      if (this.isPlaying === false) {
        const base64midi = this.vrvToolkit.renderToMIDI();
        const song = 'data:audio/midi;base64,' + base64midi;
        $('#player').show();
        $('#player').midiPlayer.play(song);
        this.isPlaying = true;
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
      $('#player').hide();
      this.isPlaying = false;
    }
  }
}
</script>

<style scoped>

</style>
