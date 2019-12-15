<template>
  <div>
    <p>Some text</p>
    <textarea id="abc-source" ref="tuneInput" v-model="tune.text" />
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
    <button @click="addNote">
      Add note
    </button>
    <div id="note-length-container">
      <button @click="changeNoteLength(1)">
        1
      </button>
      <button @click="changeNoteLength(2)">
        2
      </button>
      <button @click="changeNoteLength(4)">
        4
      </button>
      <button @click="changeNoteLength(8)">
        8
      </button>
      <button @click="changeNoteLength(16)">
        16
      </button>
      <button @click="changeNoteLength(32)">
        32
      </button>
    </div>
    <div id="note-modifiers-container">
      <button :class="{ active: currentNode.dotCount === 1 }" @click="setDots(1)">
        &#9210;
      </button>
      <button :class="{ active: currentNode.dotCount === 2 }" @click="setDots(2)">
        &#9210;&#9210;
      </button>
    </div>
    <div id="paper" />
  </div>
</template>

<script>
import 'abcjs/abcjs-midi.css';
import abcjs from 'abcjs/midi';
import { getNoteMetadata, MODIFY_OPERATIONS, modifyNote, replaceSubstring } from './NodeModifier';
import { extractMetadata } from './TuneService';
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
      tune: {
        text: 'X:1\nT: Cooley\'s\nM: 4/4\nL: 1/8\nR: reel\nK: Emin\nD2DD||',
        meta: {
          defaultNoteLength: null
        }
      },
      currentNode: {
        id: null,
        start: null,
        end: null,
        text: null,
        dotCount: null
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
        this.currentAbcFragment = this.tune.text.substring(currentRange.startChar, currentRange.endChar);
      } else {
        this.currentAbcFragment = '(none)';
      }
    },
    async loadData () {
      const data = await this.$axios.$get('/songs/MRegWOs_T0006A_01.abc');
      console.log(data);
      this.tune.text = data.trim();
      this.tune.meta = extractMetadata(this.tune.text);
    },
    onNodeClick (abcElem, tuneNumber, classes) {
      console.log(abcElem);
      this.currentNode.start = abcElem.startChar;
      this.currentNode.end = abcElem.endChar;
      this.currentNode.id = abcElem.__ob__.id;
      this.reloadNote();
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
    addNote () {
      let selectionLeft, selectionRight;
      if (this.currentNode.start == null) {
        this.tune.text += ' A';
        selectionLeft = this.tune.text.length - 3;
        selectionRight = this.tune.text.length - 1;
      } else {
        const left = this.tune.text.slice(0, this.currentNode.end).trim();
        const right = this.tune.text.slice(this.currentNode.end).trim();
        selectionLeft = left.length + 1;
        selectionRight = selectionLeft + 2;
        this.tune.text = left + ' A ' + right;
      }
      this.redrawTune();
      this.showSelection(selectionLeft, selectionRight);
    },
    changeNoteLength (length) {
      this.modifyNoteOperation(MODIFY_OPERATIONS.CHANGE_LENGTH, {
        defaultNoteLength: this.tune.meta.defaultNoteLength,
        targetLength: length
      });
    },
    setDots (dotsCount) {
      if (this.currentNode.dotCount === dotsCount) {
        this.modifyNoteOperation(MODIFY_OPERATIONS.DOT, { dotCount: 0 });
      } else {
        this.modifyNoteOperation(MODIFY_OPERATIONS.DOT, { dotCount: dotsCount });
      }
    },
    modifyNoteOperation (operation, opts) {
      if (this.currentNode.start === null) {
        return;
      }
      const nodeStr = this.currentNode.text;
      let modifiedNode = modifyNote(operation, nodeStr, opts);
      console.log(modifiedNode);
      if (nodeStr[nodeStr.length - 1] === ' ' && modifiedNode[modifiedNode.length - 1] !== ' ') {
        modifiedNode += ' ';
      }
      const z = replaceSubstring(this.tune.text, this.currentNode.start, this.currentNode.end + 1, modifiedNode);
      this.setTune(z, modifiedNode);
    },
    setTune (z, modifiedNode) {
      this.tune.text = z;
      this.redrawTune();
      this.currentNode.end = this.currentNode.start + modifiedNode.length;
      if (modifiedNode === ' ' || modifiedNode === '') {
        this.showSelection(0, 0);
        this.resetNoteProperties();
      } else {
        this.showSelection(this.currentNode.start, this.currentNode.end);
      }
    },
    redrawTune () {
      this.abcjsEditor.editarea.setString(this.tune.text);
    },
    showSelection (from, to) {
      if (this.abcjsEditor.editarea != null) {
        this.currentNode.start = from;
        this.currentNode.end = to;
        this.reloadNote();
        this.abcjsEditor.editarea.setSelection(this.currentNode.start, this.currentNode.end);
      }
    },
    resetNoteProperties () {
      this.currentNode.id = null;
      this.currentNode.start = null;
      this.currentNode.end = null;
      this.currentNode.text = null;
      this.currentNode.dotCount = null;
    },
    reloadNote () {
      this.currentNode.text = this.tune.text.substr(
        this.currentNode.start, (this.currentNode.end - this.currentNode.start)
      );
      this.currentNode.dotCount = getNoteMetadata(this.currentNode.text).dotsCount;
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

  #note-length-container {
    background-color: antiquewhite;
    color: red;
  }
  #note-modifiers-container {
    background-color: #abb0fa;
    color: #695fff;
    font-size: large;
    .active {
      color: #0942ff;
    }
  }
</style>
