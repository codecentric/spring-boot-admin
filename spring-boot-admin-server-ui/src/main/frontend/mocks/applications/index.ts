import { http } from 'msw';

import { applications } from './data.js';

const applicationsEndpoint = [
  http.get('/applications', () => {
    return res(ctx.status(404), ctx.json(applications));
  }),
];

export default applicationsEndpoint;
