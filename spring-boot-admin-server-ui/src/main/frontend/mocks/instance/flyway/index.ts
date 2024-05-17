import { HttpResponse, http } from 'msw';

import { flyway } from './data.js';

const flywayEndpoints = [
  http.get('/instances/:instanceId/actuator/flyway', () => {
    return HttpResponse.json(flyway);
  }),
];

export default flywayEndpoints;
