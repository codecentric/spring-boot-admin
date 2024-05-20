import clsx from "clsx";
import Heading from "@theme/Heading";
import styles from "./styles.module.css";

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<"svg">>;
  description: JSX.Element;
};

const FeatureList: FeatureItem[] = [
  {
    title: "Monitoring",
    Svg: require("./Monitoring.svg").default,
    description: (
      <>
        Get a fast impression of your Spring Boot's service status and check metrics,
        logs, and more.
      </>
    )
  },
  {
    title: "Logging",
    Svg: require("./Logging.svg").default,
    description: (
      <>
        Access your application's log files and view them in the browser.
        Switch log levels without restarting your application.
      </>
    )
  },
  {
    title: "Memory-Insights",
    Svg: require("./MemoryInsights.svg").default,
    description: (
      <>
        Extend or customize your website layout by implementing your own plugins.
      </>
    )
  },
  {
    title: "Open Source",
    Svg: require("./OpenSource.svg").default,
    description: (
      <>
        Build apon the shoulders of giants: We believe in open source. Spring Boot Admin is free to use and
        build on top Spring Boot's Actuator endpoints.
      </>
    )
  },
  {
    title: "Customization",
    Svg: require("./Cusromization.svg").default,
    description: (
      <>
        Extend or customize your website layout by implementing your own plugins.
      </>
    )
  }
];


export default function HomepageFeatures(): JSX.Element {
  return (
    <section className={styles.features}>
      <div className='container'>
        <div className={clsx("row", styles.justifyCenter)}>
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}

function Feature({ title, Svg, description }: FeatureItem) {
  return (
    <div className={clsx("col col--4")}>
      <div className='text--center'>
        <Svg className={styles.featureSvg} role='img' />
      </div>
      <div className='text--center padding-horiz--md'>
        <Heading as='h2'>{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}
