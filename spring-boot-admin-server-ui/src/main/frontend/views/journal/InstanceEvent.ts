export class InstanceEvent {
  public readonly instance: string;
  public readonly version: string;
  public readonly type: string;
  public readonly timestamp: Date;
  public readonly payload: any;

  constructor({ instance, version, type, timestamp, ...payload }) {
    this.instance = instance;
    this.version = version;
    this.type = type;
    this.timestamp = new Date(timestamp);
    this.payload = payload;
  }

  get key() {
    return `${this.instance}-${this.version}`;
  }
}
InstanceEvent.STATUS_CHANGED = 'STATUS_CHANGED';
InstanceEvent.REGISTERED = 'REGISTERED';
InstanceEvent.DEREGISTERED = 'DEREGISTERED';
InstanceEvent.REGISTRATION_UPDATED = 'REGISTRATION_UPDATED';
InstanceEvent.INFO_CHANGED = 'INFO_CHANGED';
InstanceEvent.ENDPOINTS_DETECTED = 'ENDPOINTS_DETECTED';
