import { getAdoc, getAdocFiles } from "astro-adoc";
import path from "path";

const PATH = path.resolve("../../../spring-boot-admin-samples/");

const ASCIIDOC_OPTIONS = {
  safe: "unsafe",
  attributes: {
    "samples-dir": PATH,
    "github-src":
      "https://github.com/codecentric/spring-boot-admin/tree/master",
  },
};
const headerRegex =
  /.*<h(?<depth>[0-9]).*(id="(?<slug>.*)").*>(?<text>.*)<\/h[0-9]>.*/g;

export async function getAdocs(dir: string = "src/content/adocs/") {
  const adocFiles = await getAdocFiles(dir);
  return adocFiles
    .map((file) => {
      let path = file.replace(".adoc", "");
      if (path.endsWith("/index")) path = path.replace("/index", "");
      path = path.replace(dir, "");
      const adoc = getAdoc(file, ASCIIDOC_OPTIONS);

      return {
        path,
        adoc,
      };
    })
    .map(({ path, adoc }) => {
      const converted = adoc.convert();
      const headings = [];
      let result: RegExpExecArray | null;
      do {
        result = headerRegex.exec(converted);
        if (result && result.groups) {
          headings.push({
            depth: result.groups.depth,
            slug: result.groups.slug,
            text: result.groups.text,
          });
        }
      } while (result != null);

      return {
        params: { slug: path },
        props: {
          id: `${path}.adoc`,
          slug: path,
          data: {
            title: adoc.getTitle(),
          },
          render() {
            return {
              Content: converted,
              headings,
            };
          },
        },
      };
    });
}
