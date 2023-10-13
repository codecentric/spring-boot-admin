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
    return this.getEvents().filter(
      (event) => event.startupStep.depth === depth,
    );
  }

  getById(id) {
    return this.getEvents().find((event) => event.startupStep.id === id);
  }

  getByParentId(parentId) {
    return this.getEvents().filter(
      (event) => event.startupStep.parentId === parentId,
    );
  }

  getStartTime() {
    return this.getEvents()
      .map((e) => Date.parse(e.startTime))
      .reduce((a, b) => Math.min(a, b), Number.MAX_VALUE);
  }

  getEndTime() {
    return this.getEvents()
      .map((e) => Date.parse(e.endTime))
      .reduce((a, b) => Math.max(a, b), Number.MIN_VALUE);
  }

  getPath(id) {
    const event = this.getById(id);
    if (!event) {
      return [];
    }

    const path = [id];
    let parent = event.startupStep.parent;
    while (parent !== null && parent !== undefined) {
      path.push(parent.startupStep.id);
      parent = parent.startupStep.parent;
    }

    return path;
  }

  getPeriod(event) {
    const eventStartTime = Date.parse(event.startTime);
    const eventEndTime = Date.parse(event.endTime);
    const treeStartTime = this.getStartTime();
    const treeEndTime = this.getEndTime();

    const treeTimeSpan = treeEndTime - treeStartTime;
    const relativeEventStartTime = eventStartTime - treeStartTime;
    const relativeEventEndTime = eventEndTime - treeStartTime;
    const relativeStart =
      relativeEventStartTime > 0 ? relativeEventStartTime / treeTimeSpan : 0;
    const relativeEnd =
      relativeEventEndTime > 0 ? relativeEventEndTime / treeTimeSpan : 0;

    return {
      start: relativeStart,
      end: relativeEnd,
    };
  }
}
