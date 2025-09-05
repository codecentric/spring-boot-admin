export const filterPropertiesByName = (
  properties: Array<SpringPropertyDefinition>,
  keywords: string[],
  exclusive: boolean = true
) => {
  if (exclusive) {
    return properties.filter(property => !containsKeywordIgnoreCase(property.name, keywords));
  }

  return properties.filter(property => containsKeywordIgnoreCase(property.name, keywords));
};

function containsKeywordIgnoreCase(str: string, keywords: string[]): boolean {
  const lowerStr = str.toLowerCase();
  return keywords.some(keyword => lowerStr.includes(keyword.toLowerCase()));
}
