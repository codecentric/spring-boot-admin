const now = new Date();
const today = [
  now.getFullYear(),
  String(now.getMonth() + 1).padStart(2, '0'),
  String(now.getDate()).padStart(2, '0'),
].join('-');

export const scheduledtasksResponse = {
  sessions: [
    {
      id: 'D43F6A32C1E34A7B',
      creationTime: today + 'T08:23:45.123Z',
      lastAccessedTime: today + 'T09:12:34.567Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'admin',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'admin',
            authorities: ['ROLE_ADMIN', 'ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-01',
        userAgent:
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
        remoteAddress: '192.168.1.105',
      },
    },
    {
      id: 'E54G7B43D2F45B8C',
      creationTime: today + 'T08:26:45.234Z',
      lastAccessedTime: today + 'T09:21:45.678Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'user123',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'user123',
            authorities: ['ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-02',
        userAgent: 'PostmanRuntime/7.29.2',
        remoteAddress: '192.168.1.110',
      },
    },
    {
      id: 'F65H8C54E3G56D9E',
      creationTime: today + 'T08:32:11.678Z',
      lastAccessedTime: today + 'T09:15:23.456Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'user456',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'user456',
            authorities: ['ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-03',
        userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
        remoteAddress: '192.168.1.120',
      },
    },
    {
      id: 'G76I9D65F4H67E0F',
      creationTime: today + 'T09:05:22.345Z',
      lastAccessedTime: today + 'T09:19:23.456Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'user789',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'user789',
            authorities: ['ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'mobile-session-01',
        userAgent: 'Mozilla/5.0 (Android 12; Mobile)',
        remoteAddress: '192.168.1.140',
      },
    },
    {
      id: 'H87J0E76G5I78F1G',
      creationTime: today + 'T08:45:33.456Z',
      lastAccessedTime: today + 'T09:10:12.345Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'manager1',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'manager1',
            authorities: ['ROLE_MANAGER', 'ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-04',
        userAgent:
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/96.0.4664.110',
        remoteAddress: '192.168.1.160',
      },
    },
    {
      id: 'I98K1F87H6J89G2H',
      creationTime: today + 'T08:50:44.567Z',
      lastAccessedTime: today + 'T08:55:22.678Z',
      maxInactiveInterval: 1800,
      expired: true,
      principal: 'user321',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'user321',
            authorities: ['ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-05',
        userAgent: 'Mozilla/5.0 (iPad; CPU OS 15_0 like Mac OS X)',
        remoteAddress: '192.168.1.170',
      },
    },
    {
      id: 'J09L2G98I7K90H3I',
      creationTime: today + 'T09:00:55.678Z',
      lastAccessedTime: today + 'T09:22:33.789Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'analyst1',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'analyst1',
            authorities: ['ROLE_ANALYST', 'ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-06',
        userAgent: 'Mozilla/5.0 (X11; Linux x86_64) Firefox/95.0',
        remoteAddress: '192.168.1.180',
      },
    },
    {
      id: 'K10M3H09J8L01I4J',
      creationTime: today + 'T09:05:06.789Z',
      lastAccessedTime: today + 'T09:20:44.890Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'support1',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'support1',
            authorities: ['ROLE_SUPPORT', 'ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-07',
        userAgent:
          'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Edge/96.0.1054.62',
        remoteAddress: '192.168.1.190',
      },
    },
    {
      id: 'L21N4I10K9M12J5K',
      creationTime: today + 'T09:10:17.890Z',
      lastAccessedTime: today + 'T09:10:17.890Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'guest',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'guest',
            authorities: ['ROLE_GUEST'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-08',
        userAgent:
          'Mozilla/5.0 (iPhone; CPU iPhone OS 15_1 like Mac OS X) Safari/605.1.15',
        remoteAddress: '192.168.1.200',
      },
    },
    {
      id: 'M32O5J21L0N23K6L',
      creationTime: today + 'T09:15:28.901Z',
      lastAccessedTime: today + 'T09:18:55.012Z',
      maxInactiveInterval: 1800,
      expired: false,
      principal: 'developer1',
      attributes: {
        SPRING_SECURITY_CONTEXT: {
          authentication: {
            name: 'developer1',
            authorities: ['ROLE_DEVELOPER', 'ROLE_USER'],
            authenticated: true,
          },
        },
        sessionTrackingId: 'web-session-09',
        userAgent:
          'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Chrome/96.0.4664.110',
        remoteAddress: '192.168.1.210',
      },
    },
  ],
  sessionCount: 10,
  activeSessionCount: 9,
  expiredSessionCount: 1,
};
