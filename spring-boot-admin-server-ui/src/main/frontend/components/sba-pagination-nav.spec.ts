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
import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import SbaPaginationNav from '@/components/sba-pagination-nav.vue';

import { render } from '@/test-utils';

describe('sba-pagination-nav.vue', () => {
  it('should show a button when page count is 0', async () => {
    render(SbaPaginationNav, {
      props: {
        pageCount: 0,
      },
    });

    const prevPage = screen.getByRole('button', {
      name: 'Go to previous page',
    });
    expect(prevPage).toBeDisabled();

    const nextPage = screen.getByRole('button', { name: 'Go to next page' });
    expect(nextPage).toBeDisabled();
  });

  it('should show a button when page count is 1', async () => {
    render(SbaPaginationNav, {
      props: {
        pageCount: 1,
      },
    });

    const prevPage = screen.getByRole('button', {
      name: 'Go to previous page',
    });
    expect(prevPage).toBeDisabled();

    const nextPage = screen.getByRole('button', { name: 'Go to next page' });
    expect(nextPage).toBeDisabled();

    screen.getByRole('button', { name: 'Page 1, current page' });
  });

  it('should show first and last page when page count is 12 including intermediate pages', async () => {
    render(SbaPaginationNav, {
      props: {
        pageCount: 11,
        modelValue: 6,
      },
    });

    expect(
      screen.queryByRole('button', { name: 'Go to previous page' }),
    ).not.toBeDisabled();

    expect(
      screen.queryByRole('button', { name: 'Go to page 1' }),
    ).toBeVisible();
    expect(
      screen.queryByRole('button', { name: 'Go to page 2' }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Go to page 3' }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Go to page 4' }),
    ).toBeVisible();
    expect(
      screen.queryByRole('button', { name: 'Go to page 5' }),
    ).toBeVisible();

    const selectedButton = screen.getByRole('button', {
      name: 'Page 6, current page',
    });
    expect(selectedButton).toBeVisible();
    expect(selectedButton).toBeDisabled();
    expect(selectedButton).toHaveClass('is-active');

    expect(
      screen.queryByRole('button', { name: 'Go to page 7' }),
    ).toBeVisible();
    expect(
      screen.queryByRole('button', { name: 'Go to page 8' }),
    ).toBeVisible();
    expect(
      screen.queryByRole('button', { name: 'Go to page 9' }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Go to page 10' }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole('button', { name: 'Go to page 11' }),
    ).toBeVisible();

    expect(
      screen.queryByRole('button', { name: 'Go to previous page' }),
    ).not.toBeDisabled();
  });
});
