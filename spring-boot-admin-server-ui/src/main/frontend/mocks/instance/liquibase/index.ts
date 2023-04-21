import { rest } from 'msw';

import { liquibase } from './data.js';

const liquibaseEndpoints = [
  rest.get('/instances/:instanceId/actuator/liquibase', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(liquibase));
  }),
];

export default liquibaseEndpoints;
