import { rest } from 'msw';

const infoEndpoint = [
  rest.get('/instances/:instanceId/actuator/info', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json({}));
  }),
];

export default infoEndpoint;
