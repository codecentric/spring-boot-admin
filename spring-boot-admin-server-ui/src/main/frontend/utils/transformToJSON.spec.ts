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
  it('transforms simple key', () => {
    const output = transformToJSON(input);

    expect(output['service-url']).toEqual('http://localhost:8080');
  });

  it('transforms nested key', () => {
    const output = transformToJSON(input);

    expect(output.tags.environment).toEqual('test');
  });

  it('transforms array', () => {
    const output = transformToJSON(input);

    expect(output.sidebar.links).toHaveLength(1);
    expect(output.sidebar.links[0]).toEqual({
      iframe: 'true',
      label: '🏠 Home',
      url: 'https://codecentric.de',
    });
  });

  it('handles conflict when property is both string and object', () => {
    const conflictInput = {
      'my-service-headless.kubernetes': 'namespace',
      'my-service-headless': 'some-value',
    };

    const output = transformToJSON(conflictInput, 'LAX');
    expect(output['my-service-headless']['__']).toEqual('some-value');
    expect(output['my-service-headless']['kubernetes']).toEqual('namespace');
  });
});
