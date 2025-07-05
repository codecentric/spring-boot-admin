export function trimIndent(str: string): string {
  // Replace tabs with two spaces for consistent indent measurement
  const lines = str.replace(/\t/g, '  ').split('\n');

  // Remove leading blank lines
  while (lines.length > 0 && lines[0].trim() === '') {
    lines.shift();
  }
  // Remove trailing blank lines
  while (lines.length > 0 && lines[lines.length - 1].trim() === '') {
    lines.pop();
  }

  // Determine minimum indent from non-blank lines
  const indents = lines
    .filter((line) => line.trim().length > 0)
    .map((line) => {
      const match = line.match(/^ */);
      return match ? match[0].length : 0;
    });
  const minIndent = indents.length > 0 ? Math.min(...indents) : 0;

  // Remove the common indent and reconstruct
  return lines.map((line) => line.slice(minIndent)).join('\n');
}
