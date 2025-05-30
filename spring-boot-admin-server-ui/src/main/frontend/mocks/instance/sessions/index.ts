import { HttpResponse, http } from 'msw';

import { scheduledtasksResponse } from '@/mocks/instance/scheduledtasks/data';

const endpoints = [
  http.get('/instances/:instanceId/actuator/scheduledtasks', () => {
    return HttpResponse.json(scheduledtasksResponse);
  }),
];

export default endpoints;
