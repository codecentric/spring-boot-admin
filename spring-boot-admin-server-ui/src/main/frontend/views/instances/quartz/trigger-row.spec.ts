import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import TriggerRow from './trigger-row.vue';

import { render } from '@/test-utils';

describe('trigger-row.vue', () => {
  beforeEach(async () => {
    render(TriggerRow, {
      props: {
        triggerDetail: {
          group: 'group2',
          name: 'triggerSampleJob2',
          state: 'NORMAL',
          type: 'simple',
          startTime: 1629451789546,
          previousFireTime: 1629452183546,
          nextFireTime: 1629452185546,
          priority: 0,
          simple: { interval: 2000, repeatCount: -1, timesTriggered: 198 },
        },
      },
    });
  });

  it('should render with closed details initially', async () => {
    const intervalElement = await screen.queryByText('interval');
    expect(intervalElement).toBeNull();
  });

  it('should render details when clicking on arrow', async () => {
    const toggleArrow = await screen.findByRole('button');
    await userEvent.click(toggleArrow);
    const intervalElement = await screen.findByText('interval');
    expect(intervalElement).toBeVisible();
  });
});
