import { mapKeys } from 'lodash-es';
import moment from 'moment/moment';

const normalize = (obj) => mapKeys(obj, (value, key) => key.toLowerCase());

export class Exchange {
  readonly timestamp: moment.Moment;
  readonly request: {
    uri: string;
    method: string;
    headers: { [key: string]: string[] };
  };
  readonly response: {
    status?: number;
    headers?: { [key: string]: string[] };
  } | null;

  readonly timeTaken?: string;

  constructor({ timestamp, request, response, ...exchange }) {
    Object.assign(this, exchange);
    this.timestamp = moment(timestamp);
    this.request = { ...request, headers: normalize(request.headers) };
    this.response = response
      ? { ...response, headers: normalize(response.headers) }
      : null;
  }

  get key() {
    return `${this.timestamp.valueOf()}-${this.request.method}-${
      this.request.uri
    }`;
  }

  get contentLengthResponse() {
    return this.extractContentLength(this.response);
  }

  get contentLengthRequest() {
    return this.extractContentLength(this.request);
  }

  get contentTypeResponse() {
    return this.extractContentType(this.response);
  }

  get contentTypeRequest() {
    return this.extractContentType(this.request);
  }

  extractContentLength(origin) {
    const contentLength =
      origin &&
      origin.headers['content-length'] &&
      origin.headers['content-length'][0];
    if (contentLength && /^\d+$/.test(contentLength)) {
      return parseInt(contentLength);
    }
    return null;
  }

  extractContentType(origin) {
    const contentType =
      origin &&
      origin.headers['content-type'] &&
      origin.headers['content-type'][0];
    if (contentType) {
      const idx = contentType.indexOf(';');
      return idx >= 0 ? contentType.substring(0, idx) : contentType;
    }
    return null;
  }

  compareTo(other: Exchange) {
    return this.timestamp.isAfter(other.timestamp);
  }

  isPending() {
    return !this.response;
  }

  isSuccess() {
    return this.response && this.response.status <= 399;
  }

  isClientError() {
    return (
      this.response &&
      this.response.status >= 400 &&
      this.response.status <= 499
    );
  }

  isServerError() {
    return (
      this.response &&
      this.response.status >= 500 &&
      this.response.status <= 599
    );
  }
}
