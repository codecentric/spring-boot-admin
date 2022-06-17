import { rest } from 'msw';
import { mappings } from './data';
import { auditeventsresponse } from './auditevents_data';

const mappingsEndpoint = [
  rest.get(
    '/instances/:instanceId/actuator/mappings',
    (req, res, ctx) => {
      return res(ctx.status(200), ctx.json(mappings));
    }
  ),
  rest.get(
    '/instances/:instanceId/actuator/auditevents',
    (req, res, ctx) => {
      return res(ctx.status(200), ctx.json(auditeventsresponse));
    }
  ),
];

export default mappingsEndpoint;
