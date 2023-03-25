export {};

declare global {

  type View = InternalView | ExternalView;

  interface InternalView {
    name: string;
    path: string;
    order: number;

    [key: string]: any;
  }

  interface ExternalView {
    href: string;
  }

  type HealthStatus = 'DOWN' |
    'UP' |
    'RESTRICTED' |
    'UNKNOWN' |
    'OUT_OF_SERVICE' |
    'OFFLINE';
}
