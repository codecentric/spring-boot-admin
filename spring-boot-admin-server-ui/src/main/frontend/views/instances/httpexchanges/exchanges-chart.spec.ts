import { waitFor } from '@testing-library/vue';
import { Chart } from 'chart.js';
import moment from 'moment';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { render } from '@/test-utils';
import { Exchange } from '@/views/instances/httpexchanges/Exchange';
import ExchangesChart from '@/views/instances/httpexchanges/exchanges-chart.vue';

vi.mock('chartjs-adapter-moment', () => ({}));

HTMLCanvasElement.prototype.getContext = vi.fn(
  () => ({}),
) as unknown as typeof HTMLCanvasElement.prototype.getContext;

const makeExchange = (isoTimestamp: string, status = 200) =>
  new Exchange({
    timestamp: isoTimestamp,
    request: { uri: '/api/test', method: 'GET', headers: {} },
    response: { status, headers: {} },
  });

describe('ExchangesChart – time range selection', () => {
  beforeEach(() => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.spyOn(Chart.prototype, 'update').mockImplementation(() => {});
    vi.spyOn(Element.prototype, 'getBoundingClientRect').mockReturnValue({
      left: 0,
      right: 500,
      top: 0,
      bottom: 200,
      width: 500,
      height: 200,
      x: 0,
      y: 0,
      toJSON: () => {},
    } as DOMRect);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  const renderChart = (exchanges: Exchange[] = []) => {
    const result = render(ExchangesChart, { props: { exchanges } });
    const chartEl = result.container.querySelector(
      '.exchange-chart',
    ) as HTMLElement;
    const vm = (chartEl as any)?.__vueParentComponent?.proxy;
    if (vm?.chart) {
      vm.chart.chartArea = { left: 0, right: 500 };
      vm.chart.scales = { x: { getPixelForValue: () => 0 } };
    }
    const fire = (type: string, clientX?: number) =>
      chartEl.dispatchEvent(new MouseEvent(type, { clientX, bubbles: true }));
    return { ...result, chartEl, vm, fire };
  };

  describe('selection overlay', () => {
    it('is not shown initially', () => {
      const { container } = renderChart();

      expect(container.querySelector('.exchange-chart__selection')).toBeNull();
    });

    it('appears while the mouse is dragged', async () => {
      const { fire, container } = renderChart();

      fire('mousedown', 100);
      fire('mousemove', 200);

      await waitFor(() =>
        expect(
          container.querySelector('.exchange-chart__selection'),
        ).toBeInTheDocument(),
      );
    });

    it('disappears after mouseup', async () => {
      const { fire, container } = renderChart();
      fire('mousedown', 100);
      fire('mousemove', 200);
      await waitFor(() =>
        expect(
          container.querySelector('.exchange-chart__selection'),
        ).toBeInTheDocument(),
      );

      fire('mouseup');

      await waitFor(() =>
        expect(
          container.querySelector('.exchange-chart__selection'),
        ).toBeNull(),
      );
    });

    it('disappears when the mouse leaves the chart', async () => {
      const { fire, container } = renderChart();
      fire('mousedown', 100);
      fire('mousemove', 200);
      await waitFor(() =>
        expect(
          container.querySelector('.exchange-chart__selection'),
        ).toBeInTheDocument(),
      );

      fire('mouseleave');

      await waitFor(() =>
        expect(
          container.querySelector('.exchange-chart__selection'),
        ).toBeNull(),
      );
    });

    it('has correct position and width for a left-to-right drag', async () => {
      const { fire, container } = renderChart();

      fire('mousedown', 100);
      fire('mousemove', 300);

      await waitFor(() => {
        const overlay = container.querySelector<HTMLElement>(
          '.exchange-chart__selection',
        );
        expect(overlay?.style.left).toBe('100px');
        expect(overlay?.style.width).toBe('200px');
      });
    });

    it('has correct position and width for a right-to-left drag', async () => {
      const { fire, container } = renderChart();

      fire('mousedown', 300);
      fire('mousemove', 100);

      await waitFor(() => {
        const overlay = container.querySelector<HTMLElement>(
          '.exchange-chart__selection',
        );
        expect(overlay?.style.left).toBe('100px');
        expect(overlay?.style.width).toBe('200px');
      });
    });
  });

  describe('select event', () => {
    it('emits null when clicking without dragging', async () => {
      const { emitted, fire } = renderChart();

      fire('mousedown', 150);
      fire('mouseup');

      await waitFor(() => expect(emitted().select).toBeTruthy());
      expect(emitted().select[0]).toEqual([null]);
    });

    it('emits null when drag distance is less than 5 pixels', async () => {
      const { emitted, fire } = renderChart();

      fire('mousedown', 100);
      fire('mousemove', 103);
      fire('mouseup');

      await waitFor(() => expect(emitted().select).toBeTruthy());
      expect(emitted().select[0]).toEqual([null]);
    });

    it('emits null when no exchanges fall within the selected pixel range', async () => {
      const exchanges = [makeExchange('2024-01-01T10:00:00.050Z')];
      const { emitted, fire, vm } = renderChart(exchanges);
      if (vm?.chart) vm.chart.scales.x.getPixelForValue = () => 100;

      fire('mousedown', 300);
      fire('mousemove', 490);
      fire('mouseup');

      await waitFor(() => expect(emitted().select).toBeTruthy());
      expect(emitted().select[0]).toEqual([null]);
    });

    it('emits [min, max] moments for exchanges within the selected range', async () => {
      const t1 = '2024-01-01T10:00:00.050Z';
      const t2 = '2024-01-01T10:00:00.100Z';
      const t3 = '2024-01-01T10:00:00.200Z';
      const exchanges = [makeExchange(t3), makeExchange(t2), makeExchange(t1)];
      const { emitted, fire, vm } = renderChart(exchanges);
      const pixelMap: Record<string, number> = {
        [moment(t1).toISOString()]: 50,
        [moment(t2).toISOString()]: 100,
        [moment(t3).toISOString()]: 200,
      };
      if (vm?.chart) {
        vm.chart.scales.x.getPixelForValue = (ts: moment.Moment) =>
          pixelMap[ts.toISOString()] ?? 0;
      }

      fire('mousedown', 10);
      fire('mousemove', 400);
      fire('mouseup');

      await waitFor(() => expect(emitted().select).toBeTruthy());
      const [range] = emitted().select[0] as [moment.Moment[]];
      expect(range).toHaveLength(2);
      expect(range[0].isSame(moment(t1))).toBe(true);
      expect(range[1].isSame(moment(t3))).toBe(true);
    });

    it('emits [min, max] regardless of drag direction (right-to-left)', async () => {
      const t1 = '2024-01-01T10:00:00.050Z';
      const t2 = '2024-01-01T10:00:00.200Z';
      const exchanges = [makeExchange(t2), makeExchange(t1)];
      const { emitted, fire, vm } = renderChart(exchanges);
      if (vm?.chart) {
        vm.chart.scales.x.getPixelForValue = (ts: moment.Moment) =>
          ts.isSame(moment(t1)) ? 50 : 150;
      }

      fire('mousedown', 400);
      fire('mousemove', 10);
      fire('mouseup');

      await waitFor(() => expect(emitted().select).toBeTruthy());
      const [range] = emitted().select[0] as [moment.Moment[]];
      expect(range).toHaveLength(2);
      expect(range[0].isSame(moment(t1))).toBe(true);
      expect(range[1].isSame(moment(t2))).toBe(true);
    });
  });
});
