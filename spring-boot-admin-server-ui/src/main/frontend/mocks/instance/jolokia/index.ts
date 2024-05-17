import { HttpResponse, http } from 'msw';

import { jolokiaList } from './data';

import { jolokiaRead } from '@/mocks/instance/jolokia/data.read';

const jolokiaEndpoint = [
  http.get('/instances/:instanceId/actuator/jolokia/list', () => {
    return HttpResponse.json(jolokiaList);
  }),
  http.post('/instances/:instanceId/actuator/jolokia', async () => {
    try {
      const body = { type: 'read' };
      if (body.type === 'read') {
        return HttpResponse.json(jolokiaRead);
      }
    } catch (e) {
      console.error(e);
    }
  }),
];

export default jolokiaEndpoint;
