import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import DispatcherMappings from '@/views/instances/mappings/DispatcherMappings.vue';

describe('DispatcherMappings.vue', () => {
  describe('dispatcherServlet available', () => {
    beforeEach(async () => {
      render(DispatcherMappings, {
        props: {
          dispatchers: {
            dispatcherServlet: [
              {
                handler: 'Actuator root web endpoint',
                predicate:
                  '{GET [/actuator], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
                details: {
                  handlerMethod: {
                    className:
                      'org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping.WebMvcLinksHandler',
                    name: 'links',
                    descriptor:
                      '(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)Ljava/util/Map;',
                  },
                  requestMappingConditions: {
                    consumes: [],
                    headers: [],
                    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
                    params: [],
                    patterns: ['/actuator'],
                    produces: [
                      {
                        mediaType:
                          'application/vnd.spring-boot.actuator.v3+json',
                        negated: false,
                      },
                      {
                        mediaType:
                          'application/vnd.spring-boot.actuator.v2+json',
                        negated: false,
                      },
                      {
                        mediaType: 'application/json',
                        negated: false,
                      },
                    ],
                  },
                },
              },
            ],
          },
        },
      });
    });

    it('should render endpoint path', async () => {
      const path = await screen.getByRole('cell', { name: '/actuator' });
      expect(path).toBeVisible();
    });

    it('should render produces information', async () => {
      const produces = screen.getByRole('cell', {
        name: 'application/vnd.spring-boot.actuator.v3+json, application/vnd.spring-boot.actuator.v2+json, application/json',
      });
      expect(produces).toBeVisible();
    });

    it('should render http-verbs', async () => {
      const httpVerbs = screen.getByRole('cell', {
        name: 'GET, POST, PUT, DELETE, OPTIONS',
      });
      expect(httpVerbs).toBeVisible();
    });
  });

  describe('dispatcherServlet not available', () => {
    it('should not crash when no data is available', () => {
      render(DispatcherMappings);
    });
  });
});
