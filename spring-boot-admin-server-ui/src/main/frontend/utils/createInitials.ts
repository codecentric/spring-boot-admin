export function createInitials(input: string, maxLength: number = 3): string {
  const trimmed = input.trim();
  if (!trimmed) return '';

  const words = trimmed
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .split(/[\s\-_]+/)
    .filter((word) => word.length > 0);

  return words
    .slice(0, maxLength) // Limit to maxLength words
    .map((word) => word[0].toUpperCase())
    .join('');
}
