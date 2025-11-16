import { setupWorker } from 'msw/browser';

import auditEventsEndpoint from './instance/auditevents/index';
import flywayEndpoints from './instance/flyway/index';
import httpTraceEndpoints from './instance/httptrace/index';
import liquibaseEndpoints from './instance/liquibase/index';
import mappingsEndpoint from './instance/mappings/index';
import metricsEndpoint from './instance/metrics/index';
import sessionEndpoints from './instance/sessions/index';

const handler = [
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
  ...metricsEndpoint,
  ...httpTraceEndpoints,
  ...sessionEndpoints,
];

export const worker = setupWorker(...handler);
