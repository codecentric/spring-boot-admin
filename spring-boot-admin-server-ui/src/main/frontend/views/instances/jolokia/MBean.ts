import { flatMap, fromPairs } from 'lodash-es';

import { MBeanDescriptor } from './MBeanDescriptor';

export class MBean {
  constructor({ descriptor, op, ...mBean }) {
    Object.assign(this, mBean);
    this.descriptor = new MBeanDescriptor(descriptor);
    const flattenedOps = flatMap(Object.entries(op || {}), ([name, value]) => {
      if (Array.isArray(value)) {
        return value.map((v) => [name, v]);
      } else {
        return [[name, value]];
      }
    }).map(([name, operation]) => [
      getOperationName(name, operation),
      operation,
    ]);
    this.op = flattenedOps.length > 0 ? fromPairs(flattenedOps) : null;
  }
}

const getOperationName = (name, descriptor) => {
  const params = descriptor.args.map((arg) => arg.type).join(',');
  return `${name}(${params})`;
};
