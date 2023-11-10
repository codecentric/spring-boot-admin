import { beforeEach, describe, expect, it } from 'vitest';

import ViewRegistry from '@/viewRegistry';
import { addExternalLink, addIframeView } from '@/views/external/index';

describe('External View', () => {
  let viewRegistry: ViewRegistry;

  beforeEach(() => {
    viewRegistry = new ViewRegistry();
  });

  describe('External Link', () => {
    it('will be added to view registry', () => {
      addExternalLink(viewRegistry, {
        label: 'External Link',
        url: 'https://www.codecentric.de',
      });

      expect(viewRegistry.views).toHaveLength(1);
      expect(viewRegistry.views.map((v) => v.label)).toContain('External Link');
    });

    it('will be added to view registry with children', () => {
      addExternalLink(viewRegistry, {
        label: 'External Link',
        url: 'https://www.codecentric.de',
        children: [
          {
            label: 'External Link Child',
            url: 'https://www.codecentric.de',
          },
        ],
      });

      expect(viewRegistry.views).toHaveLength(2);
      expect(viewRegistry.views.map((v) => v.label)).toContain('External Link');
      expect(viewRegistry.views.map((v) => v.label)).toContain(
        'External Link Child',
      );
    });

    it('will add multiple external links', () => {
      addExternalLink(viewRegistry, {
        label: 'External Link 1',
        url: 'https://www.codecentric.de',
      });
      addExternalLink(viewRegistry, {
        label: 'External Link 2',
        url: 'https://www.codecentric.de',
      });

      expect(viewRegistry.views).toHaveLength(2);
      expect(viewRegistry.views.map((v) => v.label)).toContain(
        'External Link 1',
      );
      expect(viewRegistry.views.map((v) => v.label)).toContain(
        'External Link 2',
      );
    });
  });

  describe('External Iframe', () => {
    it('will be added to view registry', () => {
      addIframeView(viewRegistry, {
        label: 'External Iframe',
        url: 'https://www.codecentric.de',
        iframe: true,
      });

      expect(viewRegistry.views).toHaveLength(1);
      expect(viewRegistry.views.map((v) => v.label)).toContain(
        'External Iframe',
      );
    });

    it('will add multiple iframes', () => {
      addIframeView(viewRegistry, {
        label: 'External Link 1',
        url: 'https://www.codecentric.de',
      });
      addIframeView(viewRegistry, {
        label: 'External Link 2',
        url: 'https://www.codecentric.de',
      });

      expect(viewRegistry.views).toHaveLength(2);
      expect(viewRegistry.views.map((v) => v.label)).toContain(
        'External Link 1',
      );
      expect(viewRegistry.views.map((v) => v.label)).toContain(
        'External Link 2',
      );
    });
  });
});
