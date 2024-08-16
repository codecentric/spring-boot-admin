import { HttpResponse, http } from 'msw';

import {
  memoryCommittedResponse,
  memoryMaxResponse,
  memoryUsedResponse,
} from '@/mocks/instance/metrics/data';

const metricsEntpoints = [
  http.get('/instances/:instanceId/actuator/metrics/jvm.memory.max', () => {
    return HttpResponse.json(memoryMaxResponse);
  }),
  http.get('/instances/:instanceId/actuator/metrics/jvm.memory.used', () => {
    return HttpResponse.json(memoryUsedResponse);
  }),
  http.get(
    '/instances/:instanceId/actuator/metrics/jvm.memory.committed',
    () => {
      return HttpResponse.json(memoryCommittedResponse);
    },
  ),
];

export default metricsEntpoints;
