import {rest, setupWorker} from 'msw'
import {handlers} from '@/handlers';

export const worker = setupWorker(
  ...handlers,
  rest.post(
    '*/actuator/shutdown',
    (req, res, ctx) => {
      return res(ctx.status(403), ctx.json({}));
    }
  ),
)
