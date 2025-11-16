/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { describe, expect, it } from 'vitest';

import { sanitizeHtml } from './sanitizeHtml';

describe('sanitizeHtml', () => {
  it('should return plain text unchanged', () => {
    const input = 'This is plain text';
    expect(sanitizeHtml(input)).toBe('This is plain text');
  });

  it('should preserve safe HTML tags', () => {
    const input = '<p>Hello <strong>world</strong></p>';
    expect(sanitizeHtml(input)).toBe('<p>Hello <strong>world</strong></p>');
  });

  it('should remove script tags', () => {
    const input = '<p>Hello</p><script>alert("XSS")</script>';
    expect(sanitizeHtml(input)).toBe('<p>Hello</p>');
  });

  it('should remove potentially dangerous attributes', () => {
    const input = '<p onclick="alert(\'XSS\')">Click me</p>';
    expect(sanitizeHtml(input)).toBe('<p>Click me</p>');
  });

  it('should remove iframe tags', () => {
    const input = '<p>Content</p><iframe src="evil.com"></iframe>';
    expect(sanitizeHtml(input)).toBe('<p>Content</p>');
  });

  it('should transform anchor tags to open in new tab', () => {
    const input = '<a href="https://example.com">Link</a>';
    const result = sanitizeHtml(input);
    expect(result).toContain('target="_blank"');
    expect(result).toContain('https://example.com');
  });

  it('should preserve existing anchor href and add target="_blank"', () => {
    const input = '<a href="https://spring.io">Spring</a>';
    const result = sanitizeHtml(input);
    expect(result).toBe(
      '<a href="https://spring.io" target="_blank">Spring</a>',
    );
  });

  it('should handle anchor tags that already have target attribute', () => {
    const input = '<a href="https://example.com" target="_self">Link</a>';
    const result = sanitizeHtml(input);
    // The simpleTransform should override the target attribute
    expect(result).toContain('target="_blank"');
  });

  it('should remove javascript: protocol from links', () => {
    const input = '<a href="javascript:alert(\'XSS\')">Click</a>';
    const result = sanitizeHtml(input);
    expect(result).not.toContain('javascript:');
  });

  it('should handle empty string', () => {
    expect(sanitizeHtml('')).toBe('');
  });

  it('should handle multiple paragraphs with mixed content', () => {
    const input = '<p>First paragraph</p><p>Second <em>paragraph</em></p>';
    expect(sanitizeHtml(input)).toBe(
      '<p>First paragraph</p><p>Second <em>paragraph</em></p>',
    );
  });

  it('should remove style tags', () => {
    const input = '<style>body { color: red; }</style><p>Content</p>';
    expect(sanitizeHtml(input)).toBe('<p>Content</p>');
  });

  it('should handle nested HTML elements', () => {
    const input = '<div><p>Nested <strong>content</strong></p></div>';
    const result = sanitizeHtml(input);
    expect(result).toContain('Nested');
    expect(result).toContain('content');
  });

  it('should remove object and embed tags', () => {
    const input =
      '<p>Safe content</p><object data="evil.swf"></object><embed src="evil.swf">';
    expect(sanitizeHtml(input)).toBe('<p>Safe content</p>');
  });

  it('should preserve multiple links with target="_blank" for each', () => {
    const input =
      '<a href="https://first.com">First</a> and <a href="https://second.com">Second</a>';
    const result = sanitizeHtml(input);
    expect(result).toBe(
      '<a href="https://first.com" target="_blank">First</a> and <a href="https://second.com" target="_blank">Second</a>',
    );
  });

  it('should handle HTML entities correctly', () => {
    const input = '<p>Price: &lt;$10&gt;</p>';
    expect(sanitizeHtml(input)).toBe('<p>Price: &lt;$10&gt;</p>');
  });

  it('should remove form elements', () => {
    const input = '<form><input type="text" /></form><p>Content</p>';
    expect(sanitizeHtml(input)).toBe('<p>Content</p>');
  });

  it('should handle links with query parameters', () => {
    const input =
      '<a href="https://example.com?param=value&other=123">Link</a>';
    const result = sanitizeHtml(input);
    expect(result).toContain('https://example.com?param=value&amp;other=123');
    expect(result).toContain('target="_blank"');
  });

  it('should preserve list structures', () => {
    const input = '<ul><li>Item 1</li><li>Item 2</li></ul>';
    expect(sanitizeHtml(input)).toBe('<ul><li>Item 1</li><li>Item 2</li></ul>');
  });

  it('should preserve download attribute in anchor tag', () => {
    const input = '<a href="https://example.com" download>Link</a>';
    const result = sanitizeHtml(input);
    expect(result).toContain('https://example.com');
    expect(result).toContain('download');
  });
});
