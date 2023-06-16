export const STATE_PREPARED = 'prepared';
export const STATE_INPUT_ARGS = 'input-args';
export const STATE_EXECUTING = 'executing';
export const STATE_FAILED = 'failed';
export const STATE_COMPLETED = 'completed';

export function resultContainsErrorStatus(result) {
  if (result.status >= 400) {
    return true;
  }

  const parsedResponse = parseValue(result.data);
  if (Array.isArray(parsedResponse)) {
    return parsedResponse.some((r) => r.status >= 400);
  } else {
    return result.data.status >= 400;
  }
}

export function parseValue(data) {
  if (Array.isArray(data)) {
    return data.map((elem) => {
      const parsedBody = JSON.parse(elem['body']);
      return {
        instanceId: elem['instanceId'],
        ...parsedBody,
      };
    });
  } else {
    return data.value;
  }
}

export function responseHandler(result) {
  if (resultContainsErrorStatus(result)) {
    const parsedResult = parseValue(result.data);
    let failedRequest = result.data;

    // Show first failed request
    if (Array.isArray(result.data)) {
      failedRequest = parsedResult[0];
    }

    const error = new Error(`Execution failed: ${failedRequest.error}`);
    error.stacktrace = failedRequest.stacktrace;
    console.warn('Invocation failed', error);

    return {
      state: STATE_FAILED,
      error,
    };
  } else {
    return {
      result: parseValue(result.data),
      state: STATE_COMPLETED,
    };
  }
}
