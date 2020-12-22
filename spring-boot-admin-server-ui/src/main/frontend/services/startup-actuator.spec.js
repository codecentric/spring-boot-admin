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

import {StartupActuatorService} from './startup-actuator'
import {cloneDeep} from 'lodash';



describe('StartupActuatorService', () => {
  let data = {};
  let events = {};

  beforeEach(() => {
    data = cloneDeep(require('./startup-actuator.fixture.spec.json'));
    events = data.timeline.events;
  });

  it('should find element by id', () => {
    let item8 = StartupActuatorService.getById(events, 8);

    expect(item8).toEqual({
      'startupStep': {
        'name': 'spring.beans.instantiate',
        'id': 8,
        'parentId': 7,
        'tags': [
          {
            'key': 'beanName',
            'value': 'org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory'
          }
        ]
      },
      'startTime': '2020-12-10T21:53:42.958550077Z',
      'endTime': '2020-12-10T21:53:42.960040652Z',
      'duration': 'PT0.001490575S'
    })
  });

  it('should add parents as reference', () => {
    let tree = StartupActuatorService.parseAsTree(data);
    let child = tree.getById(8);
    let parent = tree.getById(7);

    expect(child.startupStep.parent).toBe(parent);
    expect(child.startupStep.depth).toBe(3);
  });

  it('should add children as reference', () => {
    let tree = StartupActuatorService.parseAsTree(data);
    let parent = tree.getById(7);
    let child = tree.getById(8);

    expect(parent.startupStep.children).toContain(child);
  });

  it('should extract map from event tags', () => {
    let tag = {
      'key': 'event',
      'value': 'ServletRequestHandledEvent: url=[/applications]; client=[0:0:0:0:0:0:0:1]; method=[GET, POST]; servlet=[dispatcherServlet]; session=[null]; user=[null]; time=[13ms]; status=[OK]'
    };

    let parsedTag = StartupActuatorService.parseTag(tag);

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
      }
    });
  });

  it('should parse tags when iterating startupSteps', () => {
    let tree = StartupActuatorService.parseAsTree(data);
    let children = tree.getByParentId(6);

    expect(children.length)
      .toBe(25);
    expect(children.map((event) => event.startupStep.id))
      .toStrictEqual([7, 9, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 24, 25, 26, 27, 28, 31, 32, 34, 35, 36, 37, 38, 43]);
  });

  it('should find start and end time', () => {
    let tree = StartupActuatorService.parseAsTree(data);
    let startTime = tree.getStartTime();
    let endTime = tree.getEndTime();

    expect(startTime).toBe(Date.parse('2020-12-10T21:53:41.836728041Z'))
    expect(endTime).toBe(Date.parse('2020-12-10T22:13:47.495769441Z'))
  });

  it('should return progress of event in context of whole tree', () => {
    let tree = StartupActuatorService.parseAsTree(data);
    let period = tree.getPeriod(tree.getById(1));

    expect(period.start).toBe(0);
    expect(period.end).toBe(0.000038153408219073554);
  });

  it('should return the path in tree for a given id (no pun intended)', () => {
    let tree = StartupActuatorService.parseAsTree(data);

    let path = tree.getPath(10);
    expect(path).toEqual([10, 9, 6, 5])
  });

  it('should parse duration to seconds', () => {
    let tree = StartupActuatorService.parseAsTree(data);
    let event = tree.getById(1);

    expect(event.duration).toBe(45.279861);
  });
});
