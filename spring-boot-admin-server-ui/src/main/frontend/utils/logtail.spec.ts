import { filter, firstValueFrom, take, toArray } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';

import logtail, {
  StreamType,
  TrimtoCompleteLines,
  fetchLogfileRange,
} from './logtail';

const headers = (contentRange) => ({
  get: (header) =>
    header.toLowerCase() === 'content-range' ? contentRange : null,
});

describe('fetchLogfileRange', () => {
  it('delegates to the instance and returns its result', async () => {
    const result = { data: 'log data' };
    const instance = {
      fetchLogfileRange: vi.fn().mockResolvedValue(result),
    };

    await expect(fetchLogfileRange(instance, 10, 20)).resolves.toBe(result);
    expect(instance.fetchLogfileRange).toHaveBeenCalledWith(10, 20);
  });
});

describe('TrimtoCompleteLines', () => {
  it('trims partial leading and trailing lines', () => {
    expect(TrimtoCompleteLines('abcde\nfghij\nklmnop', 10, 27)).toEqual({
      trimmedCompleteLines: 'fghij\n',
      windowStart: 16,
      windowEnd: 21,
    });
  });

  it('keeps complete trailing lines', () => {
    expect(TrimtoCompleteLines('abcde\nfghij\nklmnop\n', 10, 28)).toEqual({
      trimmedCompleteLines: 'fghij\nklmnop\n',
      windowStart: 16,
      windowEnd: 28,
    });
  });

  it('returns an empty window when no complete line is present', () => {
    expect(TrimtoCompleteLines('abcde', 10, 14)).toEqual({
      trimmedCompleteLines: '',
      windowStart: 15,
      windowEnd: 14,
    });
  });

  it('returns an empty window after trimming a single leading line', () => {
    expect(TrimtoCompleteLines('abcde\n', 10, 15)).toEqual({
      trimmedCompleteLines: '',
      windowStart: 16,
      windowEnd: 15,
    });
  });

  it('keeps the first complete line when the window starts at the file beginning', () => {
    expect(TrimtoCompleteLines('abcde\nfghij', 0, 10)).toEqual({
      trimmedCompleteLines: 'abcde\n',
      windowStart: 0,
      windowEnd: 5,
    });
  });
});

describe('logtail', () => {
  it('emits a trailing partial line only after a later response completes it', async () => {
    const getFn = vi
      .fn()
      .mockResolvedValueOnce({
        data: 'first\npart',
        status: 206,
        headers: headers('bytes 0-9/10'),
      })
      .mockResolvedValueOnce({
        data: '\npartial\n',
        status: 206,
        headers: headers('bytes 5-13/14'),
      });

    const parts = await firstValueFrom(
      logtail(getFn, 1, 100).pipe(
        filter((part) => part.type === StreamType.Data),
        take(2),
        toArray(),
      ),
    );

    expect(parts.map((part) => part.addendum)).toEqual([
      'first\n',
      'partial\n',
    ]);
    expect(parts.map((part) => part.windowStart)).toEqual([0, 6]);
    expect(parts.map((part) => part.windowEnd)).toEqual([5, 13]);
    expect(getFn.mock.calls[1][0].headers.range).toBe('bytes=5-');
  });
});
