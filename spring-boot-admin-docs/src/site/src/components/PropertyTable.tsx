import { filterPropertiesByName } from "@site/src/propertiesUtil";
import styles from "./PropertyTable.module.css";
import { CopyButton } from "@site/src/components/CopyButton";

type Props = {
  title?: string;
  properties: Array<SpringPropertyDefinition>
  filter?: Array<string>
  exclusive?: boolean,
  additionalProperties: Array<SpringPropertyDefinition>
}

export function PropertyTable({
                                title,
                                properties,
                                filter = [],
                                exclusive = true,
                                additionalProperties = [] as Array<SpringPropertyDefinition>,
                              }: Readonly<Props>) {
  const filteredProperties = filterPropertiesByName(properties, filter, exclusive)
    .sort((a, b) => {
      return a.name.length - b.name.length || a.name.localeCompare(b.name);
    });

  const propertiesToShow = [
    ...filteredProperties,
    ...additionalProperties
  ];

  const hasDefaultValueOrType = (property: SpringPropertyDefinition) => {
    console.log(property.defaultValue, typeof property.defaultValue);
    console.log(property.type, typeof property.type);
    return property.defaultValue || property.type
  }

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
      {propertiesToShow.map((a) => (
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
              <p dangerouslySetInnerHTML={{__html: a.description}} />
              {hasDefaultValueOrType(a) && (
                <dl>
                  {a.type && (
                    <div>
                      <dt><span style={{ fontStyle: "italic" }}>Type:</span>&nbsp;</dt>
                      <dd><code>{a.type}</code></dd>
                    </div>
                  )}
                  {a.defaultValue && (
                    <div>
                      <dt><span style={{ fontStyle: "italic" }}>Default:</span>&nbsp;</dt>
                      <dd><code>{JSON.stringify(a.defaultValue)}</code></dd>
                    </div>
                  )}
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
