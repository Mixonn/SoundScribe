<template>
  <div>
    <p>Some text</p>
    <textarea id="abc-source" ref="tuneInput" v-model="tune" />
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
    <button @click="nodeUp">
      Up
    </button>
    <button @click="nodeDown">
      Down
    </button>
    <button @click="removeNote">
      Remove
    </button>
    <div id="paper" />
  </div>
</template>

<script>
import 'abcjs/abcjs-midi.css';
import abcjs from 'abcjs/midi';
import { MODIFY_OPERATIONS, modifyNote, replaceSubstring } from './NodeModifier';
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
      tune: 'X:1\nT: Cooley\'s\nM: 4/4\nL: 1/8\nR: reel\nK: Emin\nD2DD||',
      currentNode: {
        id: -1,
        start: -1,
        end: -1
      }
    }
  },
  async mounted () {
    await this.loadData();
    this.abcjsEditor = new abcjs.Editor('abc-source', {
      canvas_id: 'paper',
      generate_midi: true,
      midi_id: 'midi',
      abcjsParams: {
        midiListener: this.listener,
        animate: {
          listener: this.animate
        },
        clickListener: this.onNodeClick
      }
    });
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
    },
    onNodeClick (abcElem, tuneNumber, classes) {
      console.log(abcElem);
      this.currentNode.start = abcElem.startChar;
      this.currentNode.end = abcElem.endChar;
      this.currentNode.id = abcElem.__ob__.id;
    },
    nodeUp () {
      this.modifyNoteOperation(MODIFY_OPERATIONS.UP);
    },
    nodeDown () {
      this.modifyNoteOperation(MODIFY_OPERATIONS.DOWN);
    },
    removeNote () {
      this.modifyNoteOperation(MODIFY_OPERATIONS.REMOVE)
    },
    modifyNoteOperation (operation) {
      const nodeStr = this.tune.substr(this.currentNode.start, (this.currentNode.end - this.currentNode.start));
      let modifiedNode = modifyNote(operation, nodeStr);
      if (nodeStr[nodeStr.length - 1] === ' ' && modifiedNode[modifiedNode.length - 1] !== ' ') {
        modifiedNode += ' ';
      }
      const z = replaceSubstring(this.tune, this.currentNode.start, this.currentNode.end + 1, modifiedNode);
      this.setTune(z, modifiedNode);
    },
    setTune (z, modifiedNode) {
      this.tune = z;
      this.abcjsEditor.editarea.setString(z);
      this.currentNode.end = this.currentNode.start + modifiedNode.length;
      if (modifiedNode === ' ' || modifiedNode === '') {
        this.abcjsEditor.editarea.setSelection(0, 0);
      } else {
        this.abcjsEditor.editarea.setSelection(this.currentNode.start, this.currentNode.end);
      }
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
