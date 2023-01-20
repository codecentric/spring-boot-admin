import { rest } from 'msw';

import { auditeventsresponse } from '@/mocks/instance/auditevents/data';

const endpoints = [
  rest.get('/instances/:instanceId/actuator/auditevents', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(auditeventsresponse));
  }),
];

export default endpoints;
