/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useEffect, useRef, useState, useMemo } from 'react';
import styles from './HexMesh.module.css';

interface HexMeshProps<T> {
  items: T[];
  classForItem?: (item: T | undefined) => string | undefined;
  renderItem?: (item: T) => React.ReactNode;
  onClick?: (item: T, event: React.MouseEvent) => void;
}

interface Layout {
  cols: number;
  rows: number;
  sideLength: number;
}

const tileCount = (cols: number, rows: number): number => {
  const shorterRows = Math.floor(rows / 2);
  return rows * cols - shorterRows;
};

const calcSideLength = (width: number, height: number, cols: number, rows: number): number => {
  const fitToWidth = width / cols / Math.sqrt(3);
  const fitToHeight = (height * 2) / (3 * rows + 1);
  return Math.min(fitToWidth, fitToHeight);
};

const calcLayout = (minTileCount: number, width: number, height: number): Layout => {
  let cols = 1;
  let rows = 1;
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

export function HexMesh<T>({ items, classForItem, renderItem, onClick }: HexMeshProps<T>) {
  const rootRef = useRef<HTMLDivElement>(null);
  const [layout, setLayout] = useState<Layout>({ cols: 1, rows: 1, sideLength: 1 });

  const { cols, rows, sideLength } = layout;

  const hexHeight = useMemo(() => sideLength * 2, [sideLength]);
  const hexWidth = useMemo(() => sideLength * Math.sqrt(3), [sideLength]);
  const meshWidth = useMemo(() => hexWidth * cols, [hexWidth, cols]);
  const meshHeight = useMemo(() => sideLength * (2 + (rows - 1) * 1.5), [sideLength, rows]);

  const point = (i: number): string => {
    const innerSideLength = sideLength * 0.95;
    const marginTop = hexHeight / 2;
    const marginLeft = hexWidth / 2;
    const x = marginLeft + innerSideLength * Math.cos(((1 + i * 2) * Math.PI) / 6);
    const y = marginTop + innerSideLength * Math.sin(((1 + i * 2) * Math.PI) / 6);
    return `${x},${y}`;
  };

  const hexPath = useMemo(() => {
    const points = [point(0), point(1), point(2), point(3), point(4), point(5)];

    // Radius for the rounded corners
    const cornerRadius = sideLength * 0.05;

    // Parse points into coordinate pairs
    const coords = points.map((p) => {
      const [x, y] = p.split(',').map(Number);
      return { x, y };
    });

    // Helper function to calculate distance between two points
    const distance = (p1: { x: number; y: number }, p2: { x: number; y: number }) =>
      Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));

    // Helper function to move along a line from p1 towards p2 by a given distance
    const moveAlong = (p1: { x: number; y: number }, p2: { x: number; y: number }, dist: number) => {
      const d = distance(p1, p2);
      const ratio = dist / d;
      return {
        x: p1.x + (p2.x - p1.x) * ratio,
        y: p1.y + (p2.y - p1.y) * ratio,
      };
    };

    // Build the path
    let path = '';

    for (let i = 0; i < coords.length; i++) {
      const current = coords[i];
      const prev = coords[(i - 1 + coords.length) % coords.length];
      const next = coords[(i + 1) % coords.length];

      // Point after current corner (moving from current towards next)
      const nextEdgeLength = distance(current, next);
      const afterCorner = moveAlong(current, next, Math.min(cornerRadius, nextEdgeLength / 2));

      // Point before current corner (moving from current towards prev)
      const prevEdgeLength = distance(current, prev);
      const beforeCorner = moveAlong(current, prev, Math.min(cornerRadius, prevEdgeLength / 2));

      if (i === 0) {
        // Start at the point after the first corner
        path += `M ${afterCorner.x},${afterCorner.y} `;
      } else {
        // Draw line to the point before this corner
        path += `L ${beforeCorner.x},${beforeCorner.y} `;
        // Draw quadratic bezier curve around this corner using the corner as control point
        path += `Q ${current.x},${current.y} ${afterCorner.x},${afterCorner.y} `;
      }
    }

    // Close the path (draws line back to start and bezier around first corner)
    const firstCorner = coords[0];
    const lastCorner = coords[coords.length - 1];
    const firstEdgeLength = distance(firstCorner, lastCorner);
    const beforeFirstCorner = moveAlong(firstCorner, lastCorner, Math.min(cornerRadius, firstEdgeLength / 2));
    path += `L ${beforeFirstCorner.x},${beforeFirstCorner.y} `;

    const afterFirstCorner = moveAlong(
      firstCorner,
      coords[1],
      Math.min(cornerRadius, distance(firstCorner, coords[1]) / 2)
    );
    path += `Q ${firstCorner.x},${firstCorner.y} ${afterFirstCorner.x},${afterFirstCorner.y} `;

    path += 'Z';
    return path;
  }, [sideLength, hexHeight, hexWidth]);

  const translate = (col: number, row: number): string => {
    const x = (col - 1) * hexWidth + (row % 2 ? 0 : hexWidth / 2);
    const y = (row - 1) * sideLength * 1.5;
    return `translate(${x},${y})`;
  };

  const getItem = (col: number, row: number): T | undefined => {
    const rowOffset = (row - 1) * cols - Math.max(Math.floor((row - 1) / 2), 0);
    const index = rowOffset + col - 1;
    return items[index];
  };

  const handleClick = (event: React.MouseEvent, col: number, row: number) => {
    const item = getItem(col, row);
    if (item && onClick) {
      onClick(item, event);
    }
  };

  const updateLayout = () => {
    if (rootRef.current) {
      const boundingClientRect = rootRef.current.getBoundingClientRect();
      const newLayout = calcLayout(items.length, boundingClientRect.width, boundingClientRect.height);
      setLayout(newLayout);
    }
  };

  useEffect(() => {
    updateLayout();
  }, [items.length]);

  useEffect(() => {
    if (rootRef.current) {
      rootRef.current.style.fontSize = `${sideLength / 9.5}px`;
    }
  }, [sideLength]);

  useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      updateLayout();
    });

    if (rootRef.current) {
      resizeObserver.observe(rootRef.current);
    }

    return () => {
      resizeObserver.disconnect();
    };
  }, [items.length]);

  const hexes = useMemo(() => {
    const result: React.ReactNode[] = [];
    for (let row = 1; row <= rows; row++) {
      const colCount = cols + (row % 2 ? 0 : -1);
      for (let col = 1; col <= colCount; col++) {
        const item = getItem(col, row);
        const className = classForItem ? classForItem(item) : undefined;

        result.push(
          <g
            key={`${col}-${row}`}
            className={`${styles.hex} ${className || ''} ${item ? styles.hasContent : ''}`}
            transform={translate(col, row)}
            onClick={(e) => handleClick(e, col, row)}
          >
            <path d={hexPath} />
            {item && renderItem && (
              <foreignObject
                height={hexHeight}
                width={hexWidth}
                style={{ pointerEvents: 'none' }}
                x="0"
                y="0"
              >
                {renderItem(item)}
              </foreignObject>
            )}
          </g>
        );
      }
    }
    return result;
  }, [rows, cols, hexPath, hexHeight, hexWidth, items, classForItem, renderItem]);

  return (
    <div ref={rootRef} className={styles.hexMesh}>
      <svg height={meshHeight} width={meshWidth} xmlns="http://www.w3.org/2000/svg">
        <defs>
          <clipPath id="hex-clip">
            <path d={hexPath} />
          </clipPath>
        </defs>
        {hexes}
      </svg>
    </div>
  );
}
