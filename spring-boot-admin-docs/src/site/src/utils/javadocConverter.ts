/**
 * Converts Javadoc tags to HTML equivalents for rendering in Docusaurus.
 * 
 * Handles:
 * - {@code ...} → <code>...</code>
 * - {@link ClassName} → <code>ClassName</code>
 * - {@link ClassName#method} → <code>ClassName#method</code>
 */
export function convertJavadocToHtml(text: string): string {
	if (!text) {
		return text;
	}

	// Replace {@code ...} with <code>...</code>
	let converted = text.replace(/{@code\s+([^}]+)}/g, '<code>$1</code>');

	// Replace {@link ...} with <code>...</code>
	converted = converted.replace(/{@link\s+([^}]+)}/g, '<code>$1</code>');

	return converted;
}
