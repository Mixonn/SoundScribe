<template>
  <div class="app">
    <div id="buttons-container">
      <img alt="Select" class="controlButtons" src="/buttons/select.png">
      <img alt="Undo" class="controlButtons" src="/buttons/undo.png">
      <img alt="Redo" class="controlButtons" src="/buttons/redo.png">
      <img alt="Elevate" class="controlButtons" src="/buttons/elevate.png" @click="noteUp">
      <img alt="Lower" class="controlButtons" src="/buttons/lower.png" @click="noteDown">
      <img alt="Add" class="controlButtons" src="/buttons/plus.png" @click="addNote('A')">
      <img alt="Remove" class="controlButtons" src="/buttons/minus.png" @click="removeNote">
      <img alt="Update" class="controlButtons" src="/buttons/update.png" @click="uploadAbc">
    </div>

    <div id="note-length-container">
      <img class="controlButtons" :src="require('@/static/buttons/notes/1.png')" @click="changeNoteLength(1)">
      <img class="controlButtons" :src="require('@/static/buttons/notes/2.png')" @click="changeNoteLength(2)">
      <img class="controlButtons" :src="require('@/static/buttons/notes/4.png')" @click="changeNoteLength(4)">
      <img class="controlButtons" :src="require('@/static/buttons/notes/8.png')" @click="changeNoteLength(8)">
      <img class="controlButtons" :src="require('@/static/buttons/notes/16.png')" @click="changeNoteLength(16)">
      <img class="controlButtons" :src="require('@/static/buttons/notes/32.png')" @click="changeNoteLength(32)">
      <img
        class="controlButtons"
        :class="{ active: currentNode.dotCount === 1 }"
        :src="require('@/static/buttons/notes/oneDot.png')"
        @click="setDots(1)"
      >
      <img
        class="controlButtons"
        :class="{ active: currentNode.dotCount === 2 }"
        :src="require('@/static/buttons/notes/twoDot.png')"
        @click="setDots(2)"
      >
      <img
        class="controlButtons"
        :src="require('@/static/buttons/notes/pause.png')"
        @click="addNote('z')"
      >
      <img
        class="controlButtons"
        :class="{ active: currentNode.flatCount === 1 }"
        :src="require('@/static/buttons/notes/flat.png')"
        @click="setFlats(1)"
      >
      <img
        class="controlButtons"
        :class="{ active: currentNode.flatCount === 2 }"
        :src="require('@/static/buttons/notes/doubleFlat.png')"
        @click="setFlats(2)"
      >
      <img
        class="controlButtons"
        :class="{ active: currentNode.sharpCount === 1 }"
        :src="require('@/static/buttons/notes/sharp.png')"
        @click="setSharps(1)"
      >
      <img
        class="controlButtons"
        :class="{ active: currentNode.sharpCount === 2 }"
        :src="require('@/static/buttons/notes/doubleSharp.png')"
        @click="setSharps(2)"
      >
      <img
        class="controlButtons"
        :class="{ active: currentNode.natural === true }"
        :src="require('@/static/buttons/notes/natural.png')"
        @click="setNatural(!currentNode.natural)"
      >
      <img
        class="controlButtons"
        :src="require('@/static/buttons/notes/barLine.png')"
        @click="addNote('|')"
      >
      <img
        class="controlButtons"
        :src="require('@/static/buttons/notes/doubleBarLine.png')"
        @click="addNote(':|')"
      >
      <img
        class="controlButtons"
        :src="require('@/static/buttons/notes/newline.png')"
        @click="lineBreak(true)"
      >
      <img
        class="controlButtons"
        :src="require('@/static/buttons/notes/removeLine.png')"
        @click="lineBreak(false)"
      >
    </div>
    <div id="metadata-container">
      <div id="metrum-container">
        <p v-if=" tune.error.metre != null " class="input-error">
          {{ tune.error.metre }}
        </p>
        <v-text-field
          :value="tune.meta.metre"
          label="Metrum"
          @input="handleMetreInput"
        />
      </div>
      <div id="bpm">
        <v-slider
          v-model="tune.meta.bpm"
          class="align-center bpm-slider"
          label="BPM"
          :max="360"
          :min="20"
          hide-details
          @change="updateBpm(tune.meta.bpm)"
        >
          <template v-slot:append>
            <v-text-field
              v-model="tune.meta.bpm"
              class="mt-0 pt-0"
              hide-details
              single-line
              type="number"
              style="width: 60px"
            />
          </template>
        </v-slider>
      </div>
    </div>
    <v-container
      id="scroll-target"
      style="max-height: 400px"
      class="overflow-y-auto"
    >
      <div id="paper" />
    </v-container>
    <textarea id="abc-source" ref="tuneInput" v-model="tune.text" style="display: none" />
    <!--    <div class="listener-output">-->
    <!--      <div class="label">-->
    <!--        Currently Playing: <span class="abc-string">{{ currentAbcFragment }}</span>-->
    <!--      </div>-->

    <!--      <div class="label">-->
    <!--        Parameters sent to listener callback:-->
    <!--      </div>-->
    <!--      <div>Progress: {{ progress.progress }}</div>-->
    <!--      <div>Current Time: {{ progress.currentTime }}</div>-->
    <!--      <div>Total Duration: {{ progress.duration }}</div>-->
    <!--      <div>New Beat? {{ progress.newBeat }}</div>-->
    <!--    </div>-->
    <div id="midi" />
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
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script>
import 'abcjs/abcjs-midi.css';
import abcjs from 'abcjs/midi';
import { getNoteMetadata, MODIFY_OPERATIONS, modifyNote, replaceSubstring } from './NodeModifier';
import { extractMetadata, setBpm, setMetre } from './TuneService';
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
      snackbar: {
        text: '',
        display: false,
        timeout: 5000,
        success: true
      },
      tune: {
        text: 'X:1\nT: Cooley\'s\nM: 4/4\nL: 1/8\nR: reel\nK: Emin\nD2DD||',
        meta: {
          defaultNoteLength: null,
          metre: '4/4'
        },
        error: {
          metre: null
        }
      },
      currentNode: {
        id: null,
        start: null,
        end: null,
        text: null,
        dotCount: null,
        sharpCount: null,
        flatCount: null,
        natural: null
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
      const abcUrl = 'download/' + this.$route.params.song;
      const data = await this.$axios.$get(abcUrl);
      this.tune.text = data.trim();
      this.tune.meta = extractMetadata(this.tune.text);
    },
    async uploadAbc () {
      if (this.tune.text == null || this.tune.text === '') {
        return;
      }
      await this.$axios.$put(`convert/update-file-abc-raw?fileName=${this.$route.params.song})`,
        this.tune.text,
        { headers: { 'Content-Type': 'text/plain' } })
        .then((r) => {
          this.snackbar.text = 'Plik został zaaktualizowany';
          this.snackbar.success = true;
          this.snackbar.display = true;
        }).catch((r) => {
          this.snackbar.text = 'Nie udało się zaaktualizować pliku';
          this.snackbar.success = false;
          this.snackbar.display = true;
        });
    },
    onNodeClick (abcElem, tuneNumber, classes) {
      console.log(abcElem);
      this.currentNode.start = abcElem.startChar;
      this.currentNode.end = abcElem.endChar;
      this.currentNode.id = abcElem.__ob__.id;
      this.reloadNote();
    },
    noteUp () {
      this.modifyNoteOperation(MODIFY_OPERATIONS.UP);
    },
    noteDown () {
      this.modifyNoteOperation(MODIFY_OPERATIONS.DOWN);
    },
    removeNote () {
      this.modifyNoteOperation(MODIFY_OPERATIONS.REMOVE)
    },
    addNote (noteText) {
      let selectionLeft, selectionRight;
      if (this.noNoteSelected() || this.currentNode.start === 0) {
        selectionLeft = this.tune.text.length + 1;
        this.tune.text += ' ' + noteText;
        selectionRight = this.tune.text.length;
      } else {
        const left = this.tune.text.slice(0, this.currentNode.end);
        const right = this.tune.text.slice(this.currentNode.end);
        selectionLeft = left.length + 1;
        selectionRight = selectionLeft + noteText.length;
        this.tune.text = `${left} ${noteText} ${right}`;
      }
      this.redrawTune();
      this.showSelection(selectionLeft, selectionRight);
    },
    lineBreak (shouldAdd) {
      if (this.noNoteSelected() || this.currentNode.start === 0) {
        return;
      }
      if (shouldAdd) {
        const before = this.tune.text.substring(0, this.currentNode.end);
        this.tune.text = `${before} \n ${this.tune.text.substring(this.currentNode.end, this.tune.text.length)}`;
      } else {
        let toRemoveFirstEol = this.tune.text.substring(this.currentNode.end, this.tune.text.length);
        toRemoveFirstEol = toRemoveFirstEol.replace('\n', '');
        this.tune.text = this.tune.text.substring(0, this.currentNode.end) + toRemoveFirstEol;
      }
      this.redrawTune();
      this.showSelection();
    },
    changeNoteLength (length) {
      this.modifyNoteOperation(MODIFY_OPERATIONS.CHANGE_LENGTH, {
        defaultNoteLength: this.tune.meta.defaultNoteLength,
        targetLength: length
      });
    },
    setDots (dotsCount) {
      if (this.noNoteSelected()) {
        return;
      }
      if (this.currentNode.dotCount === dotsCount) {
        this.modifyNoteOperation(MODIFY_OPERATIONS.DOT, { dotCount: 0 });
      } else {
        this.modifyNoteOperation(MODIFY_OPERATIONS.DOT, { dotCount: dotsCount });
      }
    },
    setSharps (sharpCount) {
      if (this.noNoteSelected()) {
        return;
      }
      if (this.currentNode.sharpCount === sharpCount) {
        this.modifyNoteOperation(MODIFY_OPERATIONS.SHARP, { sharpCount: 0 });
      } else {
        this.modifyNoteOperation(MODIFY_OPERATIONS.SHARP, { sharpCount });
      }
    },
    setFlats (flatCount) {
      if (this.noNoteSelected()) {
        return;
      }
      if (this.currentNode.flatCount === flatCount) {
        this.modifyNoteOperation(MODIFY_OPERATIONS.FLAT, { flatCount: 0 });
      } else {
        this.modifyNoteOperation(MODIFY_OPERATIONS.FLAT, { flatCount });
      }
    },
    setNatural (isNatural) {
      if (this.noNoteSelected()) {
        return;
      }
      if (this.currentNode.natural === false) {
        this.modifyNoteOperation(MODIFY_OPERATIONS.NATURAL, { isNatural });
      } else {
        this.modifyNoteOperation(MODIFY_OPERATIONS.NATURAL, { isNatural: false });
      }
    },
    modifyNoteOperation (operation, opts) {
      if (this.noNoteSelected()) {
        return;
      }
      const nodeStr = this.currentNode.text;
      let modifiedNode = modifyNote(operation, nodeStr, opts);
      console.log(modifiedNode);
      if (nodeStr[nodeStr.length - 1] === ' ' && modifiedNode[modifiedNode.length - 1] !== ' ') {
        modifiedNode += ' ';
      }
      const z = replaceSubstring(this.tune.text, this.currentNode.start, this.currentNode.end + 1, modifiedNode);
      this.setTuneWithModifiedNote(z, modifiedNode);
    },
    setTuneWithModifiedNote (z, modifiedNode) {
      this.tune.text = z;
      this.redrawTune();
      this.currentNode.end = this.currentNode.start + modifiedNode.length;
      if (modifiedNode === ' ' || modifiedNode === '') {
        this.unselectNote();
      } else {
        this.showSelection(this.currentNode.start, this.currentNode.end);
      }
    },
    redrawTune () {
      this.abcjsEditor.editarea.setString(this.tune.text);
    },
    showSelection (from, to) {
      if (from === undefined && to === undefined) {
        from = this.currentNode.start;
        to = this.currentNode.end;
      }
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
      this.currentNode.sharpCount = null;
      this.currentNode.flatCount = null;
      this.currentNode.natural = null;
    },
    reloadNote () {
      this.currentNode.text = this.tune.text.substr(
        this.currentNode.start, (this.currentNode.end - this.currentNode.start)
      );
      if (this.currentNode.text === '' || this.currentNode.text === ' ') {
        return;
      }
      const noteMetadata = getNoteMetadata(this.currentNode.text);
      this.currentNode.dotCount = noteMetadata.dotsCount;
      this.currentNode.sharpCount = noteMetadata.sharpCount;
      this.currentNode.flatCount = noteMetadata.flatCount;
      this.currentNode.natural = noteMetadata.natural;
    },
    updateBpm (bpm) {
      this.tune.text = setBpm(this.tune.text, bpm);
      this.unselectNote();
      this.redrawTune();
      // this.abcjsEditor.pauseMidi(false);
      // this.abcjsEditor.redrawMidi();
    },
    handleMetreInput (input) {
      if (input.match(/^\d+\/\d+$/g) != null) {
        this.tune.meta.metre = input;
        this.tune.error.metre = null;
        this.tune.text = setMetre(this.tune.text, input);
        this.unselectNote();
        this.redrawTune();
      } else {
        this.tune.error.metre = 'Nieprawidłowa wartość metrum, przykładowe poprawne: 4/4, 3/2, 15/8'
      }
    },
    noNoteSelected () {
      return this.currentNode.start == null || this.currentNode.start === 0;
    },
    unselectNote () {
      this.showSelection(0, 0);
      this.resetNoteProperties();
    }
  }
}
</script>

<style scoped lang="scss">
  .app {
    color: black;
  }

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
  #metadata-container {
    display: flex;
    align-items: center;
    #metrum-container {
      width: 100px;
      padding: 20px;
    }
  }

  #midi {
    width: 756px;
  }

  .label {
    font-weight: bold;
  }

  .input-error {
    color: red;
  }

  .controlButtons {
    width: 50px;
    height: 50px;
    &:hover,
    &:focus {
      box-shadow: inset 0 0 0 2em rgba(0, 0, 0, .4);
      transition: box-shadow .1s;
      -webkit-transition: box-shadow .1s;
    }
  }

  .active {
    background-color: #ff9901;
    opacity: .8;
    border-radius: 20px;
    transition: background-color .5s;
    -webkit-transition: background-color .5s;
  }

  #buttons-container {
    background-color: #f7f7f7;
  }
  #note-length-container {
    background-color: #f7f7f7;
    color: red;
  }
  #note-modifiers-container {
    background-color: #f7f7f7;
    color: #695fff;
    font-size: large;
    .active {
      color: #0942ff;
    }
  }
  #note-operations {
    button {
      border: 1px solid #949297;
    }
  }

  .bpm-slider {
    width: 300px !important;
  }
</style>
