import { ALGOLIA } from "../consts";
import { getAdocs } from "./adocs.loader";
import algoliasearch from "algoliasearch";

const client = algoliasearch(ALGOLIA.appId, process.env.ALGOLIA_WRITE_KEY!);

let adocs = await getAdocs("../content/adocs/");
const data = adocs
  .map((adoc) => {
    return {
      objectID: adoc.props.slug,
      slug: adoc.props.slug,
      title: adoc.props.data.title,
      type: adoc.props.data.title,
      content: adoc.props.render().Content.toString().slice(0, 5000),
    };
  })
  .filter((entry) => entry.objectID);

let parse = JSON.parse(JSON.stringify(data));
try {
  const res = await client
    .initIndex(ALGOLIA.indexName)
    .saveObjects(parse)
    .then((res) => console.log(res));
  console.log("res", res);
} catch (err) {
  console.error("error", err);
}
