import { HttpResponse, http } from 'msw';

const infoEndpoint = [
  http.get('/instances/:instanceId/actuator/info', () => {
    return HttpResponse.json({});
  }),
];

export default infoEndpoint;
