export function modifyNote (direction, note) {
  switch (direction.toLowerCase()) {
    case 'up':
      return moveUp(note);
    case 'down':
      return moveDown(note);
    default:
      return note;
  }
}

export function replaceSubstring (original, start, end, replacement) {
  return original.substr(0, start) + replacement + original.substr(end - 1, original.length)
}

function moveDown (note) {
  // console.log(note);
  // const splittedNote = split(note);
  // console.log(splittedNote);
  return 'C4';
}

function moveUp (note) {
  return 'C4';
}

const notes = ['C', 'D', 'E', 'F', 'G', 'A', 'B', 'c', 'd', 'e', 'f', 'g', 'a', 'b'];

function split (note) {
  const output = [];
  output.push(note.split(/(\d+)/).filter(Boolean));
  return output;
}
