export const MODIFY_OPERATIONS = {
  UP: 'up',
  DOWN: 'down'
};

export function modifyNote (direction, note) {
  switch (direction.toLowerCase()) {
    case MODIFY_OPERATIONS.UP:
      return moveUp(note);
    case MODIFY_OPERATIONS.DOWN:
      return moveDown(note);
    default:
      return note;
  }
}

export function replaceSubstring (original, start, end, replacement) {
  return original.substr(0, start) + replacement + original.substr(end - 1, original.length)
}

class Note {
  constructor (note) {
    if (note.includes('z')) {
      return note;
    }
    const slittedNote = split(note);
    const tonePart = slittedNote[0];
    this.nodeLength = slittedNote[1];
    this.toneName = getToneName(tonePart);
    this.toneMove = getToneMove(tonePart);
  }

  moveDown () {
    const toneIdx = notes.indexOf(this.toneName);
    if (toneIdx === -1) {
      console.log(`Cannot find tone for ${this.toneName}\nAvailable tones: ${notes}`);
      return;
    }
    if (toneIdx === 0) {
      this.toneName = 'B';
      this.toneMove += ',';
    } else {
      this.toneName = notes[toneIdx - 1];
    }
  }

  moveUp () {
    const toneIdx = notes.indexOf(this.toneName);
    if (toneIdx === -1) {
      console.log(`Cannot find tone for ${this.toneName}\nAvailable tones: ${notes}`);
      return;
    }
    if (toneIdx === notes.length - 1) {
      this.toneName = 'c';
      this.toneMove += '\'';
    } else {
      this.toneName = notes[toneIdx + 1];
    }
  }

  toString () {
    return this.toneName + this.toneMove + this.nodeLength;
  }
}

function moveDown (note) {
  const noteObj = new Note(note);
  noteObj.moveDown();
  return noteObj.toString();
}

function moveUp (note) {
  const noteObj = new Note(note);
  noteObj.moveUp();
  return noteObj.toString();
}

const notes = ['C', 'D', 'E', 'F', 'G', 'A', 'B', 'c', 'd', 'e', 'f', 'g', 'a', 'b'];

function split (note) {
  return note.split(/(\d+)/).filter(Boolean);
}

function getToneName (tone) {
  const toneName = tone.match(/[a-zA-Z]+/g);
  if (toneName !== null) {
    return toneName[0];
  }
  return '';
}

function getToneMove (tone) {
  const toneMove = tone.match(/[^a-zA-Z]+/g);
  if (toneMove !== null) {
    return toneMove[0];
  }
  return '';
}
