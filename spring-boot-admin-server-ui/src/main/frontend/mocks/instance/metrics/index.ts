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
  http.get('/instances/:instanceId/actuator/metrics', () => {
    return HttpResponse.json({
      names: ['cache.gets', 'jdbc.connections.active'],
    });
  }),
  http.get('/instances/:instanceId/actuator/metrics/cache.gets', () => {
    return HttpResponse.json({
      name: 'cache.gets',
      description: 'The number of cache gets',
      baseUnit: 'none',
      measurements: [
        {
          statistic: 'COUNT',
          value: 150,
        },
        {
          statistic: 'TOTAL_TIME',
          value: 120.5,
        },
        {
          statistic: 'MAX',
          value: 5.2,
        },
      ],
      availableTags: [
        {
          tag: 'name',
          values: ['myCache'],
        },
        {
          tag: 'cache',
          values: ['myCache'],
        },
        {
          tag: 'result',
          values: ['hit', 'miss'],
        },
      ],
    });
  }),
  http.get(
    '/instances/:instanceId/actuator/metrics/jdbc.connections.active',
    () => {
      return HttpResponse.json({
        name: 'jdbc.connections.active',
        description: 'The number of active JDBC connections',
        baseUnit: 'connections',
        measurements: [
          {
            statistic: 'VALUE',
            value: 5,
          },
        ],
        availableTags: [
          {
            tag: 'name',
            values: ['HikariPool-1'],
          },
          {
            tag: 'pool',
            values: ['HikariPool-1'],
          },
          {
            tag: 'min',
            values: 1,
          },
        ],
      });
    },
  ),
  http.get('/instances/:instanceId/actuator/metrics/jdbc.connections.min', () =>
    HttpResponse.json({
      name: 'jdbc.connections.min',
      description: 'The minimum number of idle JDBC connections in the pool',
      baseUnit: 'connections',
      measurements: [
        {
          statistic: 'VALUE',
          value: 10,
        },
      ],
      availableTags: [
        {
          tag: 'pool',
          values: ['HikariPool-1'],
        },
      ],
    }),
  ),
  http.get('/instances/:instanceId/actuator/metrics/jdbc.connections.max', () =>
    HttpResponse.json({
      name: 'jdbc.connections.max',
      description: 'The minimum number of idle JDBC connections in the pool',
      baseUnit: 'connections',
      measurements: [
        {
          statistic: 'VALUE',
          value: 10,
        },
      ],
      availableTags: [
        {
          tag: 'pool',
          values: ['HikariPool-1'],
        },
      ],
    }),
  ),
];

export default metricsEntpoints;
