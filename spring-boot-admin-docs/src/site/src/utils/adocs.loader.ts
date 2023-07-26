import { getAdoc, getAdocFiles } from "astro-adoc";
import merge from "lodash.merge";
import path from "path";
import { readVersionFromPom } from "../../astro.config.mjs";

const PATH = path.resolve("../../../spring-boot-admin-samples/");

let readVersionFromPom1 = readVersionFromPom();
console.log(readVersionFromPom1);
const ASCIIDOC_OPTIONS = {
  safe: "unsafe",
  attributes: {
    "samples-dir": PATH,
    "project-version": readVersionFromPom1,
    "github-src":
      "https://github.com/codecentric/spring-boot-admin/tree/master",
    "base-path": "/",
  },
};
const headerRegex =
  /.*<h(?<depth>[0-9]).*(id="(?<slug>.*)").*>(?<text>.*)<\/h[0-9]>.*/g;

type AdocsParams = {
  dir?: string;
  asciidocOptions?: any;
};

type Header = {
  depth: string;
  slug: string;
  text: string;
};

export async function getAdocs(opts: AdocsParams) {
  const dir = opts.dir || "src/content/adocs/";

  const adocFiles = await getAdocFiles(dir);
  return adocFiles
    .filter((file) => {
      return !file.split("/").pop()?.startsWith("_");
    })
    .map((file) => {
      let path = file.replace(".adoc", "");
      if (path.endsWith("/index")) path = path.replace("/index", "");
      path = path.replace(dir, "");
      let options = merge(ASCIIDOC_OPTIONS, opts.asciidocOptions);
      const adoc = getAdoc(file, options);

      return {
        path,
        adoc,
      };
    })
    .map(({ path, adoc }) => {
      const converted = adoc.convert();
      const headings: Header[] = [];
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
