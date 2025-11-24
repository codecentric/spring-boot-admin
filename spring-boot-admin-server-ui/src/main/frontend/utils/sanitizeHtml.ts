import _sanitizeHtml from 'sanitize-html';

export function sanitizeHtml(dirty: string): string {
  return _sanitizeHtml(dirty, {
    allowedEmptyAttributes:
      _sanitizeHtml.defaults.allowedEmptyAttributes.concat('download'),
    allowedAttributes: {
      a: _sanitizeHtml.defaults.allowedAttributes['a'].concat('download'),
    },
    transformTags: {
      a: function (tagName, attribs) {
        let newAttribs = attribs ? attribs : {};

        // When download attribute is not set, set target attribute
        if (attribs.download === undefined) {
          newAttribs = {
            ...newAttribs,
            target: '_blank',
          };
        }

        return {
          tagName,
          attribs: {
            ...newAttribs,
          },
        };
      },
    },
  });
}
