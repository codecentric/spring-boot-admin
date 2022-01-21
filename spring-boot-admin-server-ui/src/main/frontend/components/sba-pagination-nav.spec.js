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

import {render} from '@/test-utils';
import SbaPaginationNav from '@/components/sba-pagination-nav';
import {screen} from '@testing-library/vue';

describe('sba-pagination-nav.vue', () => {
  it('should show a button when page count is 0', async () => {
    await render(SbaPaginationNav, {
      props: {
        pageCount: 0
      }
    });

    const prevPage = screen.getByLabelText('Go to previous page');
    expect(prevPage).toBeDisabled();

    const nextPage = screen.getByLabelText('Go to next page');
    expect(nextPage).toBeDisabled();
  });

  it('should show a button when page count is 1', async () => {
    await render(SbaPaginationNav, {
      props: {
        pageCount: 1
      }
    });

    const prevPage = screen.getByLabelText('Go to previous page');
    expect(prevPage).toBeDisabled();

    const nextPage = screen.getByLabelText('Go to next page');
    expect(nextPage).toBeDisabled();

    screen.getByLabelText('Go to page 1');
  });

  it('should show first and last page when page count is 12 including intermediate pages', async () => {
    await render(SbaPaginationNav, {
      props: {
        pageCount: 11,
        current: 6
      }
    });

    expect(screen.queryByLabelText('Go to previous page')).not.toBeDisabled();

    expect(screen.queryByLabelText('Go to page 1')).toBeVisible();
    expect(screen.queryByLabelText('Go to page 2')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Go to page 3')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Go to page 4')).toBeVisible();
    expect(screen.queryByLabelText('Go to page 5')).toBeVisible();
    const selectedButton = screen.queryByLabelText('Go to page 6');
    expect(selectedButton).toBeVisible();
    expect(selectedButton).toHaveClass('is-active');
    expect(screen.queryByLabelText('Go to page 7')).toBeVisible();
    expect(screen.queryByLabelText('Go to page 8')).toBeVisible();
    expect(screen.queryByLabelText('Go to page 9')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Go to page 10')).not.toBeInTheDocument();
    expect(screen.queryByLabelText('Go to page 11')).toBeVisible();

    expect(screen.queryByLabelText('Go to next page')).not.toBeDisabled();
  });
});
