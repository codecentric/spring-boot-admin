import { screen, waitFor } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import ScheduledTaskExecutions from '@/views/instances/scheduledtasks/scheduled-task-executions.vue';

describe('ScheduledTaskExecutions', () => {
  const baseTask = {
    nextExecution: { time: '2024-06-01T12:00:00Z' },
    lastExecution: { time: '2024-05-31T12:00:00Z', status: 'SUCCESS' },
  };

  it('renders nextExecution time', async () => {
    render(ScheduledTaskExecutions, {
      props: { task: baseTask },
    });
    const nextExec = await screen.findByText('2024-06-01T12:00:00Z');
    expect(nextExec).toBeVisible();
  });

  it('renders lastExecution time and status when lastExecution exists', async () => {
    render(ScheduledTaskExecutions, {
      props: { task: baseTask },
    });

    const lastExec = await screen.findByText('2024-05-31T12:00:00Z');
    expect(lastExec).toBeVisible();

    const statusBadge = await screen.findByRole('status');
    expect(statusBadge.textContent).toBe('SUCCESS');
    expect(statusBadge.className).toBe('status-badge success');
  });

  it('does not render lastExecution time or status if lastExecution is missing', async () => {
    const task = { nextExecution: { time: '2024-06-01T12:00:00Z' } };
    render(ScheduledTaskExecutions, {
      props: { task },
    });

    const nextExec = await screen.findByText('2024-06-01T12:00:00Z');
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
    expect(statusBadge.textContent).toBe('ERROR');
    expect(statusBadge.className).toBe('status-badge error');
  });
});
