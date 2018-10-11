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

import {shallowMount} from '@vue/test-utils';
import moment from 'moment';
import sbaTimeAgo from './sba-time-ago.js';

moment.now = () => +new Date(1318781879406);

describe('time-ago', () => {

  describe('given  a short time passed time', () => {
    it('should match the snapshot', () => {
      const wrapper = shallowMount(sbaTimeAgo, {
        propsData: {
          date: moment(1318781000000)
        }
      });
      expect(wrapper.vm.$el).toMatchSnapshot();
    });
  });

  describe('given a long time passed time', () => {
    it('should match the snapshot', () => {
      const wrapper = shallowMount(sbaTimeAgo, {
        propsData: {
          date: moment(1310000000000)
        }
      });
      expect(wrapper.vm.$el).toMatchSnapshot();
    });
  });

});
