import userEvent from '@testing-library/user-event';
import { screen } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';
import Dependencies from '@/views/instances/dependencies/index.vue';

describe('Dependencies', () => {
  const GROUP_TABLE_COLUMN_INDEX = 0;
  const NAME_TABLE_COLUMN_INDEX = 1;
  const VERSION_TABLE_COLUMN_INDEX = 2;
  const LICENSES_TABLE_COLUMN_INDEX = 3;
  const PUBLISHER_TABLE_COLUMN_INDEX = 4;
  const DESCRIPTION_TABLE_COLUMN_INDEX = 5;

  const application = new Application(applications[0]);
  const instance = application.instances[0];

  describe('Render correctly', () => {
    beforeEach(() => {
      render(Dependencies, {
        props: {
          instance,
        },
      });
    });

    it('both sbom ids', async () => {
      expect(await screen.findByText(/application \(63\/63\)/)).toBeVisible();

      expect(await screen.findByText(/system \(63\/63\)/)).toBeVisible();
    });

    it('table shows correct information', async () => {
      // header order
      const tableHeaders = await screen.findAllByTestId('sbom-table-header');
      tableHeaders.forEach((tableHeader) => {
        // only one header row
        expect(tableHeader.children.length).toBe(1);

        // having exactly this order
        expect(
          tableHeader.children[0].children[GROUP_TABLE_COLUMN_INDEX]
            .textContent,
        ).toContain('instances.dependencies.list.header.group');
        expect(
          tableHeader.children[0].children[NAME_TABLE_COLUMN_INDEX].textContent,
        ).toContain('instances.dependencies.list.header.name');
        expect(
          tableHeader.children[0].children[VERSION_TABLE_COLUMN_INDEX]
            .textContent,
        ).toContain('instances.dependencies.list.header.version');
        expect(
          tableHeader.children[0].children[LICENSES_TABLE_COLUMN_INDEX]
            .textContent,
        ).toContain('instances.dependencies.list.header.licenses');
        expect(
          tableHeader.children[0].children[PUBLISHER_TABLE_COLUMN_INDEX]
            .textContent,
        ).toContain('instances.dependencies.list.header.publisher');
        expect(
          tableHeader.children[0].children[DESCRIPTION_TABLE_COLUMN_INDEX]
            .textContent,
        ).toContain('instances.dependencies.list.header.description');
      });

      // columns should be filled with values matching headers
      const log4jRows = await screen.findAllByText('log4j-api');
      log4jRows.forEach((row) => {
        const log4jRow = row.parentElement;
        expect(
          log4jRow.children[GROUP_TABLE_COLUMN_INDEX].textContent,
        ).toContain('org.apache.logging.log4j');
        expect(
          log4jRow.children[NAME_TABLE_COLUMN_INDEX].textContent,
        ).toContain('log4j-api');
        expect(
          log4jRow.children[VERSION_TABLE_COLUMN_INDEX].textContent,
        ).toContain('2.23.1');
        expect(
          log4jRow.children[LICENSES_TABLE_COLUMN_INDEX].textContent,
        ).toContain('Apache-2.0');
        expect(
          log4jRow.children[PUBLISHER_TABLE_COLUMN_INDEX].textContent,
        ).toContain('The Apache Software Foundation');
        expect(
          log4jRow.children[DESCRIPTION_TABLE_COLUMN_INDEX].textContent,
        ).toContain('The Apache Log4j API');
      });
    });
  });

  describe('filter correctly', () => {
    beforeEach(() => {
      render(Dependencies, {
        props: {
          instance,
        },
      });
    });

    it('for dependency name', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'log4j-api');

      expect(await screen.findByText(/application \(1\/63\)/)).toBeVisible();

      expect(await screen.findByText(/system \(1\/63\)/)).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(tbody.children.length).toBe(1);
      });
    });

    it('for dependency group', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'org.apache.logging.log4j');

      expect(await screen.findByText(/application \(2\/63\)/)).toBeVisible();

      expect(await screen.findByText(/system \(2\/63\)/)).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(tbody.children.length).toBe(2);
      });
    });

    it('for dependency version', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, '2.17.0');

      expect(await screen.findByText(/application \(6\/63\)/)).toBeVisible();

      expect(await screen.findByText(/system \(6\/63\)/)).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(tbody.children.length).toBe(6);
      });
    });

    it('for dependency license', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'BSD-3-Clause');

      expect(await screen.findByText(/application \(3\/63\)/)).toBeVisible();

      expect(await screen.findByText(/system \(3\/63\)/)).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(tbody.children.length).toBe(3);
      });
    });

    it('for dependency publisher', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'Eclipse Foundation');

      expect(await screen.findByText(/application \(4\/63\)/)).toBeVisible();

      expect(await screen.findByText(/system \(4\/63\)/)).toBeVisible();

      (await screen.findAllByTestId('sbom-table-body')).forEach((tbody) => {
        expect(tbody.children.length).toBe(4);
      });
    });
  });

  describe('sort correctly', () => {
    beforeEach(() => {
      server.use(
        http.get('/instances/:instanceId/actuator/sbom', () => {
          return HttpResponse.json({
            ids: ['application'],
          });
        }),
        http.get('/instances/:instanceId/actuator/sbom/application', () => {
          return HttpResponse.json({
            components: [
              {
                publisher: 'CCC',
                group: 'c.cccc.ccc',
                name: 'ccc',
                version: '3.0.0',
                description: 'C description',
                licenses: [
                  {
                    license: {
                      id: 'C',
                    },
                  },
                ],
              },
              {
                publisher: 'AAA',
                group: 'a.aaaa.aaa',
                name: 'aaa',
                version: '1.0.0',
                description: 'A description',
                licenses: [
                  {
                    license: {
                      id: 'Apache-2.0',
                    },
                  },
                ],
              },
              {
                publisher: 'BBB',
                group: 'b.bbbb.bbb',
                name: 'bbb',
                version: '2.0.0',
                description: 'B description',
                licenses: [
                  {
                    license: {
                      id: 'BSD',
                    },
                  },
                ],
              },
            ],
          });
        }),
      );

      render(Dependencies, {
        props: {
          instance,
        },
      });
    });

    const resetDefaultSort = async () => {
      for (const header of await screen.findAllByTestId('sbom-table-header')) {
        const tableHeaderColumns = header.children[0].children;
        await userEvent.click(tableHeaderColumns[GROUP_TABLE_COLUMN_INDEX]);
        await userEvent.click(tableHeaderColumns[GROUP_TABLE_COLUMN_INDEX]);
        await userEvent.click(tableHeaderColumns[NAME_TABLE_COLUMN_INDEX]);
        await userEvent.click(tableHeaderColumns[NAME_TABLE_COLUMN_INDEX]);
        await userEvent.click(tableHeaderColumns[VERSION_TABLE_COLUMN_INDEX]);
        await userEvent.click(tableHeaderColumns[VERSION_TABLE_COLUMN_INDEX]);
      }

      expect(
        await screen.queryByTestId('sorted-icon-group-ASC'),
      ).not.toBeInTheDocument();
      expect(
        await screen.queryByTestId('sorted-icon-name-ASC'),
      ).not.toBeInTheDocument();
      expect(
        await screen.queryByTestId('sorted-icon-version-ASC'),
      ).not.toBeInTheDocument();
    };

    it('initial sort by group, name and version', async () => {
      expect(
        (await screen.findAllByTestId('sorted-icon-group-ASC'))[0],
      ).toBeVisible();
      expect(
        (await screen.findAllByTestId('sorted-icon-name-ASC'))[0],
      ).toBeVisible();
      expect(
        (await screen.findAllByTestId('sorted-icon-version-ASC'))[0],
      ).toBeVisible();

      const dependencyRows = await screen.findAllByTestId(
        'sbom-table-body-row',
      );
      expect(
        dependencyRows[0].children[GROUP_TABLE_COLUMN_INDEX].textContent,
      ).toContain('a.aaaa.aaa');
      expect(
        dependencyRows[0].children[NAME_TABLE_COLUMN_INDEX].textContent,
      ).toContain('aaa');
      expect(
        dependencyRows[0].children[VERSION_TABLE_COLUMN_INDEX].textContent,
      ).toContain('1.0.0');

      expect(
        dependencyRows[1].children[GROUP_TABLE_COLUMN_INDEX].textContent,
      ).toContain('b.bbbb.bbb');
      expect(
        dependencyRows[1].children[NAME_TABLE_COLUMN_INDEX].textContent,
      ).toContain('bbb');
      expect(
        dependencyRows[1].children[VERSION_TABLE_COLUMN_INDEX].textContent,
      ).toContain('2.0.0');

      expect(
        dependencyRows[2].children[GROUP_TABLE_COLUMN_INDEX].textContent,
      ).toContain('c.cccc.ccc');
      expect(
        dependencyRows[2].children[NAME_TABLE_COLUMN_INDEX].textContent,
      ).toContain('ccc');
      expect(
        dependencyRows[2].children[VERSION_TABLE_COLUMN_INDEX].textContent,
      ).toContain('3.0.0');
    });

    it.each`
      property       | tableColumnIndex                | expectedValues
      ${'group'}     | ${GROUP_TABLE_COLUMN_INDEX}     | ${['a.aaaa.aaa', 'b.bbbb.bbb', 'c.cccc.ccc']}
      ${'name'}      | ${NAME_TABLE_COLUMN_INDEX}      | ${['aaa', 'bbb', 'ccc']}
      ${'version'}   | ${VERSION_TABLE_COLUMN_INDEX}   | ${['1.0.0', '2.0.0', '3.0.0']}
      ${'publisher'} | ${PUBLISHER_TABLE_COLUMN_INDEX} | ${['AAA', 'BBB', 'CCC']}
    `(
      'by $property ASC',
      async ({ property, tableColumnIndex, expectedValues }) => {
        await resetDefaultSort();

        // activate sort ASC
        const tableHeaderColumns = (
          await screen.findAllByTestId('sbom-table-header')
        )[0].children[0].children;
        await userEvent.click(tableHeaderColumns[tableColumnIndex]);

        expect(
          (await screen.findAllByTestId(`sorted-icon-${property}-ASC`))[0],
        ).toBeVisible();

        // check sort
        const dependencyRows = await screen.findAllByTestId(
          'sbom-table-body-row',
        );

        expect(
          dependencyRows[0].children[tableColumnIndex].textContent,
        ).toContain(expectedValues[0]);
        expect(
          dependencyRows[1].children[tableColumnIndex].textContent,
        ).toContain(expectedValues[1]);
        expect(
          dependencyRows[2].children[tableColumnIndex].textContent,
        ).toContain(expectedValues[2]);
      },
    );

    it.each`
      property       | tableColumnIndex                | expectedValues
      ${'group'}     | ${GROUP_TABLE_COLUMN_INDEX}     | ${['a.aaaa.aaa', 'b.bbbb.bbb', 'c.cccc.ccc'].reverse()}
      ${'name'}      | ${NAME_TABLE_COLUMN_INDEX}      | ${['aaa', 'bbb', 'ccc'].reverse()}
      ${'version'}   | ${VERSION_TABLE_COLUMN_INDEX}   | ${['1.0.0', '2.0.0', '3.0.0'].reverse()}
      ${'publisher'} | ${PUBLISHER_TABLE_COLUMN_INDEX} | ${['AAA', 'BBB', 'CCC'].reverse()}
    `(
      'by $property DESC',
      async ({ property, tableColumnIndex, expectedValues }) => {
        await resetDefaultSort();

        // activate sort DESC
        const tableHeaderColumns = (
          await screen.findAllByTestId('sbom-table-header')
        )[0].children[0].children;
        await userEvent.click(tableHeaderColumns[tableColumnIndex]);
        await userEvent.click(tableHeaderColumns[tableColumnIndex]);

        expect(
          (await screen.findAllByTestId(`sorted-icon-${property}-DESC`))[0],
        ).toBeVisible();

        // check sort
        const dependencyRows = await screen.findAllByTestId(
          'sbom-table-body-row',
        );

        expect(
          dependencyRows[0].children[tableColumnIndex].textContent,
        ).toContain(expectedValues[0]);
        expect(
          dependencyRows[1].children[tableColumnIndex].textContent,
        ).toContain(expectedValues[1]);
        expect(
          dependencyRows[2].children[tableColumnIndex].textContent,
        ).toContain(expectedValues[2]);
      },
    );
  });
});
