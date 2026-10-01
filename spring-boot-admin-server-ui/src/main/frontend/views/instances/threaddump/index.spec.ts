import { describe, expect, it } from 'vitest';

import ThreadDump from '@/views/instances/threaddump/index.vue';

describe('ThreadDump', () => {
  it('keeps threads in the order returned by the server', () => {
    const vm = {
      threads: {},
      threadOrder: [],
    };

    ThreadDump.methods.updateTimelines.call(vm, [
      { threadId: 42, threadState: 'RUNNABLE', threadName: 'second' },
      { threadId: 7, threadState: 'WAITING', threadName: 'first' },
    ]);

    expect(vm.threadOrder).toEqual([42, 7]);
    expect(ThreadDump.computed.orderedThreads.call(vm)).toEqual([
      vm.threads[42],
      vm.threads[7],
    ]);
  });
});
