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

import {isNumeric} from 'rxjs/internal-compatibility';

const regex = new RegExp('([^=\\s]*)=\\[([^\\]]*)\\]', 'gi')

export class StartupActuatorEventTree {
  constructor(events) {
    this.events = events;
  }

  getEvents() {
    return this.events || [];
  }

  getRoots() {
    return this.getByDepth(0);
  }

  getByDepth(depth) {
    return this.getEvents().filter((event) => event.startupStep.depth === depth)
  }

  getById(id) {
    return this.getEvents().find((event) => event.startupStep.id === id)
  }

  getByParentId(parentId) {
    return this.getEvents().filter((event) => event.startupStep.parentId === parentId)
  }

  getStartTime() {
    return this.getEvents().map(e => Date.parse(e.startTime)).reduce((a, b) => Math.min(a, b), Number.MAX_VALUE)
  }

  getEndTime() {
    return this.getEvents().map(e => Date.parse(e.endTime)).reduce((a, b) => Math.max(a, b), Number.MIN_VALUE)
  }

  getPeriod(event) {
    const eventStartTime = Date.parse(event.startTime);
    const eventEndTime = Date.parse(event.endTime)
    const treeStartTime = this.getStartTime();
    const treeEndTime = this.getEndTime();

    const treeTimeSpan = treeEndTime - treeStartTime
    const relativeEventStartTime = eventStartTime - treeStartTime;
    const relativeEventEndTime = eventEndTime - treeStartTime;
    const relativeStart = relativeEventStartTime > 0 ? relativeEventStartTime / treeTimeSpan : 0;
    const relativeEnd = relativeEventEndTime > 0 ? relativeEventEndTime / treeTimeSpan : 0;

    return {
      start: relativeStart,
      end: relativeEnd,
    };
  }
}

export const StartupActuatorService = {
  parseAsTree(data) {
    let events = data.timeline.events || [];
    let eventsForTree = events
      .sort((a, b) => a.startupStep.id - b.startupStep.id)
      .map((event) => {
        event.startupStep.parent = this.getById(events, event.startupStep.parentId);
        event.startupStep.tags = event.startupStep.tags.map(this.parseTag)
        event.startupStep.depth = 0;
        event.startupStep.children = this.getByParentId(events, event.startupStep.id);
        event.duration = isNumeric(event.duration) ? event.duration : Number.parseFloat(event.duration.replace(/[\w]*/, ''))
        return event;
      })
      .map((event) => {
        let parent = event.startupStep.parent;
        while (parent !== null && parent !== undefined) {
          parent = parent.startupStep.parent;
          event.startupStep.depth++;
        }

        return event;
      });

    return new StartupActuatorEventTree(eventsForTree);
  },
  getById(events, id) {
    return (events || [])
      .find((event) => event.startupStep.id === id)
  },
  getByParentId(events, id) {
    return (events || [])
      .filter((event) => event.startupStep.parentId === id)
  },
  parseTag(param) {
    if (param.key === 'event') {
      const parsed = {};
      parsed['eventName'] = param.value.split(':')[0];

      const matcher = param.value.matchAll(regex);
      for (const match of matcher) {
        parsed[match[1]] = match[2].split(',').map((s) => s.trim())
      }
      param.parsed = parsed;
    }

    return param;
  },
}
