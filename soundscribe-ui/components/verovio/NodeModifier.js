export function moveNode (direction, node) {
  switch (direction.toLowerCase()) {
    case 'up':
      return moveUp(node);
    case 'down':
      return moveDown(node);
    default:
      return node;
  }
}

export function replaceSubstring (original, start, end, replacement) {
  return original.substr(0, start) + replacement + original.substr(end - 1, original.length)
}

function moveDown (node) {
  return node;
}

function moveUp (node) {
  return node;
}
