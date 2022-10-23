export class MBeanDescriptor {
  constructor(raw) {
    Object.assign(this, MBeanDescriptor.parse(raw));
    this.raw = raw;
  }

  static parse(raw) {
    const attributes = raw
      .split(',')
      .map((attribute) => attribute.split('='))
      .map(([name, value]) => ({ name, value }));
    const displayName = attributes
      .map(({ value }) => value)
      .join(' ')
      .trim();
    return { attributes, displayName };
  }
}
