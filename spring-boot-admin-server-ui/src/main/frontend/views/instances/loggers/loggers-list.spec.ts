import { shallowMount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';
import { nextTick } from 'vue';

import LoggersList from './loggers-list.vue';

describe('Loggers List', () => {
  it('should extend list on scroll', async () => {
    const loggers = createLoggers(500);
    const loggersList = shallowMount(LoggersList, {
      props: {
        levels: ['OFF', 'ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'],
        loggers: loggers,
        loggersStatus: {},
      },
    });

    expect(loggersList.vm.visibleLimit).toBe(25);
    expect(loggersList.text()).toContain('my-logger-24');
    expect(loggersList.text()).not.toContain('my-logger-25');

    loggersList.vm.$refs.infiniteLoading.$emit('infinite', {
      loaded: () => {},
      complete: () => {},
    });

    expect(loggersList.vm.visibleLimit).toBe(50);
    await nextTick();

    expect(loggersList.text()).toContain('my-logger-49');
    expect(loggersList.text()).not.toContain('my-logger-50');
  });

  function createLoggers(number: number): Array<any> {
    return Array(number)
      .fill(0)
      .map((_, i) => createLogger(i));
  }

  function createLogger(i: number): any {
    return {
      name: `my-logger-${i}`,
      level: [{ effectiveLevel: 'INFO', instanceId: '123' }],
    };
  }
});
