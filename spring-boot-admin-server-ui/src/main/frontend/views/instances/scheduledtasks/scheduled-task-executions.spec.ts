import { shallowMount } from '@vue/test-utils';
import { describe, expect, it } from 'vitest';

import ScheduledTaskExecutions from '@/views/instances/scheduledtasks/scheduled-task-executions.vue';

const SbaFormattedObjStub = {
  name: 'SbaFormattedObj',
  props: ['value'],
  template: '<div class="formatted-obj">{{ value }}</div>',
};

describe('ScheduledTaskExecutions.vue', () => {
  const baseTask = {
    nextExecution: { time: '2024-06-01T12:00:00Z' },
    lastExecution: { time: '2024-05-31T12:00:00Z', status: 'Success' },
  };

  it('renders nextExecution time in SbaFormattedObj', () => {
    const wrapper = shallowMount(ScheduledTaskExecutions, {
      propsData: { task: baseTask },
      stubs: { SbaFormattedObj: SbaFormattedObjStub },
    });
    const nextExec = wrapper.findAllComponents(SbaFormattedObjStub).at(0);
    expect(nextExec.exists()).toBe(true);
    expect(nextExec.props('value')).toBe(baseTask.nextExecution.time);
  });

  it('renders lastExecution time and status when lastExecution exists', () => {
    const wrapper = shallowMount(ScheduledTaskExecutions, {
      propsData: { task: baseTask },
      stubs: { SbaFormattedObj: SbaFormattedObjStub },
    });
    const formattedObjs = wrapper.findAllComponents(SbaFormattedObjStub);
    expect(formattedObjs.length).toBe(2);
    expect(formattedObjs.at(1).props('value')).toBe(
      baseTask.lastExecution.time,
    );

    const statusSpan = wrapper.find('span[role="status"]');
    expect(statusSpan.exists()).toBe(true);
    expect(statusSpan.text()).toBe(baseTask.lastExecution.status);
    expect(statusSpan.classes()).toContain('status-badge');
    expect(statusSpan.classes()).toContain(
      baseTask.lastExecution.status.toLowerCase(),
    );
  });

  it('does not render lastExecution time or status if lastExecution is missing', () => {
    const task = { nextExecution: { time: '2024-06-01T12:00:00Z' } };
    const wrapper = shallowMount(ScheduledTaskExecutions, {
      propsData: { task },
      stubs: { SbaFormattedObj: SbaFormattedObjStub },
    });
    const formattedObjs = wrapper.findAllComponents(SbaFormattedObjStub);
    expect(formattedObjs.length).toBe(1);
    expect(formattedObjs.at(0).props('value')).toBe(task.nextExecution.time);

    const statusSpan = wrapper.find('span[role="status"]');
    expect(statusSpan.exists()).toBe(false);
  });

  it('applies classNames with lowercase status', () => {
    const task = {
      nextExecution: { time: '2024-06-01T12:00:00Z' },
      lastExecution: { time: '2024-05-31T12:00:00Z', status: 'ERROR' },
    };
    const wrapper = shallowMount(ScheduledTaskExecutions, {
      propsData: { task },
      stubs: { SbaFormattedObj: SbaFormattedObjStub },
    });
    const statusSpan = wrapper.find('span[role="status"]');
    expect(statusSpan.exists()).toBe(true);
    expect(statusSpan.classes()).toContain('status-badge');
    expect(statusSpan.classes()).toContain('error');
    expect(statusSpan.text()).toBe('ERROR');
  });

  it('renders correctly with minimal valid task', () => {
    const task = { nextExecution: { time: '2024-06-01T12:00:00Z' } };
    const wrapper = shallowMount(ScheduledTaskExecutions, {
      propsData: { task },
      stubs: { SbaFormattedObj: SbaFormattedObjStub },
    });
    expect(wrapper.exists()).toBe(true);
    expect(wrapper.findAllComponents(SbaFormattedObjStub).length).toBe(1);
  });
});
