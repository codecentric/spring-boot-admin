import { getCollection } from "astro:content";

export async function getDocs() {
  const docFiles = (await getCollection("docs")) as any[];

  return docFiles.map((entry) => ({
    params: {
      slug: entry.slug,
    },
    props: entry,
  }));
}
