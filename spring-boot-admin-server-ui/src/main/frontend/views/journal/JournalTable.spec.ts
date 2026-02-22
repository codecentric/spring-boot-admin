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
import userEvent from '@testing-library/user-event';
import { screen, within } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import JournalTable from './JournalTable.vue';

import Application from '@/services/application';
import { render } from '@/test-utils';
import {
  InstanceEvent,
  InstanceEventType,
} from '@/views/journal/InstanceEvent';

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
      let rows = screen.getAllByRole('row');
      let dataRows = rows.slice(1);
      expect(dataRows).toHaveLength(8);

      // Find and click the application column filter button
      const applicationHeader = screen.getByText('Application');
      const filterButton = applicationHeader
        .closest('th')
        ?.querySelector('[data-pc-section="columnfilter"]');

      if (filterButton) {
        await user.click(filterButton);

        // Find and select "Alpha App" from the MultiSelect dropdown
        const alphaOption = await screen.findByText('Alpha App');
        await user.click(alphaOption);

        // Close the filter menu by clicking outside or on the filter button again
        await user.click(filterButton);

        // Get rows after filtering
        rows = screen.getAllByRole('row');
        dataRows = rows.slice(1);

        // Extract application names from visible rows
        const applicationNames = dataRows
          .map((row) => {
            const cells = within(row).queryAllByRole('cell');
            return cells[1]?.textContent?.trim();
          })
          .filter(Boolean);

        // Verify only "Alpha App" events are shown
        expect(applicationNames.every((name) => name === 'Alpha App')).toBe(
          true,
        );
        expect(dataRows).toHaveLength(2);
      }
    });

    it('should filter by multiple applications', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find and click the application column filter button
      const applicationHeader = screen.getByText('Application');
      const filterButton = applicationHeader
        .closest('th')
        ?.querySelector('[data-pc-section="columnfilter"]');

      if (filterButton) {
        await user.click(filterButton);

        // Select multiple applications
        const alphaOption = await screen.findByText('Alpha App');
        await user.click(alphaOption);

        const betaOption = await screen.findByText('Beta App');
        await user.click(betaOption);

        const gammaOption = await screen.findByText('Gamma App');
        await user.click(gammaOption);

        // Close the filter
        await user.click(filterButton);

        // Get rows after filtering
        const rows = screen.getAllByRole('row');
        const dataRows = rows.slice(1);

        // Extract application names
        const applicationNames = dataRows
          .map((row) => {
            const cells = within(row).queryAllByRole('cell');
            return cells[1]?.textContent?.trim();
          })
          .filter(Boolean);

        // Verify only selected apps are shown
        expect(dataRows).toHaveLength(4);
        expect(applicationNames).toContain('Alpha App');
        expect(applicationNames).toContain('Beta App');
        expect(applicationNames).toContain('Gamma App');
        expect(applicationNames).not.toContain('Delta App');
        expect(applicationNames).not.toContain('Epsilon App');
        expect(applicationNames).not.toContain('Theta App');
        expect(applicationNames).not.toContain('Zebra App');
      }
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

      // Get all rows before filtering
      let rows = screen.getAllByRole('row');
      let dataRows = rows.slice(1);
      expect(dataRows).toHaveLength(8);

      // Find and click the instance column filter button
      const instanceHeader = screen.getByText('Instance');
      const filterButton = instanceHeader
        .closest('th')
        ?.querySelector('[data-pc-section="columnfilter"]');

      if (filterButton) {
        await user.click(filterButton);

        // Find and select "instance-2" from the MultiSelect dropdown
        const instance2Option = await screen.findByText('instance-2');
        await user.click(instance2Option);

        // Close the filter menu
        await user.click(filterButton);

        // Get rows after filtering
        rows = screen.getAllByRole('row');
        dataRows = rows.slice(1);

        // Extract instance IDs from visible rows
        const instanceIds = dataRows
          .map((row) => {
            const cells = within(row).queryAllByRole('cell');
            return cells[2]?.textContent?.trim();
          })
          .filter(Boolean);

        // Verify only "instance-2" events are shown
        expect(instanceIds.every((id) => id === 'instance-2')).toBe(true);
        expect(dataRows).toHaveLength(2);
      }
    });

    it('should filter by multiple instances', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find and click the instance column filter button
      const instanceHeader = screen.getByText('Instance');
      const filterButton = instanceHeader
        .closest('th')
        ?.querySelector('[data-pc-section="columnfilter"]');

      if (filterButton) {
        await user.click(filterButton);

        // Select multiple instances
        const instance1Option = await screen.findByText('instance-1');
        await user.click(instance1Option);

        const instance3Option = await screen.findByText('instance-3');
        await user.click(instance3Option);

        const instance5Option = await screen.findByText('instance-5');
        await user.click(instance5Option);

        // Close the filter
        await user.click(filterButton);

        // Get rows after filtering
        const rows = screen.getAllByRole('row');
        const dataRows = rows.slice(1);

        // Extract instance IDs
        const instanceIds = dataRows
          .map((row) => {
            const cells = within(row).queryAllByRole('cell');
            return cells[2]?.textContent?.trim();
          })
          .filter(Boolean);

        // Verify only selected instances are shown
        expect(dataRows).toHaveLength(3);
        expect(instanceIds).toContain('instance-1');
        expect(instanceIds).toContain('instance-3');
        expect(instanceIds).toContain('instance-5');
        expect(instanceIds).not.toContain('instance-2');
        expect(instanceIds).not.toContain('instance-4');
        expect(instanceIds).not.toContain('instance-6');
        expect(instanceIds).not.toContain('instance-7');
      }
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

      // Get all rows before filtering
      let rows = screen.getAllByRole('row');
      let dataRows = rows.slice(1);
      expect(dataRows).toHaveLength(8);

      // Find and click the event column filter button
      const eventHeader = screen.getByText('Event');
      const filterButton = eventHeader
        .closest('th')
        ?.querySelector('[data-pc-section="columnfilter"]');

      if (filterButton) {
        await user.click(filterButton);

        // Find and select "INFO_CHANGED" from the MultiSelect dropdown
        const infoChangedOption = await screen.findByText('INFO_CHANGED');
        await user.click(infoChangedOption);

        // Close the filter menu
        await user.click(filterButton);

        // Get rows after filtering
        rows = screen.getAllByRole('row');
        dataRows = rows.slice(1);

        // Extract event types from visible rows
        const eventTypes = dataRows
          .map((row) => {
            const cells = within(row).queryAllByRole('cell');
            return cells[4]?.textContent?.trim();
          })
          .filter(Boolean);

        // Verify only "INFO_CHANGED" events are shown
        expect(eventTypes.every((type) => type === 'INFO_CHANGED')).toBe(true);
        expect(dataRows).toHaveLength(2);
      }
    });

    it('should filter by multiple event types', async () => {
      const user = userEvent.setup();

      render(JournalTable, {
        props: {
          events,
          applications,
        },
      });

      // Find and click the event column filter button
      const eventHeader = screen.getByText('Event');
      const filterButton = eventHeader
        .closest('th')
        ?.querySelector('[data-pc-section="columnfilter"]');

      if (filterButton) {
        await user.click(filterButton);

        // Select multiple event types
        const registeredOption = await screen.findByText('REGISTERED');
        await user.click(registeredOption);

        const deregisteredOption = await screen.findByText('DEREGISTERED');
        await user.click(deregisteredOption);

        const statusChangedOption = await screen.findByText('STATUS_CHANGED');
        await user.click(statusChangedOption);

        // Close the filter
        await user.click(filterButton);

        // Get rows after filtering
        const rows = screen.getAllByRole('row');
        const dataRows = rows.slice(1);

        // Extract event types
        const eventTypes = dataRows
          .map((row) => {
            const cells = within(row).queryAllByRole('cell');
            return cells[4]?.textContent?.trim();
          })
          .filter(Boolean);

        // Verify only selected event types are shown
        expect(dataRows).toHaveLength(4);
        expect(eventTypes).toContain('REGISTERED');
        expect(eventTypes).toContain('DEREGISTERED');
        expect(eventTypes).toContain('STATUS_CHANGED');
        expect(eventTypes).not.toContain('INFO_CHANGED');
        expect(eventTypes).not.toContain('REGISTRATION_UPDATED');
        expect(eventTypes).not.toContain('ENDPOINTS_DETECTED');
      }
    });
  });
});
