import { setupWorker } from 'msw/browser';

import auditEventsEndpoint from './instance/auditevents/index.js';
import flywayEndpoints from './instance/flyway/index.js';
import httpTraceEndpoints from './instance/httptrace/index.js';
import liquibaseEndpoints from './instance/liquibase/index.js';
import mappingsEndpoint from './instance/mappings/index.js';
import metricsEndpoint from './instance/metrics/index.js';
import sessionEndpoints from './instance/sessions/index.js';

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
