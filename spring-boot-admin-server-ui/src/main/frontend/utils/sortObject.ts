export function sortObject(input: any): any {
  if (Array.isArray(input)) {
    return input.map(sortObject);
  } else if (input !== null && typeof input === 'object') {
    return Object.keys(input)
      .sort()
      .reduce(
        (acc, key) => {
          acc[key] = sortObject(input[key]);
          return acc;
        },
        {} as Record<string, any>,
      );
  }
  return input;
}
