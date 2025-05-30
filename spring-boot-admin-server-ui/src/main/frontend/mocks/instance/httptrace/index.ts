import { HttpResponse, http } from 'msw';

import { httptraceresponse } from '@/mocks/instance/httptrace/data';

const endpoints = [
  http.get('/instances/:instanceId/actuator/httptrace', () => {
    return HttpResponse.json(httptraceresponse);
  }),
];

export default endpoints;
