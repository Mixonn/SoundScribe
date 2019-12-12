export function extractMetadata (tune) {
  const linesWithNoteLength = tune.match(/[^\r\n]+/g).filter(ifLineContainsNoteLength);
  if (linesWithNoteLength.length !== 0) {
    let line = linesWithNoteLength[0];
    if (line.includes('/')) {
      line = line.substring(2, line.length).trim();
      const splitted = line.match(/[^/]+/g);
      return splitted[1] / splitted[0];
    }
  }
}

function ifLineContainsNoteLength (line) {
  return line.includes('L:');
}
