import { HttpResponse, http } from 'msw';

import { mappings } from './data';

const mappingsEndpoint = [
  http.get('/instances/:instanceId/actuator/mappings', () => {
    return HttpResponse.json(mappings);
  }),
];

export default mappingsEndpoint;
