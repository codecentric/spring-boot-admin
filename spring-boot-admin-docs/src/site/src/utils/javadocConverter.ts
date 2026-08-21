/**
 * Converts Javadoc tags to HTML equivalents for rendering in Docusaurus.
 *
 * Handles:
 * - {@code ...} → <code>...</code>
 * - {@link ...} → <code>...</code>
 * - {@linkplain ...} → <code>...</code>
 * - {@value ...} → <...> (constant reference, no special formatting)
 * - {@literal ...} → <...> (literal text, no formatting)
 */
export function convertJavadocToHtml(text: string): string {
	if (!text) {
		return text;
	}

	// Replace {@code ...} with <code>...</code>
	let converted = text.replace(/{@code\s+([^}]+)}/g, '<code>$1</code>');

	// Replace {@link ...} with <code>...</code>
	converted = converted.replace(/{@link\s+([^}]+)}/g, '<code>$1</code>');

	// Replace {@linkplain ...} with <code>...</code>
	converted = converted.replace(/{@linkplain\s+([^}]+)}/g, '<code>$1</code>');

	// Replace {@value ...} with plain text (constant reference)
	converted = converted.replace(/{@value\s+([^}]+)}/g, '$1');

	// Replace {@literal ...} with plain text (literal escaping)
	converted = converted.replace(/{@literal\s+([^}]+)}/g, '$1');

	return converted;
}

