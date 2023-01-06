import { rest } from 'msw';

import { applications } from './data.js';

const applicationsEndpoint = [
  rest.get('/applications', (req, res, ctx) => {
    return res(ctx.status(404), ctx.json(applications));
  }),
];

export default applicationsEndpoint;
