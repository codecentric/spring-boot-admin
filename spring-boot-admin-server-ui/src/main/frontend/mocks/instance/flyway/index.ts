import { rest } from 'msw';

import { flyway } from './data.js';

const flywayEndpoints = [
  rest.get('/instances/:instanceId/actuator/flyway', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(flyway));
  }),
];

export default flywayEndpoints;
