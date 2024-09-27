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
import { cloneDeep } from 'lodash-es';
import { beforeEach, describe, expect, it } from 'vitest';

import { StartupActuatorService } from './startup-actuator';
import fixture from './startup-actuator.fixture.spec.json';

describe('StartupActuatorService', () => {
  let data: any = {};
  let events: any = {};

  beforeEach(() => {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    data = cloneDeep(fixture);
    events = data.timeline.events;
  });

  it('should find element by id', () => {
    const item8 = StartupActuatorService.getById(events, 8);

    expect.assertions(1);
    expect(item8).toEqual({
      startupStep: {
        name: 'spring.beans.instantiate',
        id: 8,
        parentId: 7,
        tags: [
          {
            key: 'beanName',
            value:
              'org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory',
          },
        ],
      },
      startTime: '2020-12-10T21:53:42.958550077Z',
      endTime: '2020-12-10T21:53:42.960040652Z',
      duration: 'PT0.001490575S',
    });
  });

  it('should add parents as reference', () => {
    const tree = StartupActuatorService.parseAsTree(data);
    const child = tree.getById(8);
    const parent = tree.getById(7);

    expect.assertions(2);
    expect(child.startupStep.parent).toBe(parent);
    expect(child.startupStep.depth).toBe(3);
  });

  it('should add children as reference', () => {
    const tree = StartupActuatorService.parseAsTree(data);
    const parent = tree.getById(7);
    const child = tree.getById(8);

    expect.assertions(1);
    expect(parent.startupStep.children).toContain(child);
  });

  it('should extract map from event tags', () => {
    const tag = {
      key: 'event',
      value:
        'ServletRequestHandledEvent: url=[/applications]; client=[0:0:0:0:0:0:0:1]; method=[GET, POST]; servlet=[dispatcherServlet]; session=[null]; user=[null]; time=[13ms]; status=[OK]',
    };

    const parsedTag = StartupActuatorService.parseTag(tag);

    expect.assertions(1);
    expect(parsedTag).toStrictEqual({
      ...tag,
      parsed: {
        eventName: 'ServletRequestHandledEvent',
        url: ['/applications'],
        client: ['0:0:0:0:0:0:0:1'],
        method: ['GET', 'POST'],
        servlet: ['dispatcherServlet'],
        time: ['13ms'],
        status: ['OK'],
        session: ['null'],
        user: ['null'],
      },
    });
  });

  it('should parse tags when iterating startupSteps', () => {
    const tree = StartupActuatorService.parseAsTree(data);
    const children = tree.getByParentId(6);

    expect.assertions(2);
    expect(children.length).toBe(25);
    expect(children.map((event) => event.startupStep.id)).toStrictEqual([
      7, 9, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 24, 25, 26, 27, 28, 31, 32,
      34, 35, 36, 37, 38, 43,
    ]);
  });

  it('should find start and end time', () => {
    const tree = StartupActuatorService.parseAsTree(data);
    const startTime = tree.getStartTime();
    const endTime = tree.getEndTime();

    expect.assertions(2);
    expect(startTime).toBe(Date.parse('2020-12-10T21:53:41.836728041Z'));
    expect(endTime).toBe(Date.parse('2020-12-10T22:13:47.495769441Z'));
  });

  it('should return progress of event in context of whole tree', () => {
    const tree = StartupActuatorService.parseAsTree(data);
    const period = tree.getPeriod(tree.getById(1));

    expect.assertions(2);
    expect(period.start).toBe(0);
    expect(period.end).toBe(0.000038153408219073554);
  });

  it('should return the path in tree for a given id (no pun intended)', () => {
    const tree = StartupActuatorService.parseAsTree(data);

    const path = tree.getPath(10);
    expect.assertions(1);
    expect(path).toEqual([10, 9, 6, 5]);
  });

  it('should parse duration to seconds', () => {
    const tree = StartupActuatorService.parseAsTree(data);
    const event = tree.getById(1);

    expect.assertions(1);
    expect(event.duration).toBe(45.279861);
  });
});
