export const flyway = {
  contexts: {
    application: {
      flywayBeans: {
        flyway: {
          migrations: [
            {
              type: 'SQL',
              checksum: -156244537,
              version: '1',
              description: 'init',
              script: 'V1__init.sql',
              state: 'SUCCESS',
              installedBy: 'SA',
              installedOn: '2022-04-21T08:50:41.580Z',
              installedRank: 1,
              executionTime: 3,
            },
          ],
        },
      },
    },
  },
};
