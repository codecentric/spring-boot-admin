/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {
  STATE_COMPLETED,
  STATE_FAILED,
  responseHandler,
} from '@/views/instances/jolokia/responseHandler.js';

const instanceResultFailure = {
  data: {
    request: {
      mbean: 'org.springframework.boot:name=Auditevents,type=Endpoint',
      arguments: ['asd', '^12', 'asd'],
      type: 'exec',
      operation:
        'events(java.lang.String,java.time.OffsetDateTime,java.lang.String)',
    },
    stacktrace:
      'java.lang.IllegalArgumentException/java.lang.Thread.run(Thread.java:829)',
    error_type: 'java.lang.IllegalArgumentException',
    error:
      'java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found',
    status: 400,
  },
};

const instanceResultSuccess = {
  data: {
    request: {
      mbean: 'org.springframework.boot:name=Auditevents,type=Endpoint',
      arguments: ['asd', null, 'asd'],
      type: 'exec',
      operation:
        'events(java.lang.String,java.time.OffsetDateTime,java.lang.String)',
    },
    value: { events: [] },
    timestamp: 1650660516,
    status: 200,
  },
};

const applicationResultFailed = {
  data: [
    {
      instanceId: 'be5a5af1e545',
      status: 200,
      body: '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,"12",null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"stacktrace":"java.lang.IllegalArgumentException: Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found","error_type":"java.lang.IllegalArgumentException","error":"java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found","status":400}',
      contentType: 'text/plain;charset=utf-8',
    },
    {
      instanceId: '3153b559eebc',
      status: 200,
      body: '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,"12",null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"stacktrace":"java.lang.IllegalArgumentException: Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found","error_type":"java.lang.IllegalArgumentException","error":"java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found","status":400}',
      contentType: 'text/plain;charset=utf-8',
    },
  ],
};

const applicationResultSuccess = {
  data: [
    {
      instanceId: '3153b559eebc',
      status: 200,
      body: '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,null,null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"value":{"events":[]},"timestamp":1650701249,"status":200}',
      contentType: 'text/plain;charset=utf-8',
    },
    {
      instanceId: 'be5a5af1e545',
      status: 200,
      body: '{"request":{"mbean":"org.springframework.boot:name=Auditevents,type=Endpoint","arguments":[null,null,null],"type":"exec","operation":"events(java.lang.String,java.time.OffsetDateTime,java.lang.String)"},"value":{"events":[]},"timestamp":1650701249,"status":200}',
      contentType: 'text/plain;charset=utf-8',
    },
  ],
};

describe('responseHandler.js', () => {
  describe('On Instance level', () => {
    it('should succeed when a correct return value is given', () => {
      const { state, result } = responseHandler(instanceResultSuccess);

      expect(state).toBe(STATE_COMPLETED);
      expect(result).toEqual({ events: [] });
    });

    it('should handle jolokia returned errors', () => {
      const { state, error } = responseHandler(instanceResultFailure);

      expect(state).toBe(STATE_FAILED);
      expect(error.message).toBe(
        'Execution failed: java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found'
      );
    });
  });

  describe('On Application level', () => {
    it('should succeed when a correct return value is given', () => {
      const { state, result } = responseHandler(applicationResultSuccess);

      expect(state).toBe(STATE_COMPLETED);
      expect(result[0].value).toEqual(expect.objectContaining({ events: [] }));
      expect(result[1].value).toEqual(expect.objectContaining({ events: [] }));
    });

    it('should handle jolokia returned errors', () => {
      const { state, error } = responseHandler(applicationResultFailed);

      expect(state).toBe(STATE_FAILED);
      expect(error.message).toBe(
        'Execution failed: java.lang.IllegalArgumentException : Cannot convert string ^12 to type java.time.OffsetDateTime because no converter could be found'
      );
    });
  });
});
