import { render, screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import i18n from '@/i18n/index';
import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import Metric from '@/views/instances/metrics/metric.vue';

describe('Metric', () => {
  it('renders the metric with percent format', () => {
    const metricName = 'percent.test';
    const statisticType = 'VALUE';
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(Metric, {
      global: {
        plugins: [i18n],
        stubs: {
          FontAwesomeIcon: true,
        },
      },
      props: {
        metricName,
        instance,
        statisticTypes: {
          [statisticType]: 'percent',
        },
      },
      data() {
        return {
          statistics: [statisticType],
          measurements: [[{ statistic: statisticType, value: 0.1234 }]],
        };
      },
    });

    expect(screen.getByText('12.34%')).toBeInTheDocument();
  });
});
