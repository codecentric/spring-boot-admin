import { rest } from 'msw';

import { jolokiaList } from './data';

import { jolokiaRead } from '@/mocks/instance/jolokia/data.read';

const jolokiaEndpoint = [
  rest.get('/instances/:instanceId/actuator/jolokia/list', (req, res, ctx) => {
    return res(ctx.status(200), ctx.json(jolokiaList));
  }),
  rest.post(
    '/instances/:instanceId/actuator/jolokia',
    async (req, res, ctx) => {
      try {
        let body = await req.json();
        if (body.type === 'read') {
          return res(ctx.status(200), ctx.json(jolokiaRead));
        }
      } catch (e) {
        console.error(e);
      }
    }
  ),
];

export default jolokiaEndpoint;
