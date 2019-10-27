<template>
  <v-layout>
    <v-flex class="text-center">
      <img
        src="/v.png"
        alt="Vuetify.js"
        class="mb-5"
      >
      <blockquote class="blockquote">
        &#8220;First, solve the problem. Then, write the code.&#8221;
        <footer>
          <small>
            <em>&mdash;John Johnson</em>
          </small>
        </footer>
      </blockquote>
      <div class="panel-body">
        <div id="app" class="panel" style="border: 1px solid lightgray; min-height: 800px;" />
      </div>
      <script type="module">
        import 'verovio';

        const options = {
        defaultView: 'document', // instead of 'responsive' by default
        defaultZoom: 4 // 0-7, default is 3
        }

        // Create the app - here with an empty option object
        const app = new Verovio.App(document.getElementById("app"), {options});

        // Load a file (MEI or MusicXML)
        fetch("https://raw.githubusercontent.com/music-encoding/sample-encodings/master/MEI_4.0/Music/Complete_examples/Bach-J-C_Fughette_Gmaj.mei")
        .then(function(response) {
        return response.text();
        })
        .then(function(text) {
        app.loadData(text);
        });
      </script>
    </v-flex>
  </v-layout>
</template>
