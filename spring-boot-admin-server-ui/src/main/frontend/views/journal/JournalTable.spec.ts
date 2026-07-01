/*!
 * Copyright 2014-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import NotificationcenterPlugin from '@stekoe/vue-toast-notificationcenter';
import userEvent from '@testing-library/user-event';
import { screen, render as tlRender, within } from '@testing-library/vue';
import { RouterLinkStub } from '@vue/test-utils';
import PrimeVue from 'primevue/config';
import Tooltip from 'primevue/tooltip';
import { beforeEach, describe, expect, it } from 'vitest';
import { createI18n } from 'vue-i18n';
import {
  type LocationQueryRaw,
  createMemoryHistory,
  createRouter,
} from 'vue-router';

import JournalTable from './JournalTable.vue';

import components from '@/components/index';

import SbaModalPlugin from '@/plugins/modal';
import Application from '@/services/application';
import { render } from '@/test-utils';
import {
  InstanceEvent,
  InstanceEventType,
} from '@/views/journal/InstanceEvent';

let terms = {};
const modules: Record<string, any> = import.meta.glob('@/**/i18n.en.json', {
  eager: true,
});
for (const modulesKey in modules) {
  terms = { ...terms, ...modules[modulesKey] };
}

describe('JournalTable', () => {
  let events: InstanceEvent[];
  let applications: Application[];

  beforeEach(() => {
    events = [
      new InstanceEvent({
        instance: 'instance-1',
        version: 1,
        type: InstanceEventType.REGISTERED,
        timestamp: new Date('2024-01-01T10:00:00Z'),
        registration: { name: 'Zebra App' },
      }),
      new InstanceEvent({
        instance: 'instance-2',
        version: 1,
        type: InstanceEventType.REGISTERED,
        timestamp: new Date('2024-01-01T10:05:00Z'),
        registration: { name: 'Alpha App' },
      }),
      new InstanceEvent({
        instance: 'instance-3',
        version: 1,
        type: InstanceEventType.STATUS_CHANGED,
        timestamp: new Date('2024-01-01T10:10:00Z'),
        registration: { name: 'Beta App' },
      }),
      new InstanceEvent({
        instance: 'instance-4',
        version: 1,
        type: InstanceEventType.DEREGISTERED,
        timestamp: new Date('2024-01-01T10:15:00Z'),
        registration: { name: 'Gamma App' },
      }),
      new InstanceEvent({
        instance: 'instance-5',
        version: 2,
        type: InstanceEventType.REGISTRATION_UPDATED,
        timestamp: new Date('2024-01-01T10:20:00Z'),
        registration: { name: 'Delta App' },
      }),
      new InstanceEvent({
        instance: 'instance-6',
        version: 1,
        type: InstanceEventType.INFO_CHANGED,
        timestamp: new Date('2024-01-01T10:25:00Z'),
        registration: { name: 'Epsilon App' },
      }),
      new InstanceEvent({
        instance: 'instance-7',
        version: 1,
        type: InstanceEventType.ENDPOINTS_DETECTED,
        timestamp: new Date('2024-01-01T10:30:00Z'),
        registration: { name: 'Theta App' },
      }),
      new InstanceEvent({
        instance: 'instance-2',
        version: 2,
        type: InstanceEventType.INFO_CHANGED,
        timestamp: new Date('2024-01-01T10:35:00Z'),
        registration: { name: 'Alpha App' },
      }),
    ];

    applications = [
      new Application({
        id: 'app-1',
        name: 'Zebra App',
        instances: [{ id: 'instance-1' }],
      }),
      new Application({
        id: 'app-2',
        name: 'Alpha App',
        instances: [{ id: 'instance-2' }],
      }),
      new Application({
        id: 'app-3',
        name: 'Beta App',
        instances: [{ id: 'instance-3' }],
      }),
      new Application({
        id: 'app-4',
        name: 'Gamma App',
        instances: [{ id: 'instance-4' }],
      }),
      new Application({
        id: 'app-5',
        name: 'Delta App',
        instances: [{ id: 'instance-5' }],
      }),
      new Application({
        id: 'app-6',
        name: 'Epsilon App',
        instances: [{ id: 'instance-6' }],
      }),
      new Application({
        id: 'app-7',
        name: 'Theta App',
        instances: [{ id: 'instance-7' }],
      }),
    ];
  });

  describe('Application name', () => {
    it('should sort by application name', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the application column header and click it to sort
      const applicationHeader = screen.getByText('Application');
      await user.click(applicationHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      // Skip header row
      const dataRows = rows.slice(1);

      // Extract application names from the rows
      const applicationNames = dataRows
        .map((row) => {
          const cells = within(row).queryAllByRole('cell');
          // Application column is the second column (after expander)
          return cells[1]?.textContent?.trim();
        })
        .filter(Boolean);

      // Verify they are sorted alphabetically
      // The table shows all events, and sorting should order them alphabetically
      expect(applicationNames.length).toBeGreaterThan(0);
      // Check that the list is sorted alphabetically
      const sortedNames = [...applicationNames].sort((a, b) =>
        a!.localeCompare(b!),
      );
      expect(applicationNames).toEqual(sortedNames);
    });

    it('should sort by application name in descending order when clicked twice', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the application column header and click it twice
      const applicationHeader = screen.getByText('Application');
      await user.click(applicationHeader);
      await user.click(applicationHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract application names from the rows
      const applicationNames = dataRows
        .map((row) => {
          const cells = within(row).queryAllByRole('cell');
          return cells[1]?.textContent?.trim();
        })
        .filter(Boolean);

      // Verify they are sorted in descending order
      expect(applicationNames.length).toBeGreaterThan(0);
      // Check that the list is sorted in reverse alphabetical order
      const sortedNames = [...applicationNames].sort((a, b) =>
        b!.localeCompare(a!),
      );
      expect(applicationNames).toEqual(sortedNames);
    });

    it('should filter by application name', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Get all rows before filtering
      expect(getVisibleRows()).toHaveLength(8);

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Application',
      );

      await selectOption(user, listbox, 'Alpha App');
      await applyFilter(user, dialog, multiselect);

      expect(getVisibleColumnValues(1)).toEqual(['Alpha App', 'Alpha App']);
    });

    it('should filter by multiple applications', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Application',
      );

      await selectOptions(user, listbox, [
        'Alpha App',
        'Delta App',
        'Zebra App',
      ]);
      await applyFilter(user, dialog, multiselect);

      const applicationNames = getVisibleColumnValues(1);

      expect(applicationNames).toHaveLength(4);
      expect(applicationNames).toEqual(
        expect.arrayContaining(['Alpha App', 'Delta App', 'Zebra App']),
      );
      expect(applicationNames).not.toContain('Beta App');
      expect(applicationNames).not.toContain('Gamma App');
    });

    it('keeps the multiselect overlay open for multiple mouse selections', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Application',
      );

      await selectOption(user, listbox, 'Alpha App');

      expect(listbox).toBeVisible();
      expect(getOption(listbox, 'Delta App')).toBeVisible();

      await selectOption(user, listbox, 'Delta App');

      expect(listbox).toBeVisible();
      expectOptionSelected(listbox, 'Alpha App');
      expectOptionSelected(listbox, 'Delta App');

      await applyFilter(user, dialog, multiselect);
    });
  });

  describe('Instance id', () => {
    it('should sort by instance id', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the instance column header and click it to sort
      const instanceHeader = screen.getByText('Instance');
      await user.click(instanceHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract instance IDs from the rows
      const instanceIds = dataRows
        .map((row) => {
          const cells = within(row).queryAllByRole('cell');
          // Instance column is the third column (after expander and application)
          return cells[2]?.textContent?.trim();
        })
        .filter(Boolean);

      // Verify they are sorted alphabetically
      expect(instanceIds).toEqual([
        'instance-1',
        'instance-2',
        'instance-2',
        'instance-3',
        'instance-4',
        'instance-5',
        'instance-6',
        'instance-7',
      ]);
    });

    it('should sort by instance id in descending order when clicked twice', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the instance column header and click it twice
      const instanceHeader = screen.getByText('Instance');
      await user.click(instanceHeader);
      await user.click(instanceHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract instance IDs from the rows
      const instanceIds = dataRows
        .map((row) => {
          const cells = within(row).queryAllByRole('cell');
          return cells[2]?.textContent?.trim();
        })
        .filter(Boolean);

      // Verify they are sorted in descending order
      expect(instanceIds).toEqual([
        'instance-7',
        'instance-6',
        'instance-5',
        'instance-4',
        'instance-3',
        'instance-2',
        'instance-2',
        'instance-1',
      ]);
    });

    it('should filter by instance id', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      expect(getVisibleRows()).toHaveLength(8);

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Instance',
      );

      await selectOption(user, listbox, 'instance-2');
      await applyFilter(user, dialog, multiselect);

      expect(getVisibleColumnValues(2)).toEqual(['instance-2', 'instance-2']);
    });

    it('should filter by multiple instances', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Instance',
      );

      await selectOptions(user, listbox, [
        'instance-1',
        'instance-3',
        'instance-5',
      ]);
      await applyFilter(user, dialog, multiselect);

      const instanceIds = getVisibleColumnValues(2);

      expect(instanceIds).toHaveLength(3);
      expect(instanceIds).toEqual(
        expect.arrayContaining(['instance-1', 'instance-3', 'instance-5']),
      );
      expect(instanceIds).not.toContain('instance-2');
    });
  });

  describe('Time', () => {
    it('should sort by time', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the time column header and click it to sort
      const timeHeader = screen.getByText('Time');
      await user.click(timeHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract timestamps from the rows
      const timestamps = dataRows.map((row) => {
        const cells = within(row).queryAllByRole('cell');
        // Time column is the fourth column (after expander, application, and instance)
        return cells[3]?.textContent?.trim();
      });

      // Compare by checking the order is ascending
      for (let i = 0; i < timestamps.length - 1; i++) {
        const current = new Date(timestamps[i] || '');
        const next = new Date(timestamps[i + 1] || '');
        expect(current.getTime()).toBeLessThanOrEqual(next.getTime());
      }
    });

    it('should sort by time in descending order when clicked twice', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the time column header and click it twice
      const timeHeader = screen.getByText('Time');
      await user.click(timeHeader);
      await user.click(timeHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract timestamps from the rows
      const timestamps = dataRows.map((row) => {
        const cells = within(row).queryAllByRole('cell');
        return cells[3]?.textContent?.trim();
      });

      // Verify they are sorted in descending order (newest first)
      for (let i = 0; i < timestamps.length - 1; i++) {
        const current = new Date(timestamps[i] || '');
        const next = new Date(timestamps[i + 1] || '');
        expect(current.getTime()).toBeGreaterThanOrEqual(next.getTime());
      }
    });
  });

  describe('Event type', () => {
    it('should sort by event type', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the event column header and click it to sort
      const eventHeader = screen.getByText('Event');
      await user.click(eventHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract event types from the rows
      const eventTypes = dataRows
        .map((row) => {
          const cells = within(row).queryAllByRole('cell');
          // Event column is the fifth column
          return cells[4]?.textContent?.trim();
        })
        .filter(Boolean);

      // Verify they are sorted alphabetically
      expect(eventTypes).toEqual([
        'DEREGISTERED',
        'ENDPOINTS_DETECTED',
        'INFO_CHANGED',
        'INFO_CHANGED',
        'REGISTERED',
        'REGISTERED',
        'REGISTRATION_UPDATED',
        'STATUS_CHANGED',
      ]);
    });

    it('should sort by event type in descending order when clicked twice', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find the event column header and click it twice
      const eventHeader = screen.getByText('Event');
      await user.click(eventHeader);
      await user.click(eventHeader);

      // Get all rows in the table
      const rows = screen.getAllByRole('row');
      const dataRows = rows.slice(1);

      // Extract event types from the rows
      const eventTypes = dataRows
        .map((row) => {
          const cells = within(row).queryAllByRole('cell');
          return cells[4]?.textContent?.trim();
        })
        .filter(Boolean);

      // Verify they are sorted in descending order
      expect(eventTypes).toEqual([
        'STATUS_CHANGED',
        'REGISTRATION_UPDATED',
        'REGISTERED',
        'REGISTERED',
        'INFO_CHANGED',
        'INFO_CHANGED',
        'ENDPOINTS_DETECTED',
        'DEREGISTERED',
      ]);
    });

    it('should filter by event type', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      expect(getVisibleRows()).toHaveLength(8);

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Event',
      );

      await selectOption(user, listbox, 'INFO_CHANGED');
      await applyFilter(user, dialog, multiselect);

      expect(getVisibleColumnValues(4)).toEqual([
        'INFO_CHANGED',
        'INFO_CHANGED',
      ]);
    });

    it('should filter by multiple event types', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      const { dialog, multiselect, listbox } = await openMultiSelectFilter(
        user,
        'Event',
      );

      await selectOptions(user, listbox, [
        'REGISTERED',
        'DEREGISTERED',
        'STATUS_CHANGED',
      ]);
      await applyFilter(user, dialog, multiselect);

      const eventTypes = getVisibleColumnValues(4);

      expect(eventTypes).toHaveLength(4);
      expect(eventTypes).toEqual(
        expect.arrayContaining([
          'REGISTERED',
          'DEREGISTERED',
          'STATUS_CHANGED',
        ]),
      );
      expect(eventTypes).not.toContain('INFO_CHANGED');
      expect(eventTypes).not.toContain('REGISTRATION_UPDATED');
    });

    it('preselects filters from route query on mount and shows selected values', async () => {
      const user = userEvent.setup();

      await renderJournalTableWithRouter({
        events,
        applications,
        query: {
          q: 'Alpha',
          application: ['Alpha App'],
          instanceId: ['instance-2'],
          type: ['INFO_CHANGED'],
        },
      });

      expect(screen.getByRole('textbox')).toHaveValue('Alpha');

      expect(getVisibleColumnValues(1)).toEqual(['Alpha App']);
      expect(getVisibleColumnValues(2)).toEqual(['instance-2']);
      expect(getVisibleColumnValues(4)).toEqual(['INFO_CHANGED']);

      await expectPreselectedOption(user, 'Application', 'Alpha App');
      await expectPreselectedOption(user, 'Instance', 'instance-2');
      await expectPreselectedOption(user, 'Event', 'INFO_CHANGED');
    });
  });
});

function getColumnFilterButton(headerText: string) {
  const header = screen.getByText(headerText);
  const headerCell = header.closest('th');

  if (!headerCell) {
    throw new Error(`Header cell for ${headerText} not found`);
  }

  return within(headerCell).getByRole('button', { name: /Filter Menu/ });
}

function getVisibleColumnValues(columnIndex: number) {
  return getVisibleRows()
    .map((row) =>
      within(row).queryAllByRole('cell')[columnIndex]?.textContent?.trim(),
    )
    .filter(Boolean);
}

function getVisibleRows() {
  return screen.getAllByRole('row').slice(1);
}

function getOption(listbox: HTMLElement, name: string) {
  return within(listbox).getByRole('option', { name });
}

function expectOptionSelected(listbox: HTMLElement, name: string) {
  expect(getOption(listbox, name)).toHaveAttribute('aria-selected', 'true');
}

function getMultiSelectTrigger(container: HTMLElement) {
  const combobox = within(container).getByRole('combobox');
  const trigger = combobox.closest('[data-pc-name="multiselect"]');

  if (!(trigger instanceof HTMLElement)) {
    throw new Error('MultiSelect trigger not found');
  }

  return trigger;
}

async function openMultiSelectFilter(
  user: ReturnType<typeof userEvent.setup>,
  headerText: string,
) {
  await user.click(getColumnFilterButton(headerText));

  const dialog = await screen.findByRole('dialog');
  const multiselect = getMultiSelectTrigger(dialog);

  await user.click(multiselect);

  return {
    dialog,
    multiselect,
    listbox: await screen.findByRole('listbox'),
  };
}

async function selectOption(
  user: ReturnType<typeof userEvent.setup>,
  listbox: HTMLElement,
  optionName: string,
) {
  await user.click(getOption(listbox, optionName));
}

async function selectOptions(
  user: ReturnType<typeof userEvent.setup>,
  listbox: HTMLElement,
  optionNames: string[],
) {
  for (const optionName of optionNames) {
    await selectOption(user, listbox, optionName);
  }
}

async function applyFilter(
  user: ReturnType<typeof userEvent.setup>,
  dialog: HTMLElement,
  multiselect: HTMLElement,
) {
  await user.click(multiselect);
  await user.click(within(dialog).getByRole('button', { name: 'Apply' }));
}

async function expectPreselectedOption(
  user: ReturnType<typeof userEvent.setup>,
  headerText: string,
  optionName: string,
) {
  const { multiselect, listbox } = await openMultiSelectFilter(
    user,
    headerText,
  );

  expectOptionSelected(listbox, optionName);

  await user.click(multiselect);
  await user.click(getColumnFilterButton(headerText));
}

async function renderJournalTableWithRouter({
  events,
  applications,
  query = {},
}: {
  events: InstanceEvent[];
  applications: Application[];
  query?: LocationQueryRaw;
}) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ path: '/', component: JournalTable }],
  });

  await router.push({ path: '/', query });
  await router.isReady();

  return tlRender(JournalTable, {
    props: {
      events,
      applications,
    },
    global: {
      plugins: [
        router,
        createI18n({
          locale: 'en',
          messages: {
            en: terms,
          },
          legacy: false,
          fallbackWarn: false,
          missingWarn: false,
        }),
        NotificationcenterPlugin,
        SbaModalPlugin,
        [
          PrimeVue,
          {
            theme: {
              options: {
                darkModeSelector: false,
              },
            },
          },
        ],
        components,
      ],
      directives: {
        tooltip: Tooltip,
      },
      stubs: {
        RouterLink: RouterLinkStub,
        'sba-exchanges-chart': true,
        'font-awesome-icon': {
          template: '<svg data-testid="icon" />',
        },
      },
    },
  });
}
