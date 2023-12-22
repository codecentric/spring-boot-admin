import {responseHandler, STATE_COMPLETED, STATE_FAILED} from '@/views/instances/jolokia/responseHandler.js';

const instanceResultFailure = {
  data: {
    'request': {
      'mbean': 'org.springframework.boot:name=Auditevents,type=Endpoint',
      'arguments': ['asd', '^12', 'asd'],
      'type': 'exec',
      'operation': 'events(java.lang.String,java.time.OffsetDateTime,java.lang.String)'
    },
    'stacktrace': 'java.lang.IllegalArgumentException/java.lang.Thread.run(Thread.java:829)',
    'error_type': 'java.lang.IllegalArgumentException',
    'error': 'java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found',
    'status': 400
  }
};

const instanceResultSuccess = {
  data: {
    'request': {
      'mbean': 'org.springframework.boot:name=Auditevents,type=Endpoint',
      'arguments': ['asd', null, 'asd'],
      'type': 'exec',
      'operation': 'events(java.lang.String,java.time.OffsetDateTime,java.lang.String)'
    },
    'value': {'events': []},
    'timestamp': 1650660516,
    'status': 200
  }
};

const applicationResultFailed = {
  data: [{
    'instanceId': 'be5a5af1e545',
    'status': 200,
    'body': '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,null,null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"value":{"events":[]},"timestamp":1650701249,"status":200}',
    'contentType': 'text/plain;charset=utf-8'
  }, {
    'instanceId': '3153b559eebc',
    'status': 200,
    'body': '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,"12",null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"stacktrace":"java.lang.IllegalArgumentException: Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found","error_type":"java.lang.IllegalArgumentException","error":"java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found","status":400}',
    'contentType': 'text/plain;charset=utf-8'
  }]
}

const applicationResultSuccess = {
  data: [{
    'instanceId': '3153b559eebc',
    'status': 200,
    'body': '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,null,null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"value":{"events":[]},"timestamp":1650701249,"status":200}',
    'contentType': 'text/plain;charset=utf-8'
  }, {
    'instanceId': 'be5a5af1e545',
    'status': 200,
    'body': '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,null,null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"value":{"events":[]},"timestamp":1650701249,"status":200}',
    'contentType': 'text/plain;charset=utf-8'
  }]
}

const applicationResultHttpError = {
  data: [{
    'instanceId': 'be5a5af1e545',
    'status': 200,
    'body': '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,null,null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"value":{"events":[]},"timestamp":1650701249,"status":200}',
    'contentType': 'text/plain;charset=utf-8'
  }, {
    'instanceId': '3153b559eebc',
    'status': 502
  }]
}

describe('responseHandler.js', () => {
  describe('On Instance level', () => {
    it('should succeed when a correct return value is given', () => {
      const {state, result} = responseHandler(instanceResultSuccess);

      expect(state).toBe(STATE_COMPLETED);
      expect(result).toEqual({'events': []})
    });

    it('should handle jolokia returned errors', () => {
      const {state, error} = responseHandler(instanceResultFailure);

      expect(state).toBe(STATE_FAILED);
      expect(error.message).toBe('Execution failed: java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found');
    });
  });

  describe('On Application level', () => {
    it('should succeed when a correct return value is given', () => {
      const stateResultList = responseHandler(applicationResultSuccess);

      stateResultList.forEach(({state, result}) => {
        expect(state).toBe(STATE_COMPLETED);
        expect(result).toEqual(expect.objectContaining({'events': []}))
      });
    });

    it('should handle jolokia returned errors', () => {
      const stateResultList = responseHandler(applicationResultFailed);

      {
        const {state, result} = stateResultList[0];
        expect(state).toBe(STATE_COMPLETED);
        expect(result).toEqual(expect.objectContaining({'events': []}))
      }
      {
        const {state, error} = stateResultList[1];
        expect(state).toBe(STATE_FAILED);
        expect(error.message).toBe('Execution failed: java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found');
      }
    });

    it('should handle HTTP returned errors', () => {
      const stateResultList = responseHandler(applicationResultHttpError);

      {
        const {state, result} = stateResultList[0];
        expect(state).toBe(STATE_COMPLETED);
        expect(result).toEqual(expect.objectContaining({'events': []}))
      }
      {
        const {state, error} = stateResultList[1];
        expect(state).toBe(STATE_FAILED);
        expect(error.message).toBe('Execution failed with HTTP error: 502');
      }
    });
  });
});
