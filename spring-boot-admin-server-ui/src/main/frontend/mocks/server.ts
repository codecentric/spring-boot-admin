import { setupServer } from 'msw/node';

import auditEventsEndpoint from '@/mocks/instance/auditevents';
import flywayEndpoints from '@/mocks/instance/flyway';
import healthEndpoint from '@/mocks/instance/health';
import infoEndpoint from '@/mocks/instance/info';
import jolokiaEndpoint from '@/mocks/instance/jolokia';
import liquibaseEndpoints from '@/mocks/instance/liquibase';
import mappingsEndpoint from '@/mocks/instance/mappings';

const handler = [
  ...infoEndpoint,
  ...healthEndpoint,
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
  ...jolokiaEndpoint,
];

export const server = setupServer(...handler);
