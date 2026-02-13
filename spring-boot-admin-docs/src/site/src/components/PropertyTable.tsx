import { filterPropertiesByName } from "@site/src/propertiesUtil";
import styles from "./PropertyTable.module.css";
import { CopyButton } from "@site/src/components/CopyButton";

type Props = {
  title?: string;
  properties: Array<SpringPropertyDefinition>;
  filter?: Array<string>;
  includeOnly?: boolean;
  additionalProperties?: Array<SpringPropertyDefinition>;
}

function getFilteredProperties(properties: Array<SpringPropertyDefinition>, filter: Array<string>, includeOnly: boolean) {
  if (filter.length === 0) {
    return properties;
  }

  return filterPropertiesByName(properties, filter, includeOnly)
    .filter((property, index, self) =>
      index === self.findIndex((p) => p.name === property.name)
    )
    .sort((a, b) => {
      return a.name.length - b.name.length || a.name.localeCompare(b.name);
    });
}

export function PropertyTable({
                                title,
                                properties,
                                filter = [],
                                includeOnly = true,
                                additionalProperties = [] as Array<SpringPropertyDefinition>
                              }: Readonly<Props>) {


  const filteredProperties = getFilteredProperties(properties, filter, includeOnly);

  const propertiesToShow = [
    ...filteredProperties,
    ...additionalProperties
  ];

  const hasDefaultValueOrType = (property: SpringPropertyDefinition) => {
    return property.defaultValue || property.type;
  };

  return (
    <table className={styles.propertyTable}>
      {title && <caption>{title}</caption>}
      <thead>
      <tr>
        <th>Property</th>
      </tr>
      </thead>
      <tbody>
      {propertiesToShow.map((a) => (
        <>
          <tr key={a.name}>
            <td className={styles.propertyCell}>
              <div className={styles.propertyBlock}>
                <CopyButton text={a.name} />
                <code>
                  {a.name}
                </code>
              </div>
              <div className={styles.descriptionBlock}>
                <p dangerouslySetInnerHTML={{ __html: a.description }} />
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
              </div>
            </td>
          </tr>
        </>
      ))}
      </tbody>
    </table>
  );
}
