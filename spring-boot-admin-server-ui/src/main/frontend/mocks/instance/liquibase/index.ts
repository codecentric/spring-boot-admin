import { HttpResponse, http } from 'msw';

import { liquibase } from './data.js';

const liquibaseEndpoints = [
  http.get('/instances/:instanceId/actuator/liquibase', () => {
    return HttpResponse.json(liquibase);
  }),
];

export default liquibaseEndpoints;
