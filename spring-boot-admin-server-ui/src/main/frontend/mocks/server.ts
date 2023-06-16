import { setupServer } from 'msw/node';

import auditEventsEndpoint from '@/mocks/instance/auditevents';
import flywayEndpoints from '@/mocks/instance/flyway';
import jolokiaEndpoint from '@/mocks/instance/jolokia';
import liquibaseEndpoints from '@/mocks/instance/liquibase';
import mappingsEndpoint from '@/mocks/instance/mappings/index.js';

const handler = [
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
  ...jolokiaEndpoint,
];

export const server = setupServer(...handler);
