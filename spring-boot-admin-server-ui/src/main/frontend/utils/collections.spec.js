/*
 * Copyright 2014-2019 the original author or authors.
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


import {anyValueMatches} from './collections';

describe('anyValueMatches', () => {
  it('should return predicate value', () => {
    const predicate = jest.fn(() => true);
    expect(anyValueMatches('test', predicate)).toBe(true);
  });

  it('should call predicate for string', () => {
    const predicate = jest.fn();
    anyValueMatches('test', predicate);
    expect(predicate).toHaveBeenCalledWith('test');
  });

  it('should call predicate for number', () => {
    const predicate = jest.fn();
    anyValueMatches(1, predicate);
    expect(predicate).toHaveBeenCalledWith(1);
  });

  it('should call predicate for boolean', () => {
    const predicate = jest.fn();
    anyValueMatches(true, predicate);
    expect(predicate).toHaveBeenCalledWith(true);
  });

  it('should call predicate for null', () => {
    const predicate = jest.fn();
    anyValueMatches(null, predicate);
    expect(predicate).toHaveBeenCalledWith(null);
  });

  it('should call predicate for undefined', () => {
    const predicate = jest.fn();
    anyValueMatches(undefined, predicate);
    expect(predicate).toHaveBeenCalledWith(undefined);
  });

  it('should not call predicate for empty object', () => {
    const predicate = jest.fn();
    anyValueMatches({}, predicate);
    expect(predicate).not.toHaveBeenCalled()
  });

  it('should not call predicate for empty array', () => {
    const predicate = jest.fn();
    anyValueMatches([], predicate);
    expect(predicate).not.toHaveBeenCalled()
  });

  it('should not call predicate for elements in array', () => {
    const predicate = jest.fn();
    anyValueMatches(['test', 1, true, {value: 'nested-obj'}, ['nested-array'], [], {}], predicate);
    expect(predicate).toHaveBeenNthCalledWith(1, 'test');
    expect(predicate).toHaveBeenNthCalledWith(2, 1);
    expect(predicate).toHaveBeenNthCalledWith(3, true);
    expect(predicate).toHaveBeenNthCalledWith(4, 'nested-obj');
    expect(predicate).toHaveBeenNthCalledWith(5, 'nested-array');
  });
});
