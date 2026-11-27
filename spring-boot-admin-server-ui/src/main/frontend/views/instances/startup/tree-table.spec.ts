import { fireEvent, render } from '@testing-library/vue';
import { expect, it } from 'vitest';

import TreeTable from './tree-table.vue';

import { StartupActuatorService } from '@/services/startup-actuator';

it('mounts visible rows and restores expanded descendants when reopening a branch', async () => {
  const events = [0, 1, 2].map((id) => ({
    startupStep: { id, parentId: id - 1, name: 'step-' + id, tags: [] },
    duration: 'PT1S',
  }));
  const tree = StartupActuatorService.parseAsTree({ timeline: { events } });
  const { container } = render(TreeTable, {
    props: { tree, expand: new Set() },
    global: { mocks: { $t: (key: string) => key } },
  });
  const rows = () => container.querySelectorAll('.tree-item');
  const toggle = (index: number) =>
    fireEvent.click(rows()[index].querySelector('.icon')!);

  expect(rows()).toHaveLength(1);
  await toggle(0);
  expect(rows()).toHaveLength(2);
  await toggle(1);
  expect(rows()).toHaveLength(3);
  await toggle(0);
  expect(rows()).toHaveLength(1);
  await toggle(0);
  expect(rows()).toHaveLength(3);
});
