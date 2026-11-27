export const STATE_PREPARED = 'prepared';
export const STATE_INPUT_ARGS = 'input-args';
export const STATE_EXECUTING = 'executing';
export const STATE_FAILED = 'failed';
export const STATE_COMPLETED = 'completed';

function failedResponse(result, parsedValue) {
  if (result.status >= 400) return result.data || result;
  if (Array.isArray(result.data)) {
    return parsedValue.find((response) => response.status >= 400);
  }
  return result.data?.status >= 400 ? result.data : undefined;
}

export function resultContainsErrorStatus(result) {
  return Boolean(failedResponse(result, parseValue(result.data)));
}

export function parseValue(data) {
  if (Array.isArray(data)) {
    return data.map(({ instanceId, body, status }) => {
      let response;
      try {
        response = JSON.parse(body);
      } catch (error) {
        if (status < 400) throw error;
      }
      return {
        ...response,
        instanceId,
        status: status >= 400 ? status : response?.status,
      };
    });
  }
  return data?.value;
}

export function responseHandler(result) {
  const value = parseValue(result.data);
  const failure = failedResponse(result, value);
  if (failure) {
    const reason =
      failure.error ??
      (failure.instanceId
        ? `HTTP ${failure.status} on instance ${failure.instanceId}`
        : `HTTP ${result.status}`);
    const error = Object.assign(new Error(`Execution failed: ${reason}`), {
      stacktrace: failure.stacktrace,
    });
    console.warn('Invocation failed', error);
    return { state: STATE_FAILED, error };
  }
  return { result: value, state: STATE_COMPLETED };
}
