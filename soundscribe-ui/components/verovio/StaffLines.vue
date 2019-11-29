<template>
  <div>
    <p>Some text</p>
    <textarea id="abc-source" v-model="tune" />
    <div class="listener-output">
      <div class="label">
        Currently Playing: <span class="abc-string">{{ currentAbcFragment }}</span>
      </div>

      <div class="label">
        Parameters sent to listener callback:
      </div>
      <div>Progress: {{ progress.progress }}</div>
      <div>Current Time: {{ progress.currentTime }}</div>
      <div>Total Duration: {{ progress.duration }}</div>
      <div>New Beat? {{ progress.newBeat }}</div>
    </div>
    <div id="midi" />
    <div id="paper" />
  </div>
</template>

<script>
import 'abcjs/abcjs-midi.css';
import abcjs from 'abcjs/midi';
import axios from '../../.nuxt/axios';
const fs = require('fs');
const $ = require('jquery');

export default {
  name: 'StaffLines',
  head () {
    return {
    }
  },
  data () {
    return {
      abcjsEditor: null,
      progress: { },
      currentAbcFragment: '(none)',
      tune: 'X:1\nT: Cooley\'s\nM: 4/4\nL: 1/8\nR: reel\nK: Emin\nD2DD||'
    }
  },
  mounted () {
    this.abcjsEditor = new abcjs.Editor('abc-source', {
      canvas_id: 'paper',
      generate_midi: true,
      midi_id: 'midi',
      abcjsParams: {
        midiListener: this.listener,
        animate: {
          listener: this.animate
        }
      }
    });
    this.loadData();
  },
  methods: {
    listener (midiControl, progress) {
      // This provides a more linear view of the progress, for a progress bar or for an unrelated animation.
      this.progress = progress;
    },
    colorRange (range, color) {
      if (range && range.elements) {
        range.elements.forEach(function (set) {
          set.forEach(function (item) {
            item.setAttribute('fill', color);
          });
        });
      }
    },
    animate (lastRange, currentRange) {
      // This provides the actual visual note being played. It can be used to create the "bouncing ball" effect.
      this.colorRange(lastRange, '#000000'); // Set the old note back to black.
      this.colorRange(currentRange, '#3D9AFC'); // Set the currently sounding note to blue.
      if (currentRange) {
        this.currentAbcFragment = this.tune.substring(currentRange.startChar, currentRange.endChar);
      } else {
        this.currentAbcFragment = '(none)';
      }
    },
    async loadData () {
      const data = await this.$axios.$get('/songs/MRegWOs_T0006A_01.abc');
      console.log(data);
      this.tune = data;
    }
  }
}
</script>

<style scoped lang="scss">
  .hello {
    text-align: left;
  }
  #abc-source {
    width: 460px;
    height: 160px;
    padding: 6px;
  }

  .listener-output {
    border: 1px solid #888888;
    padding: 6px;
    border-radius: 4px;
    width: 460px;
    margin-bottom: 20px;
  }

  .abc-string {
    border: 1px solid #e9ef96;
    padding: 2px;
    height: 24px;
    width: 60px;
    display: inline-block;
    background: #fbf4b8;
  }

  pre {
    border: 1px solid #888888;
    padding: 6px;
    border-radius: 4px;
    width: 460px;
  }

  #midi {
    width: 756px;
  }

  .label {
    font-weight: bold;
  }
</style>
