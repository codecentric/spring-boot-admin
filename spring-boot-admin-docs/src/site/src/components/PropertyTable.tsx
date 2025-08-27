import { filterPropertiesByName } from "@site/src/propertiesUtil";
import styles from "./PropertyTable.module.css"
import { CopyButton } from "@site/src/components/CopyButton";

type Props = {
  title?: string;
  properties: Array<Record<string, any>>
  filter?: Array<string>
  exclusive?: boolean
}

export function PropertyTable({title, properties, filter = [], exclusive = true}: Props) {
  const filteredProperties = filterPropertiesByName(properties, filter, exclusive)
    .sort((a,b) => {
      return a.name.length - b.name.length || a.name.localeCompare(b.name);
    });

  return (
    <table className={styles.propertyTable}>
      {title && <caption>{title}</caption>}
      <thead>
      <tr>
        <th>Property</th>
        <th>Description</th>
      </tr>
      </thead>
      <tbody>
      {filteredProperties.map((a) => (
        <>
          <tr key={a.name}>
            <td className={styles.propertyCell}>
              <div>
                <CopyButton text={a.name} />
                <code>
                  {a.name}
                </code>
              </div>
            </td>
            <td>
              <p>
                {a.description}
              </p>
              {a.defaultValue && (
                <dl className='dl-horizontal'>
                  <div>
                    <dt><span style={{ fontStyle: "italic" }}>Type:</span>&nbsp;</dt>
                    <dd><code>{a.type}</code></dd>
                  </div>
                  <div>
                    <dt><span style={{ fontStyle: "italic" }}>Default:</span>&nbsp;</dt>
                    <dd><code>{JSON.stringify(a.defaultValue)}</code></dd>
                  </div>
                </dl>
              )}
            </td>
          </tr>
        </>
      ))}
      </tbody>
    </table>
  );
}
