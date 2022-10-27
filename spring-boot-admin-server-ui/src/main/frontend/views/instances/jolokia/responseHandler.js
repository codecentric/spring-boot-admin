export const STATE_PREPARED = 'prepared';
export const STATE_INPUT_ARGS = 'input-args';
export const STATE_EXECUTING = 'executing';
export const STATE_FAILED = 'failed';
export const STATE_COMPLETED = 'completed';

function resultContainsErrorStatus(data) {
  return data.status >= 400;
}

function responseDataHandler(data) {
  if (resultContainsErrorStatus(data)) {
    let failedRequest = data;

    const error = new Error(`Execution failed: ${failedRequest.error}`);
    error.stacktrace = failedRequest.stacktrace;
    console.warn('Invocation failed', error);

    return {
      state: STATE_FAILED,
      error
    }
  } else {
    return {
      result: data.value,
      state: STATE_COMPLETED
    }
  }
}

function handleInstanceData(data) {
  if ('body' in data) {
    return responseDataHandler(JSON.parse(data.body));
  } else {
    const error = new Error(`Execution failed with HTTP error: ${data.status}`);
    console.warn('Invocation failed', error);
    return {
      state: STATE_FAILED,
      error
    };
  }
}

export function responseHandler(result) {
  if (Array.isArray(result.data)) {
    return result.data.map(d => ({
        instanceId: d.instanceId,
        ...handleInstanceData(d)
    }));
  } else {
    return responseDataHandler(result.data);
  }
}
