import algoliasearch from "algoliasearch";
import * as dotenv from "dotenv";
// 1. Build a dataset
import fs from "fs";
import matter from "gray-matter";
import path from "path";
import removeMd from "remove-markdown";

dotenv.config();

const client = algoliasearch(
  process.env.ALGOLIA_APP_ID,
  process.env.ALGOLIA_WRITE_API_KEY
);

const filenames = fs.readdirSync(path.join("./src/content"));
const data = filenames.map((filename) => {
  try {
    const markdownWithMeta = fs.readFileSync("./src/posts/" + filename);
    const { data: frontmatter, content } = matter(markdownWithMeta);
    return {
      id: frontmatter.slug,
      title: frontmatter.title,
      content: removeMd(content).replace(/\n/g, ""),
    };
  } catch (e) {
    // console.log(e.message)
  }
});

// 2. Send the dataset in JSON format
//client
//  .initIndex("dev_posts")
//  .saveObjects(JSON.parse(JSON.stringify(data)))
//  .then((res) => console.log(res));
