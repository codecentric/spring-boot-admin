import { setupWorker } from 'msw';

import auditEventsEndpoint from './instance/auditevents/index.js';
import flywayEndpoints from './instance/flyway/index.js';
import liquibaseEndpoints from './instance/liquibase/index.js';
import mappingsEndpoint from './instance/mappings/index.js';

const handler = [
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
];

export const worker = setupWorker(...handler);
