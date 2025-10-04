import { describe } from 'vitest';

import SbaFormattedObj from '@/components/sba-formatted-obj.vue';

import { render } from '@/test-utils';

describe('SbaFormattedObject', () => {
  describe('processes simple text', () => {
    it('autolinks urls', async () => {
      render(SbaFormattedObj, {
        props: {
          value:
            'This is a text with a link https://www.codecentric.de that is automagically created.',
        },
      });

      expect(document.body.innerHTML).toMatchSnapshot();
    });

    it('does render (safe) html', async () => {
      render(SbaFormattedObj, {
        props: {
          value:
            "This is a text with <a href='https://www.codecentric.de'>a link</a> and a bold text <b>bold</b>.",
        },
      });

      expect(document.body.innerHTML).toMatchSnapshot();
    });

    it('does not render unsafe <script> in html', async () => {
      render(SbaFormattedObj, {
        props: {
          value: "<script>(function() { console.log('pwned'); )})();</script>",
        },
      });

      expect(document.body.innerHTML).toMatchSnapshot();
    });

    it('does not render unsafe <img> in html', async () => {
      render(SbaFormattedObj, {
        props: {
          value:
            "<img onload='javascript:alert(1)' src='https://www.codecentric.de' />",
        },
      });

      expect(document.body.innerHTML).toMatchSnapshot();
    });
  });

  describe('processes json', () => {
    it('autolinks urls', async () => {
      render(SbaFormattedObj, {
        props: {
          value: {
            a: {
              nested: {
                object: {
                  key: 'This is a text with a link https://www.codecentric.de that is automagically created.',
                },
              },
            },
          },
        },
      });

      expect(document.body.innerHTML).toMatchSnapshot();
    });

    it('does not render unsafe <script> in html', async () => {
      render(SbaFormattedObj, {
        props: {
          value: {
            a: {
              nested: {
                object: {
                  key: "<script>(function() { console.log('pwned'); )})();</script>",
                },
              },
            },
          },
        },
      });

      expect(document.body.innerHTML).toMatchSnapshot();
    });
  });
});
