type Options = {
  maxLen?: number;
};

export const useClassnameShortener = (options?: Options) => {
  return {
    truncateClassname: (fqcn: string) =>
      abbreviateLoggerName(fqcn, options?.maxLen),
  };
};

/**
 * Abbreviate a fully qualified Java class name like Spring Boot logging does.
 *
 * Examples:
 *  - org.springframework.boot.web.embedded.tomcat.TomcatWebServer
 *    -> o.s.b.w.e.tomcat.TomcatWebServer
 *
 *  - com.example.very.long.package.name.MyService with maxLen=30
 *    -> c.e.v.l.p.name.MyService  (then drops leftmost segments if needed)
 *
 * @param fqcn   Fully-qualified class name (e.g., "org.example.FooBar")
 * @param maxLen Maximum length of the resulting string
 * @returns Abbreviated name
 */
function abbreviateLoggerName(fqcn: string, maxLen = 40): string {
  const input = (fqcn || '').trim();
  if (!input) return '';

  if (input.length <= maxLen) return input; // already fits

  const parts = input.split('.');
  if (parts.length === 1) {
    // No package, just crop if needed
    return input.slice(input.length - maxLen);
  }

  const simpleName = parts[parts.length - 1];
  let packages = parts.slice(0, -1);

  // 1. Start with full name
  let out = packages.join('.') + '.' + simpleName;

  // 2. Abbreviate left-to-right
  for (let i = 0; i < packages.length && out.length > maxLen; i++) {
    packages[i] = packages[i].charAt(0); // abbreviate one package
    out = packages.join('.') + '.' + simpleName;
    if (out.length <= maxLen) return out;
  }

  // 3. If still too long, drop leftmost package segments
  while (packages.length > 0 && out.length > maxLen) {
    packages = packages.slice(1);
    out = packages.join('.') + (packages.length ? '.' : '') + simpleName;
    if (out.length <= maxLen) return out;
  }

  // 4. Fallback: left-crop
  return out.slice(out.length - maxLen);
}
