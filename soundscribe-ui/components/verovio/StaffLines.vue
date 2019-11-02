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
    <div
      id="svg_output"
    />
  </div>
</template>

<script>
const fs = require('fs');
const $ = require('jquery');
const verovio = require('verovio').init(512);

export default {
  name: 'StaffLines',
  data () {
    return {
      vrvToolkit: new verovio.toolkit(),
      zoom: 50,
      pageHeight: 2970,
      pageWidth: 2100,
      page: 1
    }
  },
  async mounted () {
    const file = '/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    // const file = 'https://raw.githubusercontent.com/rism-ch/verovio-tutorial/gh-pages/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    const data = await this.$axios.get(this.$axios.defaults.baseURL + file).then(res => res.data);
    console.log(this.$axios.defaults.baseURL + file);
    this.loadData(data);
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
    }
  }
}
</script>

<style scoped>

</style>
