/**
 * Filters an array of Spring property definitions by their names based on provided keywords.
 *
 * @param properties - The array of Spring property definitions to filter
 * @param keywords - The array of keywords to search for in property names
 * @param includeOnly - If true, includes only properties matching keywords; if false, excludes matching properties (default: false)
 * @returns A filtered array of property definitions
 */
export const filterPropertiesByName = (
  properties: Array<SpringPropertyDefinition>,
  keywords: string[],
  includeOnly: boolean = false
) => {
  if (!includeOnly) {
    return properties.filter(property => !containsKeywordIgnoreCase(property.name, keywords));
  }

  return properties.filter(property => containsKeywordIgnoreCase(property.name, keywords));
};

function containsKeywordIgnoreCase(str: string, keywords: string[]): boolean {
  const searchContext = str.toLowerCase();
  return keywords.some(keyword => {
    const searchTerm = keyword.toLowerCase();
    const isIncluded = searchContext.includes(searchTerm);
    return isIncluded;
  });
}
