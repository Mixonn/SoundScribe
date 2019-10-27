<template>
  <div>
    <p>Some text</p>
    <div id="svg_output" />
  </div>
</template>

<script>
const $ = require('jquery');
const verovio = require('verovio').init(512);

const vrvToolkit = new verovio.toolkit();
const zoom = 50;
let pageHeight = 2970;
let pageWidth = 2100;

function setOptions () {
  /// ///////////////////////////////////////////////////////////
  /* Adjust the height and width according to the window size */
  /// ///////////////////////////////////////////////////////////
  pageHeight = $(document).height() * 100 / zoom;
  pageWidth = $(window).width() * 100 / zoom;
  const options = {
    pageHeight,
    pageWidth,
    scale: zoom,
    adjustPageHeight: true
  };
  vrvToolkit.setOptions(options);
}

function loadData (data) {
  setOptions();
  vrvToolkit.loadData(data);
  const svg = vrvToolkit.renderToSVG(1, {});
  $('#svg_output').html(svg);
}

export default {
  name: 'StaffLines',
  mounted () {
    // const file = '~/static/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    const file = 'https://raw.githubusercontent.com/rism-ch/verovio-tutorial/gh-pages/mei/Beethoven_StringQuartet_op.18_no.2.mei';
    $.ajax({
      url: file,
      dataType: 'text',
      success (data) {
        loadData(data);
      }
    });
  }
}
</script>

<style scoped>

</style>
