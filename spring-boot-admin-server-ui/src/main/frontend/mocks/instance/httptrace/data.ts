const now = new Date();
const today = [
  now.getFullYear(),
  String(now.getMonth() + 1).padStart(2, '0'),
  String(now.getDate()).padStart(2, '0'),
].join('-');

export const httptraceresponse = {
  traces: [
    {
      timestamp: today + 'T09:12:34.567Z',
      principal: 'admin',
      session: 'D43F6A32C1E34A7B',
      request: {
        method: 'GET',
        uri: 'http://localhost:8080/api/users',
        headers: {
          accept: ['application/json'],
          'user-agent': [
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
          ],
          authorization: ['Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'],
        },
        remoteAddress: '192.168.1.105',
      },
      response: {
        status: 200,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['1024'],
          'cache-control': ['no-cache, no-store, max-age=0, must-revalidate'],
        },
        timeTaken: 125,
      },
    },
    {
      timestamp: today + 'T09:13:45.678Z',
      principal: 'user123',
      session: 'E54G7B43D2F45B8C',
      request: {
        method: 'POST',
        uri: 'http://localhost:8080/api/orders',
        headers: {
          'content-type': ['application/json'],
          'user-agent': ['PostmanRuntime/7.29.2'],
          authorization: ['Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'],
        },
        remoteAddress: '192.168.1.110',
      },
      response: {
        status: 201,
        headers: {
          'content-type': ['application/json'],
          location: ['/api/orders/12345'],
          'content-length': ['256'],
        },
        timeTaken: 187,
      },
    },
    {
      timestamp: today + today + 'T09:14:56.789Z',
      principal: null,
      session: null,
      request: {
        method: 'GET',
        uri: 'http://localhost:8080/api/products',
        headers: {
          accept: ['application/json'],
          'user-agent': ['curl/7.68.0'],
        },
        remoteAddress: '192.168.1.115',
      },
      response: {
        status: 200,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['2048'],
          'cache-control': ['max-age=3600'],
        },
        timeTaken: 95,
      },
    },
    {
      timestamp: today + 'T09:15:23.456Z',
      principal: 'user456',
      session: 'F65H8C54E3G56D9E',
      request: {
        method: 'PUT',
        uri: 'http://localhost:8080/api/users/profile',
        headers: {
          'content-type': ['application/json'],
          'user-agent': ['Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)'],
          authorization: ['Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'],
        },
        remoteAddress: '192.168.1.120',
      },
      response: {
        status: 200,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['512'],
        },
        timeTaken: 143,
      },
    },
    {
      timestamp: today + 'T09:16:34.567Z',
      principal: null,
      session: null,
      request: {
        method: 'GET',
        uri: 'http://localhost:8080/actuator/health',
        headers: {
          accept: ['application/json'],
          'user-agent': ['Prometheus/2.40.0'],
        },
        remoteAddress: '192.168.1.200',
      },
      response: {
        status: 200,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['15'],
        },
        timeTaken: 12,
      },
    },
    {
      timestamp: today + 'T09:17:45.678Z',
      principal: 'admin',
      session: 'D43F6A32C1E34A7B',
      request: {
        method: 'DELETE',
        uri: 'http://localhost:8080/api/products/54321',
        headers: {
          'user-agent': [
            'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
          ],
          authorization: ['Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'],
        },
        remoteAddress: '192.168.1.105',
      },
      response: {
        status: 204,
        headers: {
          'cache-control': ['no-cache, no-store, max-age=0, must-revalidate'],
        },
        timeTaken: 78,
      },
    },
    {
      timestamp: today + 'T09:18:56.789Z',
      principal: null,
      session: null,
      request: {
        method: 'OPTIONS',
        uri: 'http://localhost:8080/api/users',
        headers: {
          origin: ['https://example.com'],
          'access-control-request-method': ['GET'],
          'user-agent': [
            'Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X)',
          ],
        },
        remoteAddress: '192.168.1.130',
      },
      response: {
        status: 200,
        headers: {
          'access-control-allow-origin': ['https://example.com'],
          'access-control-allow-methods': ['GET, POST, PUT, DELETE'],
          'access-control-allow-headers': ['Authorization, Content-Type'],
          'access-control-max-age': ['3600'],
        },
        timeTaken: 5,
      },
    },
    {
      timestamp: today + 'T09:19:23.456Z',
      principal: 'user789',
      session: 'G76I9D65F4H67E0F',
      request: {
        method: 'GET',
        uri: 'http://localhost:8080/api/orders/history?page=0&size=10',
        headers: {
          accept: ['application/json'],
          'user-agent': ['Mozilla/5.0 (Android 12; Mobile)'],
          authorization: ['Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'],
        },
        remoteAddress: '192.168.1.140',
      },
      response: {
        status: 200,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['1536'],
          'x-total-count': ['45'],
        },
        timeTaken: 167,
      },
    },
    {
      timestamp: today + 'T09:20:34.567Z',
      principal: null,
      session: null,
      request: {
        method: 'GET',
        uri: 'http://localhost:8080/api/nonexistent',
        headers: {
          accept: ['application/json'],
          'user-agent': ['curl/7.68.0'],
        },
        remoteAddress: '192.168.1.150',
      },
      response: {
        status: 404,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['64'],
        },
        timeTaken: 23,
      },
    },
    {
      timestamp: today + 'T09:21:45.678Z',
      principal: 'user123',
      session: 'E54G7B43D2F45B8C',
      request: {
        method: 'POST',
        uri: 'http://localhost:8080/api/checkout',
        headers: {
          'content-type': ['application/json'],
          'user-agent': ['PostmanRuntime/7.29.2'],
          authorization: ['Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'],
        },
        remoteAddress: '192.168.1.110',
      },
      response: {
        status: 400,
        headers: {
          'content-type': ['application/json'],
          'content-length': ['128'],
        },
        timeTaken: 45,
      },
    },
  ],
};
