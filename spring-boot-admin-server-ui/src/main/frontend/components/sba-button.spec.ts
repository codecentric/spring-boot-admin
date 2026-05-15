import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';
import { defineComponent } from 'vue';

import SbaButton from './sba-button.vue';

import { render } from '@/test-utils';

describe('SbaButton', () => {
  it('renders as a button element by default', () => {
    render(SbaButton, { slots: { default: 'Click me' } });
    expect(
      screen.getByRole('button', { name: 'Click me' }),
    ).toBeInTheDocument();
  });

  it('renders slot content', () => {
    render(SbaButton, { slots: { default: 'Submit' } });
    expect(screen.getByText('Submit')).toBeVisible();
  });

  it('renders as an anchor element when as="a"', () => {
    render(SbaButton, {
      props: { as: 'a', href: '#' },
      slots: { default: 'Link' },
    });
    expect(screen.getByRole('link', { name: 'Link' })).toBeInTheDocument();
  });

  it('sets href on anchor element', () => {
    render(SbaButton, {
      props: { as: 'a', href: 'https://example.com' },
      slots: { default: 'Link' },
    });
    expect(screen.getByRole('link', { name: 'Link' })).toHaveAttribute(
      'href',
      'https://example.com',
    );
  });

  it('sets title attribute', () => {
    render(SbaButton, {
      props: { title: 'My tooltip' },
      slots: { default: 'Btn' },
    });
    expect(screen.getByRole('button', { name: 'Btn' })).toHaveAttribute(
      'title',
      'My tooltip',
    );
  });

  it('is disabled when disabled prop is true', () => {
    render(SbaButton, {
      props: { disabled: true },
      slots: { default: 'Disabled' },
    });
    expect(screen.getByRole('button', { name: 'Disabled' })).toBeDisabled();
  });

  it('is not disabled by default', () => {
    render(SbaButton, { slots: { default: 'Active' } });
    expect(screen.getByRole('button', { name: 'Active' })).not.toBeDisabled();
  });

  it('emits click event when button is clicked', async () => {
    const { emitted } = render(SbaButton, { slots: { default: 'Click' } });
    await userEvent.click(screen.getByRole('button', { name: 'Click' }));
    expect(emitted().click).toHaveLength(1);
  });

  it('does not emit click event when rendered as anchor', async () => {
    const { emitted } = render(SbaButton, {
      props: { as: 'a', href: '#' },
      slots: { default: 'Link' },
    });
    await userEvent.click(screen.getByRole('link', { name: 'Link' }));
    expect(emitted().click).toBeUndefined();
  });

  it('accepts a Vue component as the as prop and emits click', async () => {
    const StubComponent = defineComponent({
      template: '<button v-bind="$attrs"><slot /></button>',
    });
    const { emitted } = render(SbaButton, {
      props: { as: StubComponent },
      slots: { default: 'Component' },
    });
    await userEvent.click(screen.getByRole('button', { name: 'Component' }));
    expect(emitted().click).toHaveLength(1);
  });

  it('passes attrs through to a component passed as the as prop', () => {
    const StubComponent = defineComponent({
      template: '<span v-bind="$attrs"><slot /></span>',
    });
    render(SbaButton, {
      props: { as: StubComponent, primary: true },
      slots: { default: 'Component' },
    });
    expect(screen.getByText('Component')).toHaveClass('is-primary');
  });

  it('applies is-primary class when primary prop is true', () => {
    render(SbaButton, {
      props: { primary: true },
      slots: { default: 'Primary' },
    });
    expect(screen.getByRole('button', { name: 'Primary' })).toHaveClass(
      'is-primary',
    );
  });

  it('does not apply is-primary class by default', () => {
    render(SbaButton, { slots: { default: 'Default' } });
    expect(screen.getByRole('button', { name: 'Default' })).not.toHaveClass(
      'is-primary',
    );
  });

  it.each([
    ['2xs', 'px-1.5'],
    ['xs', 'px-2'],
    ['sm', 'px-3'],
    ['base', 'px-4'],
  ])('applies correct padding class for size="%s"', (size, expectedClass) => {
    render(SbaButton, { props: { size }, slots: { default: 'Btn' } });
    expect(screen.getByRole('button', { name: 'Btn' })).toHaveClass(
      expectedClass,
    );
  });
});
