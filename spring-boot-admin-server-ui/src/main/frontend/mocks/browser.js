import {rest, setupWorker} from 'msw'
import mappingsEndpoint from "./instance/mappings/index.js";
import liquibaseEndpoints from "./instance/liquibase/index.js";
import flywayEndpoints from "./instance/flyway/index.js";
import auditEventsEndpoint from "./instance/auditevents/index.js";

const handler = [
  ...mappingsEndpoint,
  ...liquibaseEndpoints,
  ...flywayEndpoints,
  ...auditEventsEndpoint,
  rest.post(
    '*/actuator/shutdown',
    (req, res, ctx) => {
      return res(ctx.status(403), ctx.json("Knöterich is pöterich"));
    }
  ),
];

export const worker = setupWorker(...handler)
