export function extractMetadata (tune) {
  const linesWithNoteLength = tune.match(/[^\r\n]+/g).filter(ifLineContainsNoteLength);
  const result = {};
  if (linesWithNoteLength.length !== 0) {
    let line = linesWithNoteLength[0];
    if (line.includes('/')) {
      line = line.substring(2, line.length).trim();
      const splitted = line.match(/[^/]+/g);
      result.defaultNoteLength = splitted[1] / splitted[0];
    }
  }
  const linesWithBpm = tune.match(/[^\r\n]+/g).filter(ifLineContainsTempo);
  if (linesWithBpm.length !== 0) {
    let line = linesWithBpm[0];
    if (line.includes('=')) {
      line = line.split('=')[1];
      result.bpm = parseInt(line);
    }
  }
  const linesWithMetre = tune.match(/[^\r\n]+/g).filter(ifLineContainsMetre);
  if (linesWithMetre.length !== 0) {
    let line = linesWithMetre[0];
    line = line.split(':')[1];
    result.metre = line;
  }
  return result;
}

export function setBpm (tune, bpm) {
  const linesWithBpm = tune.match(/[^\r\n]+/g).filter(ifLineContainsTempo);
  if (linesWithBpm.length === 0) {
    return tune;
  }
  let line = linesWithBpm[0];
  if (line.includes('=')) {
    const metrum = line.split('=')[0];
    line = `${metrum}=${bpm}`;
  } else {
    line = `${line}=${bpm}`;
  }
  return tune.replace(/^Q:.*$/mg, line);
}

export function setMetre (tune, metre) {
  const linesWithMetre = tune.match(/[^\r\n]+/g).filter(ifLineContainsMetre);
  if (linesWithMetre.length === 0) {
    return tune;
  }
  let line = linesWithMetre[0];
  line = line.split(':')[0] + `:${metre}`;
  return tune.replace(/^M:.*$/mg, line);
}

function ifLineContainsNoteLength (line) {
  return line.includes('L:');
}

function ifLineContainsTempo (line) {
  return line.includes('Q:');
}

function ifLineContainsMetre (line) {
  return line.includes('M:');
}
