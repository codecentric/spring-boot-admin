import { screen, waitFor } from '@testing-library/vue';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import ScheduledTaskExecutions from '@/views/instances/scheduledtasks/scheduled-task-executions.vue';

describe('ScheduledTaskExecutions', () => {
  const originalFormat = Intl.DateTimeFormat;

  beforeEach(() => {
    Intl.DateTimeFormat = function (locale, options) {
      return new originalFormat(locale, {
        ...options,
        timeZone: 'Europe/Berlin',
      });
    } as any;
  });

  afterEach(() => {
    Intl.DateTimeFormat = originalFormat;
  });

  const baseTask = {
    nextExecution: { time: '2024-06-01T12:00:00Z' },
    lastExecution: { time: '2024-05-31T12:00:00Z', status: 'SUCCESS' },
  };

  it('renders nextExecution time', async () => {
    render(ScheduledTaskExecutions, {
      props: { task: baseTask },
    });
    const nextExec = await screen.findByText('Jun 1, 2024, 2:00:00 PM');
    expect(nextExec).toBeVisible();
  });

  it('renders lastExecution time and status when lastExecution exists', async () => {
    render(ScheduledTaskExecutions, {
      props: { task: baseTask },
    });

    const lastExec = await screen.findByText('Jun 1, 2024, 2:00:00 PM');
    expect(lastExec).toBeVisible();

    const statusBadge = await screen.findByRole('status');
    expect(statusBadge).toHaveTextContent('SUCCESS');
    expect(statusBadge).toHaveClass('status-badge success');
  });

  it('does not render lastExecution time or status if lastExecution is missing', async () => {
    const task = { nextExecution: { time: '2024-06-01T12:00:00Z' } };
    render(ScheduledTaskExecutions, {
      props: { task },
    });

    const nextExec = await screen.findByText('Jun 1, 2024, 2:00:00 PM');
    expect(nextExec).toBeVisible();

    await waitFor(() =>
      expect(screen.queryByRole('status')).not.toBeInTheDocument(),
    );

    await waitFor(() =>
      expect(screen.queryByTestId('lastExecution')).not.toBeInTheDocument(),
    );
  });

  it('applies classNames with lowercase status', async () => {
    const task = {
      nextExecution: { time: '2024-06-01T12:00:00Z' },
      lastExecution: { time: '2024-05-31T12:00:00Z', status: 'ERROR' },
    };
    render(ScheduledTaskExecutions, {
      props: { task },
    });

    const statusBadge = await screen.findByRole('status');
    expect(statusBadge).toHaveTextContent('ERROR');
    expect(statusBadge).toHaveClass('status-badge error');
  });
});
