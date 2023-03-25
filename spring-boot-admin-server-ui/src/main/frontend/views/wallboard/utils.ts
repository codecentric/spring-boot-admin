const tileCount = (cols, rows) => {
  const shorterRows = Math.floor(rows / 2);
  return rows * cols - shorterRows;
};

const calcSideLength = (width, height, cols, rows) => {
  const fitToWidth = width / cols / Math.sqrt(3);
  const fitToHeight = (height * 2) / (3 * rows + 1);
  return Math.min(fitToWidth, fitToHeight);
};

export const calcLayout = (minTileCount, width, height) => {
  let cols = 1,
    rows = 1;
  let sideLength = calcSideLength(width, height, cols, rows);

  while (minTileCount > tileCount(cols, rows)) {
    const sidelengthExtraCol = calcSideLength(width, height, cols + 1, rows);
    const sidelengthExtraRow = calcSideLength(width, height, cols, rows + 1);
    if (sidelengthExtraCol > sidelengthExtraRow) {
      sideLength = sidelengthExtraCol;
      cols++;
    } else {
      sideLength = sidelengthExtraRow;
      rows++;
    }
  }
  return {
    cols,
    rows,
    sideLength,
  };
};
