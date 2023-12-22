import { HttpResponse, http } from 'msw';

import { auditeventsresponse } from '@/mocks/instance/auditevents/data';

const endpoints = [
  http.get('/instances/:instanceId/actuator/auditevents', () => {
    return HttpResponse.json(auditeventsresponse);
  }),
];

export default endpoints;
