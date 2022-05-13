import {setupWorker} from 'msw'
import mappingsEndpoint from "./instance/mappings/index.js";
import liquibaseEndpoints from "./instance/liquibase/index.js";
import flywayEndpoints from "./instance/flyway/index.js";

const handler = [
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints
];

export const worker = setupWorker(...handler)
