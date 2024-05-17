import { HttpResponse, http } from 'msw';

const healthEndpoint = [
  http.get('/instances/:instanceId/actuator/health', () => {
    return HttpResponse.json({
      status: 'UP',
      details: {
        clientConfigServer: {
          status: 'UNKNOWN',
          details: { error: 'no property sources located' },
        },
        db: {
          status: 'UP',
          details: {
            database: 'HSQL Database Engine',
            validationQuery: 'isValid()',
          },
        },
        discoveryComposite: {
          description: 'Discovery Client not initialized',
          status: 'UNKNOWN',
          details: {
            discoveryClient: {
              description: 'Discovery Client not initialized',
              status: 'UNKNOWN',
            },
          },
        },
        diskSpace: {
          status: 'UP',
          details: {
            total: 994662584320,
            free: 300063879168,
            threshold: 10485760,
            exists: true,
          },
        },
        ping: { status: 'UP' },
        reactiveDiscoveryClients: {
          description: 'Discovery Client not initialized',
          status: 'UNKNOWN',
          details: {
            'Simple Reactive Discovery Client': {
              description: 'Discovery Client not initialized',
              status: 'UNKNOWN',
            },
          },
        },
        refreshScope: { status: 'UP' },
      },
    });
  }),
];

export default healthEndpoint;
