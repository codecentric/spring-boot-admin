import { screen, waitFor } from '@testing-library/vue';
import { shallowMount } from '@vue/test-utils';
import { describe, expect, it, vi } from 'vitest';

import Mappings from './index.vue';

import { mappings } from '@/mocks/instance/mappings/data';
import Instance from '@/services/instance.js';
import { render } from '@/test-utils';

const mockedMapping =
  mappings.contexts['spring-boot-admin-sample-servlet'].mappings;
const dispatcherServlets = mockedMapping.dispatcherServlets;
const servlets = mockedMapping.servlets;
const servletFilters = mockedMapping.servletFilters;

describe('Mappings', () => {
  it('should render the header for a context when just context name is provided', async () => {
    renderWithInstance({
      contexts: {
        'spring-boot-admin-sample-servlet': {},
      },
    });

    const header = await waitFor(() =>
      screen.getByRole('heading', { name: 'spring-boot-admin-sample-servlet' }),
    );
    expect(header).toBeVisible();
  });

  it('should render handler name when dispatcherServlets are available', async () => {
    renderWithInstance({
      contexts: {
        'spring-boot-admin-sample-servlet': {
          mappings: {
            dispatcherServlets,
          },
        },
      },
    });
    const element = await waitFor(() =>
      screen.getByRole('cell', { name: 'Actuator root web endpoint' }),
    );
    expect(element).toBeVisible();
  });

  it('should render classname when servlets are available', async () => {
    renderWithInstance({
      contexts: {
        'spring-boot-admin-sample-servlet': {
          mappings: {
            servlets,
          },
        },
      },
    });
    const element = await waitFor(() =>
      screen.getByRole('cell', {
        name: 'org.springframework.web.servlet.DispatcherServlet',
      }),
    );
    expect(element).toBeVisible();
  });

  it('should render classname when servletFilters are available', async () => {
    renderWithInstance({
      contexts: {
        'spring-boot-admin-sample-servlet': {
          mappings: {
            servletFilters,
          },
        },
      },
    });
    const element = await waitFor(() =>
      screen.getByRole('cell', { name: 'webMvcMetricsFilter' }),
    );
    expect(element).toBeVisible();
  });

  it.each`
    contentType
    ${'application/vnd.spring-boot.actuator.v2+json'}
    ${'application/vnd.spring-boot.actuator.v3+json'}
    ${'application/vnd.spring-boot.actuator.v2+json;charset=UTF-8'}
    ${'application/vnd.spring-boot.actuator.v3+json;charset=UTF-8'}
  `('should accept allowed content type headers', ({ contentType }) => {
    const wrapper = shallowMount(Mappings, {
      props: {
        instance: createInstanceWithMappingsData(mappings),
      },
    });

    expect(wrapper.vm.isSupportedContextType(contentType)).toBe(true);
  });

  it.each`
    contentType
    ${'application/vnd.spring-boot.actuator.v+json'}
    ${'application/vnd.spring-boot.actuator.v34+json'}
    ${'invalid'}
    ${null}
    ${undefined}
  `('should reject content type headers', ({ contentType }) => {
    const wrapper = shallowMount(Mappings, {
      props: {
        instance: createInstanceWithMappingsData(mappings),
      },
    });

    expect(wrapper.vm.isSupportedContextType(contentType)).toBe(false);
  });

  // Helpers
  function renderWithInstance(data) {
    render(Mappings, {
      props: {
        instance: createInstanceWithMappingsData(data),
      },
    });
  }

  function createInstanceWithMappingsData(data) {
    const instance = new Instance({ id: 4711 });
    instance.fetchMappings = vi.fn().mockReturnValue({
      headers: {
        'content-type': 'application/vnd.spring-boot.actuator.v2+json',
      },
      data,
    });
    return instance;
  }
});
