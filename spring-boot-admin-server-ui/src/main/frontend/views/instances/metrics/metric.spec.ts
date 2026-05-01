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
          /*
            To prevent:
            [Vue warn]: Wrong type passed as event handler to onClass - did you forget @ or : in front of your prop?
            Expected function or array of functions, received type string.
              at <SbaButton class="border-none sm:m-1 bg-transparent! self-end" title=null size="xs"  ... >
              at <SbaIconButton icon="trash" class="self-end" onClick=fn >
              at <Anonymous header-sticks-below="#navigation" title="percent.test" >
              at <Metric metricName="percent.test" instance= Instance { ... } statisticTypes= { VALUE: 'percent' }  ... >
              at <VTUROOT>
           */
          SbaButton: {
            inheritAttrs: false,
            template: '<button><slot /></button>',
          },
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
