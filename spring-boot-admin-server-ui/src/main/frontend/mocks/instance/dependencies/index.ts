import { HttpResponse, http } from 'msw';

import {
  applicationSbomResponse,
  sbomsResponse,
  systemSbomResponse,
} from '@/mocks/instance/dependencies/data';

const dependenciesEndpoints = [
  http.get('/instances/:instanceId/actuator/sbom', () => {
    return HttpResponse.json(sbomsResponse);
  }),
  http.get('/instances/:instanceId/actuator/sbom/application', () => {
    return HttpResponse.json(applicationSbomResponse);
  }),
  http.get('/instances/:instanceId/actuator/sbom/system', () => {
    return HttpResponse.json(systemSbomResponse);
  }),
];

export default dependenciesEndpoints;
