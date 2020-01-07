// http://abcnotation.com/examples  --all operations on abc music notation
export const MODIFY_OPERATIONS = {
  UP: 'up',
  DOWN: 'down',
  REMOVE: 'remove',
  CHANGE_LENGTH: 'change_length',
  DOT: 'dot',
  SHARP: 'sharp',
  FLAT: 'flat',
  NATURAL: 'natural'
};

const notes = ['C', 'D', 'E', 'F', 'G', 'A', 'B', 'c', 'd', 'e', 'f', 'g', 'a', 'b'];

export function modifyNote (operation, note, opts) {
  switch (operation.toLowerCase()) {
    case MODIFY_OPERATIONS.UP:
      return moveUp(note);
    case MODIFY_OPERATIONS.DOWN:
      return moveDown(note);
    case MODIFY_OPERATIONS.REMOVE:
      return '';
    case MODIFY_OPERATIONS.CHANGE_LENGTH:
      if (opts.defaultNoteLength && opts.targetLength) {
        return changeLength(note, opts.defaultNoteLength, opts.targetLength);
      }
      throw new Error('Cannot change note length without passing opts.defaultNoteLength or opts.targetLength');
    case MODIFY_OPERATIONS.DOT:
      if (opts.dotCount === undefined || opts.dotCount === null || opts.dotCount > 2) {
        return note;
      }
      return setDotCount(note, opts.dotCount);
    case MODIFY_OPERATIONS.SHARP:
      if (opts.sharpCount === undefined || opts.sharpCount === null || opts.sharpCount > 2) {
        return note;
      }
      return setSharpCount(note, opts.sharpCount);
    case MODIFY_OPERATIONS.FLAT:
      if (opts.flatCount === undefined || opts.flatCount === null || opts.flatCount > 2) {
        return note;
      }
      return setFlatCount(note, opts.flatCount);
    case MODIFY_OPERATIONS.NATURAL:
      if (opts.isNatural === undefined || opts.isNatural === null || opts.isNatural >= 2) {
        return note;
      }
      return setNatural(note, opts.isNatural);
    default:
      return note;
  }
}

export function replaceSubstring (original, start, end, replacement) {
  return original.substr(0, start) + replacement + original.substr(end - 1, original.length)
}

export function getNoteMetadata (note, opts) {
  const noteObj = new Note(note);
  return {
    dotsCount: noteObj.getDotsCount(),
    sharpCount: noteObj.getSharpCount(),
    flatCount: noteObj.getFlatCount(),
    natural: noteObj.isNatural()
  }
}

class Note {
  constructor (note) {
    const slittedNote = this.split(note);
    const tonePart = slittedNote[0];
    this.nodeLength = (slittedNote[1] === undefined) ? '1' : slittedNote[1];
    this.toneName = this.getToneName(tonePart);
    this.tonePrefix = this.getTonePrefix(tonePart);
    this.toneMove = this.getToneMove(tonePart);
    this.isPause = !!note.includes('z');
  }

  moveDown () {
    const toneIdx = notes.indexOf(this.toneName);
    if (toneIdx === -1) {
      console.log(`Cannot find tone for ${this.toneName}\nAvailable tones: ${notes}`);
      return;
    }
    if (toneIdx === 0) {
      if (this.toneMove.includes('\'')) {
        this.toneMove = this.toneMove.replace('\'', '');
      } else {
        this.toneMove += ',';
      }
      this.toneName = 'B';
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
      if (this.toneMove.includes(',')) {
        this.toneMove = this.toneMove.replace(',', '');
      } else {
        this.toneMove += '\'';
      }
      this.toneName = 'c';
    } else {
      this.toneName = notes[toneIdx + 1];
    }
  }

  /**
   * Changes note length, basing on default note length defined in abc text.
   * Examples:
   * let note = new Note(C2);
   *
   *    note.changeLength(2, 16);
   *    note.toString() -> C8
   *
   *    note.changeLength(4, 16);
   *    note.toString() -> C4
   * @param targetLength
   * @param defaultNoteLength
   */
  changeLength (targetLength, defaultNoteLength) {
    const dotCount = this.getDotsCount();
    if (Number.isInteger(defaultNoteLength / targetLength) && dotCount === 0) {
      this.nodeLength = (defaultNoteLength / targetLength).toString(); // case when shortening the fraction is avaiable
    } else {
      this.nodeLength = `${defaultNoteLength}/${targetLength}`;
      this.setDots(dotCount);
    }
  }

  setDots (dotsCount) {
    const noteFractions = this.getFractionNoteLength(this.nodeLength);
    let left = 0.5;
    while (noteFractions.left >= left) {
      left *= 2;
    }
    left /= 2;
    noteFractions.left = left;
    switch (dotsCount) {
      case 1:
        noteFractions.left = noteFractions.left * 2 + 1;
        noteFractions.right = noteFractions.right * 2;
        break;
      case 2:
        noteFractions.left = noteFractions.left * 3 + 1;
        noteFractions.right = noteFractions.right * 2;
        break;
    }
    this.nodeLength = `${noteFractions.left}/${noteFractions.right}`;
  }

  setSharps (sharpCount) {
    this.tonePrefix = '^'.repeat(sharpCount);
  }

  setFlats (flatCount) {
    this.tonePrefix = '_'.repeat(flatCount);
  }

  setNeutral (isNatural) {
    this.tonePrefix = '='.repeat(isNatural);
  }

  toString () {
    return this.tonePrefix + this.toneName + this.toneMove + this.nodeLength;
  }

  /**
   * Split note tone from note length.
   * Examples:
   * split('C2') -> ['C', '2']
   * split('C3/2') -> ['C', '3/2']
   * split('~C,,3/2') -> ['~C,,', '3/2']
   * @param note note to split
   * @returns {x} array of splitted note tone and length
   */
  split (note) {
    return note.split(/^(\D*)(.*)$/).filter(Boolean);
  }

  getToneName (tone) {
    const toneName = tone.match(/[a-zA-Z]+/g);
    if (toneName !== null) {
      return toneName[0];
    }
    return '';
  }

  getTonePrefix (tone) {
    const toneName = tone.match(/^([^a-zA-Z ]*)+/g);
    if (toneName !== null) {
      return toneName[0];
    }
    return '';
  }

  getToneMove (tone) {
    const toneMove = tone.match(/[^a-zA-Z _^=]+/g);
    if (toneMove !== null) {
      return toneMove[0];
    }
    return '';
  }

  getNumericNoteLength (noteLength) {
    if (noteLength.includes('/')) {
      const split = noteLength.split('/');
      return split[0] / split[1];
    } else {
      return parseFloat(noteLength);
    }
  }

  getFractionNoteLength (noteLength) {
    if (noteLength.includes('/')) {
      const split = noteLength.split('/');
      return {
        left: parseInt(split[0]),
        right: parseInt(split[1])
      };
    } else {
      return {
        left: parseInt(noteLength),
        right: 1
      };
    }
  }

  getDotsCount () {
    let left = 1 / 32;
    let right = 2 / 32;
    while (this.getNumericNoteLength(this.nodeLength) > right) {
      left *= 2;
      right *= 2;
    }
    const originalFraction = this.getFractionNoteLength(this.nodeLength);
    const nominator = originalFraction.left;
    const denominator = originalFraction.right;
    if (right === this.getNumericNoteLength(this.nodeLength)) {
      return 0;
    } else if (nominator / denominator <= (left + right) / 2) {
      return 1;
    } else {
      return 2;
    }
  }

  getFlatCount () {
    return (this.tonePrefix.match(/_/g) || []).length;
  }

  getSharpCount () {
    return (this.tonePrefix.match(/\^/g) || []).length;
  }

  isNatural () {
    return this.tonePrefix.includes('=');
  }
}

function moveDown (note) {
  const noteObj = new Note(note);
  if (noteObj.isPause) {
    return note;
  }
  noteObj.moveDown();
  return noteObj.toString();
}

function moveUp (note) {
  const noteObj = new Note(note);
  if (noteObj.isPause) {
    return note;
  }
  noteObj.moveUp();
  return noteObj.toString();
}

function changeLength (note, defaultNoteLength, targetLength) {
  const noteObj = new Note(note);
  noteObj.changeLength(targetLength, defaultNoteLength);
  return noteObj.toString();
}

function setDotCount (note, dotCount) {
  const noteObj = new Note(note);
  noteObj.setDots(dotCount);
  return noteObj.toString();
}

function setSharpCount (note, sharpCount) {
  const noteObj = new Note(note);
  noteObj.setSharps(sharpCount);
  return noteObj.toString();
}

function setFlatCount (note, flatCount) {
  const noteObj = new Note(note);
  noteObj.setFlats(flatCount);
  return noteObj.toString();
}

function setNatural (note, isNatural) {
  const noteObj = new Note(note);
  noteObj.setNeutral(isNatural);
  return noteObj.toString();
}

function nearlyEquals (a, b, epsilon) {
  return (a - b) < epsilon;
}
