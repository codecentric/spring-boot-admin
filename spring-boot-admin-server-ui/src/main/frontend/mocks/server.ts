import { setupServer } from 'msw/node';

import auditEventsEndpoint from '@/mocks/instance/auditevents';
import dependenciesEndpoints from '@/mocks/instance/dependencies';
import flywayEndpoints from '@/mocks/instance/flyway';
import healthEndpoint from '@/mocks/instance/health';
import infoEndpoint from '@/mocks/instance/info';
import jolokiaEndpoint from '@/mocks/instance/jolokia';
import liquibaseEndpoints from '@/mocks/instance/liquibase';
import mappingsEndpoint from '@/mocks/instance/mappings';
import metricsEntpoints from '@/mocks/instance/metrics';

const handler = [
  ...infoEndpoint,
  ...healthEndpoint,
  ...mappingsEndpoint,
  ...metricsEntpoints,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
  ...jolokiaEndpoint,
  ...dependenciesEndpoints,
];

export const server = setupServer(...handler);
