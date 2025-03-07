import { setupWorker } from 'msw/browser';

import auditEventsEndpoint from './instance/auditevents/index.js';
import flywayEndpoints from './instance/flyway/index.js';
import liquibaseEndpoints from './instance/liquibase/index.js';
import mappingsEndpoint from './instance/mappings/index.js';
import metricsEndpoint from './instance/metrics/index.js';

const handler = [
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
  ...metricsEndpoint,
];

export const worker = setupWorker(...handler);
