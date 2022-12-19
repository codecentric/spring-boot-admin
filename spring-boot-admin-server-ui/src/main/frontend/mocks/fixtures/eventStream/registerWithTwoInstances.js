export const registerWithTwoInstances = {
  name: 'spring-boot-admin-sample-servlet',
  buildVersion: '3.0.0-SNAPSHOT',
  status: 'UP',
  statusTimestamp: '2022-12-19T10:07:20.646905Z',
  instances: [
    {
      id: '25b07dc98984',
      version: 3,
      registration: {
        name: 'spring-boot-admin-sample-servlet',
        managementUrl:
          'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator',
        healthUrl:
          'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/health',
        serviceUrl:
          'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/',
        source: 'http-api',
        metadata: {
          'tags.de-service-test-6': 'A large content',
          'tags.de-service-test-4': 'A large content',
          'tags.de-service-test-5': 'A large content',
          startup: '2022-12-19T11:04:38.171314+01:00',
          'kubectl.kubernetes.iolast-applied-configuration':
            '{"name":"jvm.threads.peak","description":"The peak live thread count since the Java virtual machine started or peak was reset","baseUnit":"threads","measurements":[{"statistic":"VALUE","value":64.0}],"availableTags":[]}',
          'user.name': 'user',
          'tags.de-service-test-2': 'A large content',
          'user.password': '******',
          'tags.de-service-test-3': 'A large content',
          'tags.environment': 'test',
          'tags.de-service-test-1': 'A large content',
        },
      },
      registered: true,
      statusInfo: {
        status: 'UP',
        details: {
          reactiveDiscoveryClients: {
            description: 'Discovery Client not initialized',
            status: 'UNKNOWN',
            components: {
              'Simple Reactive Discovery Client': {
                description: 'Discovery Client not initialized',
                status: 'UNKNOWN',
              },
            },
          },
          clientConfigServer: {
            status: 'UNKNOWN',
            details: {
              error: 'no property sources located',
            },
          },
          diskSpace: {
            status: 'UP',
            details: {
              total: 994662584320,
              free: 333813846016,
              threshold: 10485760,
              path: '/Users/stekoe/workspaces/cc/spring-boot-admin/spring-boot-admin-samples/spring-boot-admin-sample-servlet/.',
              exists: true,
            },
          },
          ping: {
            status: 'UP',
          },
          discoveryComposite: {
            description: 'Discovery Client not initialized',
            status: 'UNKNOWN',
            components: {
              discoveryClient: {
                description: 'Discovery Client not initialized',
                status: 'UNKNOWN',
              },
            },
          },
          refreshScope: {
            status: 'UP',
          },
          db: {
            status: 'UP',
            details: {
              database: 'HSQL Database Engine',
              validationQuery: 'isValid()',
            },
          },
        },
      },
      statusTimestamp: '2022-12-19T10:04:39.457363Z',
      info: {
        build: {
          artifact: 'spring-boot-admin-sample-servlet',
          name: 'Spring Boot Admin Sample Servlet',
          time: '2022-12-16T07:23:45.732Z',
          version: '3.0.0-SNAPSHOT',
          group: 'de.codecentric',
        },
      },
      endpoints: [
        {
          id: 'caches',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/caches',
        },
        {
          id: 'loggers',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/loggers',
        },
        {
          id: 'heapdump',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/heapdump',
        },
        {
          id: 'features',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/features',
        },
        {
          id: 'startup',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/startup',
        },
        {
          id: 'beans',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/beans',
        },
        {
          id: 'configprops',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/configprops',
        },
        {
          id: 'threaddump',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/threaddump',
        },
        {
          id: 'auditevents',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/auditevents',
        },
        {
          id: 'info',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/info',
        },
        {
          id: 'resume',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/resume',
        },
        {
          id: 'sessions',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/sessions',
        },
        {
          id: 'restart',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/restart',
        },
        {
          id: 'logfile',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/logfile',
        },
        {
          id: 'custom',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/custom',
        },
        {
          id: 'health',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/health',
        },
        {
          id: 'refresh',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/refresh',
        },
        {
          id: 'env',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/env',
        },
        {
          id: 'pause',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/pause',
        },
        {
          id: 'scheduledtasks',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/scheduledtasks',
        },
        {
          id: 'mappings',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/mappings',
        },
        {
          id: 'metrics',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/metrics',
        },
        {
          id: 'conditions',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/conditions',
        },
        {
          id: 'httpexchanges',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/httpexchanges',
        },
        {
          id: 'shutdown',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8080/actuator/shutdown',
        },
      ],
      buildVersion: '3.0.0-SNAPSHOT',
      tags: {
        'de-service-test-6': 'A large content',
        'de-service-test-4': 'A large content',
        'de-service-test-5': 'A large content',
        'de-service-test-2': 'A large content',
        'de-service-test-3': 'A large content',
        environment: 'test',
        'de-service-test-1': 'A large content',
      },
    },
    {
      id: 'af578c480d41',
      version: 1,
      registration: {
        name: 'spring-boot-admin-sample-servlet',
        managementUrl:
          'http://ip-192-168-178-20.eu-central-1.compute.internal:8888/actuator',
        healthUrl:
          'http://ip-192-168-178-20.eu-central-1.compute.internal:8888/actuator/health',
        serviceUrl:
          'http://ip-192-168-178-20.eu-central-1.compute.internal:8888/',
        source: 'http-api',
        metadata: {
          'tags.de-service-test-6': 'A large content',
          'tags.de-service-test-4': 'A large content',
          'tags.de-service-test-5': 'A large content',
          startup: '2022-12-19T11:07:19.690036+01:00',
          'kubectl.kubernetes.iolast-applied-configuration':
            '{"name":"jvm.threads.peak","description":"The peak live thread count since the Java virtual machine started or peak was reset","baseUnit":"threads","measurements":[{"statistic":"VALUE","value":64.0}],"availableTags":[]}',
          'user.name': 'user',
          'tags.de-service-test-2': 'A large content',
          'user.password': '******',
          'tags.de-service-test-3': 'A large content',
          'tags.environment': 'test',
          'tags.de-service-test-1': 'A large content',
        },
      },
      registered: true,
      statusInfo: {
        status: 'UP',
        details: {
          reactiveDiscoveryClients: {
            description: 'Discovery Client not initialized',
            status: 'UNKNOWN',
            components: {
              'Simple Reactive Discovery Client': {
                description: 'Discovery Client not initialized',
                status: 'UNKNOWN',
              },
            },
          },
          clientConfigServer: {
            status: 'UNKNOWN',
            details: {
              error: 'no property sources located',
            },
          },
          diskSpace: {
            status: 'UP',
            details: {
              total: 994662584320,
              free: 332211322880,
              threshold: 10485760,
              path: '/Users/stekoe/workspaces/cc/spring-boot-admin/spring-boot-admin-samples/spring-boot-admin-sample-servlet/.',
              exists: true,
            },
          },
          ping: {
            status: 'UP',
          },
          discoveryComposite: {
            description: 'Discovery Client not initialized',
            status: 'UNKNOWN',
            components: {
              discoveryClient: {
                description: 'Discovery Client not initialized',
                status: 'UNKNOWN',
              },
            },
          },
          refreshScope: {
            status: 'UP',
          },
          db: {
            status: 'UP',
            details: {
              database: 'HSQL Database Engine',
              validationQuery: 'isValid()',
            },
          },
        },
      },
      statusTimestamp: '2022-12-19T10:07:20.646905Z',
      info: {},
      endpoints: [
        {
          id: 'health',
          url: 'http://ip-192-168-178-20.eu-central-1.compute.internal:8888/actuator/health',
        },
      ],
      buildVersion: null,
      tags: {
        'de-service-test-6': 'A large content',
        'de-service-test-4': 'A large content',
        'de-service-test-5': 'A large content',
        'de-service-test-2': 'A large content',
        'de-service-test-3': 'A large content',
        environment: 'test',
        'de-service-test-1': 'A large content',
      },
    },
  ],
};
