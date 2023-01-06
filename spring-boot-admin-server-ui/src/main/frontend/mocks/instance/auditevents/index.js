import { rest } from 'msw';

import data from './data.js';

const endpoints = [
  rest.get('/instances/:instanceId/actuator/auditevents', (req, res, ctx) => {
    console.log('[Auditevents] received request.');
    return res(ctx.status(200), ctx.json(data));
  }),
];

export default endpoints;
