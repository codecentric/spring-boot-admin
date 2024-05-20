import fs, { writeFileSync } from "node:fs";
import { resolve } from "node:path";
import asciidoctor from "asciidoctor";
import { globSync } from "glob";
import { NodeHtmlMarkdown } from "node-html-markdown";

const Asciidoctor = asciidoctor();
const nhm = new NodeHtmlMarkdown({
  useInlineLinks: false,
});
nhm.tableCellTranslators.set('p', ({ visitor }) => ({
  noEscape: true,
  postprocess: ({ content }) => {
    return content
      .replace(/\$/g, "&#36;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\{/g, "&#123;")
      .replace(/}/g, "&#125;");
  }
}), true);

const PATH = resolve("../../../spring-boot-admin-samples");
const ADOC_ROOT = "adoc";
const ASCIIDOC_OPTIONS = {
  safe: "unsafe",
  backend: "xhtml5",
  sourcemap: true,
  to_file: true,
  attributes: {
    "samples-dir": PATH,
    "project-version": "readVersionFromPom1",
    "github-src":
      "https://github.com/codecentric/spring-boot-admin/tree/master",
    "base-path": "/"
  }
};

const HEADER = {
  'getting-started': `sidebar_position: 1`
}

export async function processAdocs() {
  const outputFolder = `docs/`;

  globSync("**/*.md", { cwd: outputFolder })
    .forEach((file) => fs.unlinkSync(`${outputFolder}${file}`));

  globSync("**/*.adoc", { cwd: ADOC_ROOT })
    .filter((file) => !file.startsWith("_"))
    .forEach((file) => {
      const outputFilePath = file.split(".").slice(0, -1).join(".");
      const subfolder = outputFilePath.split("/").slice(0, -1).join("/");
      if (subfolder.length > 0) {
        fs.mkdirSync(`docs/${subfolder}`, { recursive: true });
      }

      let content = getAdocAsHtml(file);
      let markdown  = nhm.translate(content.convert());

      if(content.getAttribute('doctitle')) {
        markdown = `# ${content.getAttribute('doctitle')}\n\n` + markdown;
      }
      if(HEADER[outputFilePath]) {
        markdown = `---\n${HEADER[outputFilePath]}\n---\n` + markdown;
      }
      writeFileSync(`${outputFolder}${outputFilePath}.md`, markdown, "utf-8");
    });

  return;
}

function getAdocAsHtml(file: string) {
  const adocFile = `${ADOC_ROOT}/${file}`;
  return Asciidoctor.loadFile(adocFile, ASCIIDOC_OPTIONS);
}


export const trimNewLines = (s: string) => s.replace(/^\n+|\n+$/g, "");
