import { HttpResponse, http } from 'msw';

import { scheduledtasksResponse } from '@/mocks/instance/sessions/data';

const endpoints = [
  http.get('/instances/:instanceId/actuator/sessions', () => {
    return HttpResponse.json(scheduledtasksResponse);
  }),
];

export default endpoints;
