import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import Instance from '@/services/instance';
import { render } from '@/test-utils';
import { Exchange } from '@/views/instances/httpexchanges/Exchange';
import HttpExchanges from '@/views/instances/httpexchanges/index.vue';

describe('HttpExchanges - excludeActuator', () => {
  const createExchange = (uri: string, status: number = 200) => {
    return new Exchange({
      timestamp: new Date().toISOString(),
      request: {
        uri,
        method: 'GET',
        headers: {},
      },
      response: {
        status,
        headers: {},
      },
    });
  };

  const createInstance = (
    fetchHttpExchanges,
    serviceUrl = 'http://localhost:8080',
    managementUrl = 'http://localhost:8080/actuator',
  ) => {
    const instance = new Instance({
      id: '4711',
      registration: {
        serviceUrl,
        managementUrl,
      },
    });
    instance.fetchHttpExchanges = fetchHttpExchanges;
    instance.hasEndpoint = vi.fn().mockReturnValue(true);
    return instance;
  };

  describe('when actuator path is present', () => {
    const exchanges = [
      createExchange('http://127.0.0.1:8080/api/users'),
      createExchange('http://127.0.0.1:8080/actuator/health'),
      createExchange('http://127.0.0.1:8080/actuator/metrics'),
      createExchange('http://127.0.0.1:8080/api/products'),
      createExchange('http://127.0.0.1:8080/actuator/info'),
    ];

    const fetchHttpExchanges = vi.fn().mockResolvedValue({
      data: {
        exchanges: exchanges.map((e) => ({
          timestamp: e.timestamp.toISOString(),
          request: e.request,
          response: e.response,
        })),
      },
    });

    beforeEach(async () => {
      fetchHttpExchanges.mockClear();
    });

    it('excludes actuator exchanges by default', async () => {
      render(HttpExchanges, {
        props: {
          instance: createInstance(fetchHttpExchanges),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      // Wait for exchanges to be loaded and verify only non-actuator URIs are visible
      await waitFor(() => {
        // Should show non-actuator endpoints
        expect(
          screen.getByText('http://127.0.0.1:8080/api/users'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/api/products'),
        ).toBeInTheDocument();

        // Should NOT show actuator endpoints
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/health'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/metrics'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/info'),
        ).not.toBeInTheDocument();
      });
    });

    it('includes actuator exchanges when filter is disabled', async () => {
      render(HttpExchanges, {
        props: {
          instance: createInstance(fetchHttpExchanges),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      // Verify initially actuator endpoints are hidden
      await waitFor(() => {
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/health'),
        ).not.toBeInTheDocument();
      });

      // Find and uncheck the exclude actuator checkbox
      const excludeActuatorCheckbox = await screen.findByLabelText(
        'instances.httpexchanges.filter.exclude_actuator',
      );
      await userEvent.click(excludeActuatorCheckbox);

      // Wait for all exchanges to be shown including actuator endpoints
      await waitFor(() => {
        expect(
          screen.getByText('http://127.0.0.1:8080/api/users'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/api/products'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/actuator/health'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/actuator/metrics'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/actuator/info'),
        ).toBeInTheDocument();
      });
    });

    it('calculates actuator path from managementUrl and serviceUrl', async () => {
      render(HttpExchanges, {
        props: {
          instance: createInstance(
            fetchHttpExchanges,
            'http://localhost:8080',
            'http://localhost:8080/actuator',
          ),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      // Check that the checkbox exists
      const checkbox = await screen.findByLabelText(
        'instances.httpexchanges.filter.exclude_actuator',
      );
      expect(checkbox).toBeDefined();

      // Verify the component calculated actuatorPath as '/actuator'
      // by checking that it filters URIs containing '/actuator'
      await waitFor(() => {
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/health'),
        ).not.toBeInTheDocument();
      });
    });

    it('toggles actuator filter on and off', async () => {
      render(HttpExchanges, {
        props: {
          instance: createInstance(fetchHttpExchanges),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      const excludeActuatorCheckbox = await screen.findByLabelText(
        'instances.httpexchanges.filter.exclude_actuator',
      );

      // Initially checked (excluding actuator)
      expect(excludeActuatorCheckbox).toBeChecked();

      // Uncheck to include actuator
      await userEvent.click(excludeActuatorCheckbox);
      await waitFor(() => {
        expect(excludeActuatorCheckbox).not.toBeChecked();
      });

      // Check again to exclude actuator
      await userEvent.click(excludeActuatorCheckbox);
      await waitFor(() => {
        expect(excludeActuatorCheckbox).toBeChecked();
      });
    });
  });

  describe('edge cases with actuator in domain name', () => {
    it('does not filter out URLs with "actuator" in domain but not in path', async () => {
      // Test case where "actuator" appears in the domain name but the endpoint
      // itself is not an actuator endpoint
      const exchanges = [
        createExchange('http://actuator.localhost/weather', 200),
        createExchange('http://localhost/actuator-demo/health', 200),
        createExchange('http://localhost/actuatordemo', 200),
        createExchange('http://127.0.0.1:8080/weather', 200),
        createExchange('http://127.0.0.1:8080/api/data', 200),
        createExchange('http://127.0.0.1:8080/actuator/health', 200),
        createExchange('http://127.0.0.1:8080/actuator', 200),
      ];

      const fetchHttpExchanges = vi.fn().mockResolvedValue({
        data: {
          exchanges: exchanges.map((e) => ({
            timestamp: e.timestamp.toISOString(),
            request: e.request,
            response: e.response,
          })),
        },
      });

      render(HttpExchanges, {
        props: {
          instance: createInstance(
            fetchHttpExchanges,
            'https://actuator-web-frontend-aspire-actuators.dev.localhost:7096',
            'https://actuator-web-frontend-aspire-actuators.dev.localhost:7096/actuator',
          ),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      // With excludeActuator enabled by default, verify correct URIs are shown/hidden
      await waitFor(() => {
        // These should be visible - they don't have /actuator in the path
        expect(
          screen.getByText('http://actuator.localhost/weather'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/weather'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/api/data'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://localhost/actuator-demo/health'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://localhost/actuatordemo'),
        ).toBeInTheDocument();

        // This should be hidden - it has /actuator in the path
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/health'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator'),
        ).not.toBeInTheDocument();
      });

      // Verify the checkbox is present and checked
      const excludeActuatorCheckbox = await screen.findByLabelText(
        'instances.httpexchanges.filter.exclude_actuator',
      );
      expect(excludeActuatorCheckbox).toBeChecked();

      // Uncheck to include all exchanges
      await userEvent.click(excludeActuatorCheckbox);

      // Now all exchanges including the actuator one should be shown
      await waitFor(() => {
        expect(
          screen.getByText('http://actuator.localhost/weather'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/weather'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/api/data'),
        ).toBeInTheDocument();
        expect(
          screen.getByText('http://127.0.0.1:8080/actuator/health'),
        ).toBeInTheDocument();
      });
    });
  });

  describe('excludeActuator filter with other filters', () => {
    const exchanges = [
      createExchange('http://127.0.0.1:8080/api/users', 200),
      createExchange('http://127.0.0.1:8080/actuator/health', 200),
      createExchange('http://127.0.0.1:8080/actuator/metrics', 500),
      createExchange('http://127.0.0.1:8080/api/products', 404),
      createExchange('http://127.0.0.1:8080/actuator/info', 200),
    ];

    const fetchHttpExchanges = vi.fn().mockResolvedValue({
      data: {
        exchanges: exchanges.map((e) => ({
          timestamp: e.timestamp.toISOString(),
          request: e.request,
          response: e.response,
        })),
      },
    });

    beforeEach(() => {
      fetchHttpExchanges.mockClear();
    });

    it('works in combination with success filter', async () => {
      render(HttpExchanges, {
        props: {
          instance: createInstance(fetchHttpExchanges),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      // Wait for initial exchanges to load
      await waitFor(() => {
        expect(
          screen.getByText('http://127.0.0.1:8080/api/users'),
        ).toBeInTheDocument();
      });

      // Uncheck success filter to hide successful requests
      const successCheckbox = await screen.findByLabelText(
        'instances.httpexchanges.filter.success',
      );
      await userEvent.click(successCheckbox);

      // Should show only non-successful, non-actuator exchanges
      // That's just the 404 /api/products
      await waitFor(() => {
        expect(
          screen.getByText('http://127.0.0.1:8080/api/products'),
        ).toBeInTheDocument();

        // Successful non-actuator endpoints should be hidden
        expect(
          screen.queryByText('http://127.0.0.1:8080/api/users'),
        ).not.toBeInTheDocument();

        // Actuator endpoints should still be hidden (both successful and not)
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/health'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/metrics'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/info'),
        ).not.toBeInTheDocument();
      });
    });

    it('works in combination with URI filter', async () => {
      render(HttpExchanges, {
        props: {
          instance: createInstance(fetchHttpExchanges),
        },
      });

      await waitFor(() => expect(fetchHttpExchanges).toHaveBeenCalled());

      // Wait for initial exchanges to load
      await waitFor(() => {
        expect(
          screen.getByText('http://127.0.0.1:8080/api/users'),
        ).toBeInTheDocument();
      });

      // Filter by 'users' - should match only /api/users
      const inputs = screen.getAllByDisplayValue('');
      const filterInput = inputs.find(
        (input) => input.getAttribute('name') === 'filter',
      );
      if (!filterInput) throw new Error('Filter input not found');
      await userEvent.type(filterInput, 'users');

      // Wait for filter to be applied - should show only the users endpoint
      await waitFor(() => {
        expect(
          screen.getByText('http://127.0.0.1:8080/api/users'),
        ).toBeInTheDocument();

        // Other non-actuator endpoints should be hidden by URI filter
        expect(
          screen.queryByText('http://127.0.0.1:8080/api/products'),
        ).not.toBeInTheDocument();

        // Actuator endpoints should remain hidden
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/health'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/metrics'),
        ).not.toBeInTheDocument();
        expect(
          screen.queryByText('http://127.0.0.1:8080/actuator/info'),
        ).not.toBeInTheDocument();
      });
    });
  });
});
