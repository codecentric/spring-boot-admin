import {screen} from '@testing-library/vue';
import {beforeEach, describe, expect, it} from 'vitest';

import {applications} from '@/mocks/applications/data';
import Application from '@/services/application';
import {render} from '@/test-utils';
import Dependencies from '@/views/instances/dependencies/index.vue';
import userEvent from '@testing-library/user-event';

describe('Dependencies', () => {

  const GROUP_TABLE_COLUMN_INDEX = 0;
  const NAME_TABLE_COLUMN_INDEX = 1;
  const VERSION_TABLE_COLUMN_INDEX = 2;
  const LICENSES_TABLE_COLUMN_INDEX = 3;
  const PUBLISHER_TABLE_COLUMN_INDEX = 4;
  const DESCRIPTION_TABLE_COLUMN_INDEX = 5;

  beforeEach(async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(Dependencies, {
      props: {
        instance,
      },
    });
  });

  describe('Render correctly', () => {
    it('both sbom ids', async () => {
      expect(
        await screen.findByText(/application \(63\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(63\/63\)/),
      ).toBeVisible();
    });

    it('table shows correct information', async () => {
      // header order
      const tableHeaders = await screen.findAllByTestId('sbom-table-header');
      tableHeaders.forEach((tableHeader) => {
        // only one header row
        expect(tableHeader.children.length).toBe(1);

        // having exactly this order
        expect(tableHeader.children[0].children[GROUP_TABLE_COLUMN_INDEX].textContent).toContain('instances.dependencies.list.header.group');
        expect(tableHeader.children[0].children[NAME_TABLE_COLUMN_INDEX].textContent).toContain('instances.dependencies.list.header.name');
        expect(tableHeader.children[0].children[VERSION_TABLE_COLUMN_INDEX].textContent).toContain('instances.dependencies.list.header.version');
        expect(tableHeader.children[0].children[LICENSES_TABLE_COLUMN_INDEX].textContent).toContain('instances.dependencies.list.header.licenses');
        expect(tableHeader.children[0].children[PUBLISHER_TABLE_COLUMN_INDEX].textContent).toContain('instances.dependencies.list.header.publisher');
        expect(tableHeader.children[0].children[DESCRIPTION_TABLE_COLUMN_INDEX].textContent).toContain('instances.dependencies.list.header.description');
      });

      // columns should be filled with values matching headers
      const log4jRows = await screen.findAllByText('log4j-api');
      log4jRows.forEach((row) => {
        const log4jRow = row.parentElement
        expect(
          log4jRow.children[GROUP_TABLE_COLUMN_INDEX].textContent
        ).toContain('org.apache.logging.log4j');
        expect(
          log4jRow.children[NAME_TABLE_COLUMN_INDEX].textContent
        ).toContain('log4j-api');
        expect(
          log4jRow.children[VERSION_TABLE_COLUMN_INDEX].textContent
        ).toContain('2.23.1');
        expect(
          log4jRow.children[LICENSES_TABLE_COLUMN_INDEX].textContent
        ).toContain('Apache-2.0');
        expect(
          log4jRow.children[PUBLISHER_TABLE_COLUMN_INDEX].textContent
        ).toContain('The Apache Software Foundation');
        expect(
          log4jRow.children[DESCRIPTION_TABLE_COLUMN_INDEX].textContent
        ).toContain('The Apache Log4j API');
      });
    });
  });

  describe('filter correctly', () => {
    it('for dependency name', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'log4j-api');

      expect(
        await screen.findByText(/application \(1\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(1\/63\)/),
      ).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(
          tbody.children.length
        ).toBe(1);
      });
    });

    it('for dependency group', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'org.apache.logging.log4j');

      expect(
        await screen.findByText(/application \(2\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(2\/63\)/),
      ).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(
          tbody.children.length
        ).toBe(2);
      });
    });

    it('for dependency version', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, '2.17.0');

      expect(
        await screen.findByText(/application \(6\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(6\/63\)/),
      ).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(
          tbody.children.length
        ).toBe(6);
      });
    });

    it('for dependency license', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'BSD-3-Clause');

      expect(
        await screen.findByText(/application \(3\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(3\/63\)/),
      ).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(
          tbody.children.length
        ).toBe(3);
      });
    });

    it('for dependency publisher', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'Eclipse Foundation');

      expect(
        await screen.findByText(/application \(4\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(4\/63\)/),
      ).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(
          tbody.children.length
        ).toBe(4);
      });
    });
  });
});
