import { render } from '@testing-library/vue';
import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import { useDateTimeFormatter } from '@/composables/useDateTimeFormatter';
import ApplicationStatusOverview from '@/views/applications/ApplicationStatusOverview.vue';

describe('ApplicationStatusOverview', () => {
  const renderComponent = (lastUpdate: Date | undefined = undefined) => {
    render(ApplicationStatusOverview, {
      props: {
        iconName: 'check-circle',
        iconColor: 'text-green-500',
        statusLabelTextKey: 'applications.all_up',
        lastUpdate,
      },
      global: {
        mocks: {
          FontAwesomeIcon: true,
          $t: (key: string) => key,
        },
      },
    });
  };
  const { formatDateTime } = useDateTimeFormatter();

  it('shows the last update with a label when provided', () => {
    const lastUpdate = new Date();
    renderComponent(lastUpdate);

    expect(
      screen.getByText(
        `applications.last_update: ${formatDateTime(lastUpdate)}`,
      ),
    ).toBeVisible();
  });

  it('shows no last update information if not provided', () => {
    renderComponent();

    expect(
      screen.queryByText(/applications\.last_update:.+/),
    ).not.toBeInTheDocument();
  });
});
