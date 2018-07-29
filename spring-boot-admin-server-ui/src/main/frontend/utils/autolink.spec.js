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

import autolink, {Autolink} from './autolink'

describe('autolink should', () => {
  it('return the input string for normal text', () => {
    const str = 'This is just a normal text containing no hyperlinks';
    expect(autolink(str)).toBe(str);
  });

  it('return string with anchor tag for the hyperlink', () => {
    const str = 'Please visit http://example.com.';
    expect(autolink(str)).toBe('Please visit <a href="http://example.com" target="_blank" rel="noopener noreferrer">http://example.com</a>.');
  });

  it('return string with anchor tag with shortened text for the hyperlink', () => {
    const str = 'Please visit http://extraordinary.com/very/very/log/hyperlink.';

    const customAutolink = new Autolink({
      truncate: {
        length: 30,
        location: 'smart'
      },
    });
    expect(customAutolink(str)).toBe('Please visit <a href="http://extraordinary.com/very/very/log/hyperlink" target="_blank" rel="noopener noreferrer" title="http://extraordinary.com/very/very/log/hyperlink">extraordinary.com/very&hellip;rlink</a>.');
  });

  it('return string with anchor for hyperlinks in dense json', () => {
    const str = '{"name":"John Smith","links":[{"rel":"random-link1","href":"https://localhost:8000/api/123/query?action=do_something&age=21","hreflang":null,"media":null,"title":null,"type":null,"deprecation":null}]}';
    expect(autolink(str)).toBe('{"name":"John Smith","links":[{"rel":"random-link1","href":"<a href="https://localhost:8000/api/123/query?action=do_something&age=21" target="_blank" rel="noopener noreferrer">https://localhost:8000/api/123/query?action=do_something&age=21</a>","hreflang":null,"media":null,"title":null,"type":null,"deprecation":null}]}');
  });
});
