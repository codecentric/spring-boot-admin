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

import sbaAlert from '@/components/sba-alert';
import {render, screen} from '@testing-library/vue';

describe('sba-alert', () => {
  it('should render error message', () => {
    const props = {
      error: new Error('i am a error message'),
      title: 'This is a caption',
    }
    render(sbaAlert, {
      props,
      stubs: {
        'font-awesome-icon': true
      }
    });

    expect(screen.getByText('i am a error message')).toBeDefined();
    expect(screen.getByText('This is a caption')).toBeDefined();
  });

  it('should render nothing', () => {
    const props = {
      title: 'This is a caption',
    }
    render(sbaAlert, {
      props,
      stubs: {
        'font-awesome-icon': true
      }
    });

    expect(screen.queryByText('This is a caption')).not.toBeInTheDocument();
  });
})
