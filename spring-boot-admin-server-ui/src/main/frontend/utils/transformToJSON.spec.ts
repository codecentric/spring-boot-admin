import { describe, expect, it } from 'vitest';

import { transformToJSON } from '@/utils/transformToJSON';

const input = {
  'service-url': 'http://localhost:8080',
  'sidebar.links.0.iframe': 'true',
  'sidebar.links.0.label': '🏠 Home',
  'sidebar.links.0.url': 'https://codecentric.de',
  'tags.environment': 'test',
};

describe('transformToNestedPojo', () => {
  const output = transformToJSON(input);

  it('transforms simple key', () => {
    expect(output['service-url']).toEqual('http://localhost:8080');
  });

  it('transforms nested key', () => {
    expect(output.tags.environment).toEqual('test');
  });

  it('transforms array', () => {
    expect(output.sidebar.links).toHaveLength(1);
    expect(output.sidebar.links[0]).toEqual({
      iframe: 'true',
      label: '🏠 Home',
      url: 'https://codecentric.de',
    });
  });
});
