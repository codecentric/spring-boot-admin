export const liquibase = {
  contexts: {
    application: {
      liquibaseBeans: {
        liquibase: {
          changeSets: [
            {
              author: 'marceloverdijk',
              changeLog: 'db/changelog/db.changelog-master.yaml',
              comments: '',
              contexts: ['context1'],
              dateExecuted: '2022-04-21T08:50:32.817Z',
              deploymentId: '0531032536',
              description: 'createTable tableName=customer',
              execType: 'EXECUTED',
              id: '1',
              labels: ['label1', 'label2'],
              checksum: '8:46debf252cce6d7b25e28ddeb9fc4bf6',
              orderExecuted: 1,
            },
          ],
        },
      },
    },
  },
};
