export const mappings = {
  contexts: {
    'spring-boot-admin-sample-servlet': {
      mappings: {
        dispatcherServlets: {
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
                    '(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'caches'",
              predicate:
                '{GET [/actuator/caches], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/caches'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'httptrace'",
              predicate:
                '{GET [/actuator/httptrace], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/httptrace'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'metrics'",
              predicate:
                '{GET [/actuator/metrics], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/metrics'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'auditevents'",
              predicate:
                '{GET [/actuator/auditevents], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/auditevents'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'loggers-name'",
              predicate:
                '{POST [/actuator/loggers/{name}], consumes [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                  headers: [],
                  methods: ['POST'],
                  params: [],
                  patterns: ['/actuator/loggers/{name}'],
                  produces: [],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'custom'",
              predicate:
                '{GET [/actuator/custom], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/custom'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'logfile'",
              predicate:
                '{GET [/actuator/logfile], produces [text/plain;charset=UTF-8]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/logfile'],
                  produces: [
                    { mediaType: 'text/plain;charset=UTF-8', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'metrics-requiredMetricName'",
              predicate:
                '{GET [/actuator/metrics/{requiredMetricName}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/metrics/{requiredMetricName}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'loggers'",
              predicate:
                '{GET [/actuator/loggers], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/loggers'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'threaddump'",
              predicate:
                '{GET [/actuator/threaddump], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/threaddump'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'env'",
              predicate:
                '{GET [/actuator/env], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/env'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'startup'",
              predicate:
                '{GET [/actuator/startup], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/startup'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'threaddump'",
              predicate:
                '{GET [/actuator/threaddump], produces [text/plain;charset=UTF-8]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/threaddump'],
                  produces: [
                    { mediaType: 'text/plain;charset=UTF-8', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'info'",
              predicate:
                '{GET [/actuator/info], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/info'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'sessions-sessionId'",
              predicate: '{DELETE [/actuator/sessions/{sessionId}]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['DELETE'],
                  params: [],
                  patterns: ['/actuator/sessions/{sessionId}'],
                  produces: [],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'caches-cache'",
              predicate:
                '{DELETE [/actuator/caches/{cache}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['DELETE'],
                  params: [],
                  patterns: ['/actuator/caches/{cache}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'scheduledtasks'",
              predicate:
                '{GET [/actuator/scheduledtasks], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/scheduledtasks'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'configprops'",
              predicate:
                '{GET [/actuator/configprops], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/configprops'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'configprops-prefix'",
              predicate:
                '{GET [/actuator/configprops/{prefix}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/configprops/{prefix}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'env-toMatch'",
              predicate:
                '{GET [/actuator/env/{toMatch}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/env/{toMatch}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'startup'",
              predicate:
                '{POST [/actuator/startup], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['POST'],
                  params: [],
                  patterns: ['/actuator/startup'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'conditions'",
              predicate:
                '{GET [/actuator/conditions], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/conditions'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'sessions-sessionId'",
              predicate:
                '{GET [/actuator/sessions/{sessionId}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/sessions/{sessionId}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'mappings'",
              predicate:
                '{GET [/actuator/mappings], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/mappings'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'caches-cache'",
              predicate:
                '{GET [/actuator/caches/{cache}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/caches/{cache}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'beans'",
              predicate:
                '{GET [/actuator/beans], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/beans'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'health'",
              predicate:
                '{GET [/actuator/health], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/health'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'health-path'",
              predicate:
                '{GET [/actuator/health/**], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/health/**'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'caches'",
              predicate: '{DELETE [/actuator/caches]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['DELETE'],
                  params: [],
                  patterns: ['/actuator/caches'],
                  produces: [],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'heapdump'",
              predicate:
                '{GET [/actuator/heapdump], produces [application/octet-stream]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/heapdump'],
                  produces: [
                    { mediaType: 'application/octet-stream', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'loggers-name'",
              predicate:
                '{GET [/actuator/loggers/{name}], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/loggers/{name}'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler: "Actuator web endpoint 'sessions'",
              predicate:
                '{GET [/actuator/sessions], produces [application/vnd.spring-boot.actuator.v3+json || application/vnd.spring-boot.actuator.v2+json || application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler',
                  name: 'handle',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljava/util/Map;)Ljava/lang/Object;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/actuator/sessions'],
                  produces: [
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v3+json',
                      negated: false,
                    },
                    {
                      mediaType: 'application/vnd.spring-boot.actuator.v2+json',
                      negated: false,
                    },
                    { mediaType: 'application/json', negated: false },
                  ],
                },
              },
            },
            {
              handler:
                'org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#error(HttpServletRequest)',
              predicate: '{ [/error]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController',
                  name: 'error',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;)Lorg/springframework/http/ResponseEntity;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: [],
                  params: [],
                  patterns: ['/error'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController#errorHtml(HttpServletRequest, HttpServletResponse)',
              predicate: '{ [/error], produces [text/html]}',
              details: {
                handlerMethod: {
                  className:
                    'org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController',
                  name: 'errorHtml',
                  descriptor:
                    '(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)Lorg/springframework/web/servlet/ModelAndView;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: [],
                  params: [],
                  patterns: ['/error'],
                  produces: [{ mediaType: 'text/html', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#instances()',
              predicate: '{GET [/instances], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'instances',
                  descriptor: '()Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/instances'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#instance(String)',
              predicate: '{GET [/instances/{id}], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'instance',
                  descriptor:
                    '(Ljava/lang/String;)Lreactor/core/publisher/Mono;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/instances/{id}'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.ApplicationsController#unregister(String)',
              predicate: '{DELETE [/applications/{name}]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.ApplicationsController',
                  name: 'unregister',
                  descriptor:
                    '(Ljava/lang/String;)Lreactor/core/publisher/Mono;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['DELETE'],
                  params: [],
                  patterns: ['/applications/{name}'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController#getFilters()',
              predicate:
                '{GET [/notifications/filters], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController',
                  name: 'getFilters',
                  descriptor: '()Ljava/util/Collection;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/notifications/filters'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController#deleteFilter(String)',
              predicate: '{DELETE [/notifications/filters/{id}]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController',
                  name: 'deleteFilter',
                  descriptor:
                    '(Ljava/lang/String;)Lorg/springframework/http/ResponseEntity;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['DELETE'],
                  params: [],
                  patterns: ['/notifications/filters/{id}'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.ui.web.UiController#sbaSettings()',
              predicate:
                '{GET [/sba-settings.js], produces [application/javascript]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.ui.web.UiController',
                  name: 'sbaSettings',
                  descriptor: '()Ljava/lang/String;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/sba-settings.js'],
                  produces: [
                    { mediaType: 'application/javascript', negated: false },
                  ],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.ApplicationsController#applicationsStream()',
              predicate: '{GET [/applications], produces [text/event-stream]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.ApplicationsController',
                  name: 'applicationsStream',
                  descriptor: '()Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/applications'],
                  produces: [
                    { mediaType: 'text/event-stream', negated: false },
                  ],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#instanceStream(String)',
              predicate:
                '{GET [/instances/{id}], produces [text/event-stream]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'instanceStream',
                  descriptor:
                    '(Ljava/lang/String;)Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/instances/{id}'],
                  produces: [
                    { mediaType: 'text/event-stream', negated: false },
                  ],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#unregister(String)',
              predicate: '{DELETE [/instances/{id}]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'unregister',
                  descriptor:
                    '(Ljava/lang/String;)Lreactor/core/publisher/Mono;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['DELETE'],
                  params: [],
                  patterns: ['/instances/{id}'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#register(Registration, UriComponentsBuilder)',
              predicate: '{POST [/instances], consumes [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'register',
                  descriptor:
                    '(Lde/codecentric/boot/admin/server/domain/values/Registration;Lorg/springframework/web/util/UriComponentsBuilder;)Lreactor/core/publisher/Mono;',
                },
                requestMappingConditions: {
                  consumes: [{ mediaType: 'application/json', negated: false }],
                  headers: [],
                  methods: ['POST'],
                  params: [],
                  patterns: ['/instances'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.servlet.InstancesProxyController#endpointProxy(String, HttpServletRequest)',
              predicate:
                '{[GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS] [/applications/{applicationName}/actuator/**]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.servlet.InstancesProxyController',
                  name: 'endpointProxy',
                  descriptor:
                    '(Ljava/lang/String;Ljavax/servlet/http/HttpServletRequest;)Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: [
                    'GET',
                    'HEAD',
                    'POST',
                    'PUT',
                    'PATCH',
                    'DELETE',
                    'OPTIONS',
                  ],
                  params: [],
                  patterns: ['/applications/{applicationName}/actuator/**'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController#addFilter(String, String, Long)',
              predicate:
                '{POST [/notifications/filters], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.notify.filter.web.NotificationFilterController',
                  name: 'addFilter',
                  descriptor:
                    '(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)Lorg/springframework/http/ResponseEntity;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['POST'],
                  params: [],
                  patterns: ['/notifications/filters'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.ui.web.UiController#index()',
              predicate: '{GET [/], produces [text/html]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.ui.web.UiController',
                  name: 'index',
                  descriptor: '()Ljava/lang/String;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/'],
                  produces: [{ mediaType: 'text/html', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.ApplicationsController#application(String)',
              predicate:
                '{GET [/applications/{name}], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.ApplicationsController',
                  name: 'application',
                  descriptor:
                    '(Ljava/lang/String;)Lreactor/core/publisher/Mono;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/applications/{name}'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.servlet.InstancesProxyController#endpointProxy(String, HttpServletRequest, HttpServletResponse)',
              predicate:
                '{[GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS] [/instances/{instanceId}/actuator/**]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.servlet.InstancesProxyController',
                  name: 'endpointProxy',
                  descriptor:
                    '(Ljava/lang/String;Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: [
                    'GET',
                    'HEAD',
                    'POST',
                    'PUT',
                    'PATCH',
                    'DELETE',
                    'OPTIONS',
                  ],
                  params: [],
                  patterns: ['/instances/{instanceId}/actuator/**'],
                  produces: [],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#events()',
              predicate:
                '{GET [/instances/events], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'events',
                  descriptor: '()Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/instances/events'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#instances(String)',
              predicate:
                '{GET [/instances], params [name], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'instances',
                  descriptor:
                    '(Ljava/lang/String;)Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [{ name: 'name', value: null, negated: false }],
                  patterns: ['/instances'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.ui.web.UiController#login()',
              predicate: '{GET [/login], produces [text/html]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.ui.web.UiController',
                  name: 'login',
                  descriptor: '()Ljava/lang/String;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/login'],
                  produces: [{ mediaType: 'text/html', negated: false }],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.InstancesController#eventStream()',
              predicate:
                '{GET [/instances/events], produces [text/event-stream]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.InstancesController',
                  name: 'eventStream',
                  descriptor: '()Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/instances/events'],
                  produces: [
                    { mediaType: 'text/event-stream', negated: false },
                  ],
                },
              },
            },
            {
              handler:
                'de.codecentric.boot.admin.server.web.ApplicationsController#applications()',
              predicate: '{GET [/applications], produces [application/json]}',
              details: {
                handlerMethod: {
                  className:
                    'de.codecentric.boot.admin.server.web.ApplicationsController',
                  name: 'applications',
                  descriptor: '()Lreactor/core/publisher/Flux;',
                },
                requestMappingConditions: {
                  consumes: [],
                  headers: [],
                  methods: ['GET'],
                  params: [],
                  patterns: ['/applications'],
                  produces: [{ mediaType: 'application/json', negated: false }],
                },
              },
            },
            {
              handler:
                'ResourceHttpRequestHandler [Classpath [META-INF/resources/webjars/]]',
              predicate: '/webjars/**',
              details: null,
            },
            {
              handler: 'ResourceHttpRequestHandler',
              predicate: '/**',
              details: null,
            },
            {
              handler: 'ResourceHttpRequestHandler',
              predicate: '/extensions/**',
              details: null,
            },
          ],
        },
        servletFilters: [
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'webMvcMetricsFilter',
            className:
              'org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'requestContextFilter',
            className:
              'org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'homepageForwardFilter',
            className:
              'de.codecentric.boot.admin.server.ui.web.servlet.HomepageForwardingFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'sessionRepositoryFilter',
            className:
              'org.springframework.session.web.http.SessionRepositoryFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'Tomcat WebSocket (JSR356) Filter',
            className: 'org.apache.tomcat.websocket.server.WsFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'characterEncodingFilter',
            className:
              'org.springframework.boot.web.servlet.filter.OrderedCharacterEncodingFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'httpTraceFilter',
            className:
              'org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'springSecurityFilterChain',
            className:
              'org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean$1',
          },
          {
            servletNameMappings: [],
            urlPatternMappings: ['/*'],
            name: 'formContentFilter',
            className:
              'org.springframework.boot.web.servlet.filter.OrderedFormContentFilter',
          },
        ],
        servlets: [
          {
            mappings: ['/'],
            name: 'dispatcherServlet',
            className: 'org.springframework.web.servlet.DispatcherServlet',
          },
        ],
      },
      parentId: null,
    },
  },
};
