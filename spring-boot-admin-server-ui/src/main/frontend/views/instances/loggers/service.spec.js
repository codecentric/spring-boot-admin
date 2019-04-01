/*
 * Copyright 2014-2019 the original author or authors.
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

import {ApplicationLoggers, InstanceLoggers} from './service';

describe('InstanceLoggers', () => {
  const instance = {
    id: 'test-1',
    fetchLoggers: jest.fn(),
    configureLogger: jest.fn()
  };
  const service = new InstanceLoggers(instance);

  it('should configure loggers', () => {
    service.configureLogger('ROOT', 'WARN');
    expect(instance.configureLogger).toHaveBeenCalledWith('ROOT', 'WARN');
  });

  it('should fetch loggers', async () => {
    instance.fetchLoggers.mockReturnValue(
      Promise.resolve({
        data: {
          levels: ['TRACE', 'INFO', 'FATAL'],
          loggers: {
            'ROOT': {
              configuredLevel: 'INFO',
              effectiveLevel: 'INFO'
            },
            'de.codecentric': {
              configuredLevel: null,
              effectiveLevel: 'INFO'
            }
          }
        }
      })
    );

    const cfg = await service.fetchLoggers();

    expect(cfg).toEqual({
      levels: ['TRACE', 'INFO', 'FATAL'],
      loggers: [{
        name: 'ROOT',
        level: [{
          instanceId: 'test-1',
          configuredLevel: 'INFO',
          effectiveLevel: 'INFO'
        }]
      }, {
        name: 'de.codecentric',
        level: [{
          instanceId: 'test-1',
          configuredLevel: null,
          effectiveLevel: 'INFO'
        }]
      }]
    })
  });
});

describe('ApplicationLoggers', () => {
  const application = {
    fetchLoggers: jest.fn(),
    configureLogger: jest.fn()
  };
  const service = new ApplicationLoggers(application);

  it('should configure loggers', () => {
    service.configureLogger('ROOT', 'WARN');
    expect(application.configureLogger).toHaveBeenCalledWith('ROOT', 'WARN');
  });

  it('should fetch loggers', async () => {
    application.fetchLoggers.mockReturnValue(
      Promise.resolve({
        responses: [
          {
            instanceId: 'test-1',
            body: {
              levels: ['TRACE', 'FATAL'],
              loggers: {
                'ROOT': {
                  configuredLevel: 'INFO',
                  effectiveLevel: 'INFO'
                },
                'de.codecentric': {
                  configuredLevel: null,
                  effectiveLevel: 'INFO'
                }
              }
            }
          },
          {
            instanceId: 'test-2',
            body: {
              levels: ['INFO'],
              loggers: {
                'ROOT': {
                  configuredLevel: 'INFO',
                  effectiveLevel: 'INFO'
                },
                'de.codecentric': {
                  configuredLevel: 'WARN',
                  effectiveLevel: 'WARN'
                }
              }
            }
          }
        ]
      })
    );

    const cfg = await service.fetchLoggers();

    expect(cfg).toEqual({
      levels: ['TRACE', 'FATAL', 'INFO'],
      loggers: [{
        name: 'ROOT',
        level: [{
          instanceId: 'test-1',
          configuredLevel: 'INFO',
          effectiveLevel: 'INFO'
        }, {
          instanceId: 'test-2',
          configuredLevel: 'INFO',
          effectiveLevel: 'INFO'
        }]
      }, {
        name: 'de.codecentric',
        level: [{
          instanceId: 'test-1',
          configuredLevel: null,
          effectiveLevel: 'INFO'
        }, {
          instanceId: 'test-2',
          configuredLevel: 'WARN',
          effectiveLevel: 'WARN'
        }]
      }]
    })
  });
});
