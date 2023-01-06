/* eslint-disable no-undef */
class HealthStatusEnum {
  static DOWN = 'DOWN';
  static UP = 'UP';
  static RESTRICTED = 'RESTRICTED';
  static UNKNOWN = 'UNKNOWN';
  static OUT_OF_SERVICE = 'OUT_OF_SERVICE';
  static OFFLINE = 'OFFLINE';
}

export const HealthStatus = Object.freeze(HealthStatusEnum);
