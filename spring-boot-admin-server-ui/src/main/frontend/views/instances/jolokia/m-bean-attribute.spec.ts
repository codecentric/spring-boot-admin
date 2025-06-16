/*
 * Copyright 2014-2025 the original author or authors.
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
import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { describe, expect, it, vi } from 'vitest';

import { render } from '@/test-utils';
import MBeanAttribute from '@/views/instances/jolokia/m-bean-attribute.vue';

describe('m-bean-attribute.vue', () => {
  const baseProps = {
    name: 'testAttribute',
    descriptor: { desc: 'Test attribute description', rw: true },
    value: 'initialValue',
    onSaveValue: vi.fn(),
  };

  it('renders input when value is primitive and not complex', async () => {
    render(MBeanAttribute, { props: baseProps });

    expect(screen.getByLabelText('testAttribute')).toBeInTheDocument();
    expect(screen.queryByRole('textbox', { name: /textarea/i })).toBeNull();
    expect(screen.getByRole('button')).toBeEnabled();
  });

  it('renders readonly textarea when value is complex object', async () => {
    const complexProps = {
      ...baseProps,
      value: { foo: 'bar' },
    };
    render(MBeanAttribute, { props: complexProps });

    expect(screen.getByLabelText('testAttribute')).toBeInTheDocument();
    expect(screen.getByRole('textbox')).toHaveValue(
      JSON.stringify(complexProps.value, null, 4),
    );
    expect(screen.getByRole('textbox')).toHaveAttribute('readonly');
    expect(screen.getByRole('textbox')).toBeDisabled();
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('enables editing mode on edit button click if descriptor.rw is true and value is not complex', async () => {
    render(MBeanAttribute, { props: baseProps });

    const editButton = screen.getByRole('button');
    await userEvent.click(editButton);

    const input = screen.getByLabelText('testAttribute');
    expect(input).not.toHaveAttribute('disabled');
    expect(input).not.toHaveAttribute('readonly');

    expect(screen.getByText('Cancel')).toBeInTheDocument();
    expect(screen.getByText('Save')).toBeInTheDocument();
  });

  it('does not enable editing mode if descriptor.rw is false', async () => {
    const props = {
      ...baseProps,
      descriptor: { ...baseProps.descriptor, rw: false },
    };
    render(MBeanAttribute, { props });

    const editButton = screen.getByRole('button');
    await userEvent.click(editButton);

    const input = screen.getByLabelText('testAttribute');
    expect(input).toHaveAttribute('disabled');
    expect(input).toHaveAttribute('readonly');

    expect(screen.queryByText('Cancel')).toBeNull();
    expect(screen.queryByText('Save')).toBeNull();
  });

  it('cancels editing mode on cancel button click', async () => {
    render(MBeanAttribute, { props: baseProps });

    const editButton = screen.getByRole('button');
    await userEvent.click(editButton);

    const cancelButton = screen.getByText('Cancel');
    await userEvent.click(cancelButton);

    const input = screen.getByLabelText('testAttribute');
    expect(input).toHaveAttribute('disabled');
    expect(input).toHaveAttribute('readonly');

    expect(screen.queryByText('Cancel')).toBeNull();
    expect(screen.queryByText('Save')).toBeNull();
  });

  it('calls onSaveValue and disables editing mode on save', async () => {
    const onSaveValue = vi
      .fn()
      .mockResolvedValue({ data: { value: 'ok', status: 200 }, error: null });
    render(MBeanAttribute, { props: { ...baseProps, onSaveValue } });

    const editButton = screen.getByRole('button');
    await userEvent.click(editButton);

    const input = screen.getByLabelText('testAttribute');
    await userEvent.clear(input);
    await userEvent.type(input, 'newValue');

    const saveButton = screen.getByText('Save');
    await userEvent.click(saveButton);

    await waitFor(() => {
      expect(onSaveValue).toHaveBeenCalled();
    });

    expect(screen.getByLabelText('testAttribute')).toHaveAttribute('disabled');
    expect(screen.getByLabelText('testAttribute')).toHaveAttribute('readonly');
  });

  it('displays error if onSaveValue throws', async () => {
    const error = new Error('save failed');
    const onSaveValue = vi.fn().mockRejectedValue(error);
    render(MBeanAttribute, { props: { ...baseProps, onSaveValue } });

    const editButton = screen.getByRole('button');
    await userEvent.click(editButton);

    const input = screen.getByLabelText('testAttribute');
    await userEvent.type(input, 'newValue');

    const saveButton = screen.getByText('Save');
    await userEvent.click(saveButton);

    await waitFor(() => {
      expect(onSaveValue).toHaveBeenCalled();
    });
  });

  it('disables save button if input equals value', async () => {
    render(MBeanAttribute, { props: baseProps });

    const editButton = screen.getByRole('button');
    await userEvent.click(editButton);

    const saveButton = screen.getByText('Save');
    expect(saveButton).toBeDisabled();
  });

  it('updates input when prop value changes', async () => {
    const { rerender } = render(MBeanAttribute, { props: baseProps });

    expect(screen.getByLabelText('testAttribute')).toHaveValue('initialValue');

    await rerender({ value: 'newValue' });

    expect(screen.getByLabelText('testAttribute')).toHaveValue('newValue');
  });
});
