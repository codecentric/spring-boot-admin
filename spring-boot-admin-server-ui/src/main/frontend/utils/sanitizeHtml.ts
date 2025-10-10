import _sanitizeHtml from 'sanitize-html';

export function sanitizeHtml(dirty: string): string {
  return _sanitizeHtml(dirty, {
    transformTags: {
      a: _sanitizeHtml.simpleTransform('a', { target: '_blank' }),
    },
  });
}
