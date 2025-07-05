import { describe, expect, it } from 'vitest';

import { trimIndent } from './trimIndent';

describe('trimIndent', () => {
  it('removes uniform indent and trims blank lines', () => {
    const input = `
      line1
        line2
      line3
    `;
    const expected = `line1
  line2
line3`;
    expect(trimIndent(input)).toBe(expected);
  });

  it('handles no indent correctly', () => {
    const input = `line1
   line2
line3`;
    const expected = `line1
   line2
line3`;
    expect(trimIndent(input)).toBe(expected);
  });

  it('returns empty string for blank input', () => {
    expect(trimIndent('')).toBe('');
  });

  it('returns empty string for strings with only blank lines', () => {
    expect(trimIndent('\n\n')).toBe('');
  });

  it('handles tabs by treating them as two spaces', () => {
    const input = `
		foo
		bar
`;
    const expected = `foo
bar`;
    expect(trimIndent(input)).toBe(expected);
  });
});
