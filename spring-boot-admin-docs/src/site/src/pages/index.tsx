import clsx from "clsx";
import Link from "@docusaurus/Link";
import useDocusaurusContext from "@docusaurus/useDocusaurusContext";
import Layout from "@theme/Layout";
import HomepageFeatures from "@site/src/components/HomepageFeatures";
import Heading from "@theme/Heading";
import { Icon } from '@iconify/react';

import styles from "./index.module.css";

export default function Home(): JSX.Element {
  const { siteConfig } = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description=' Description will go into a meta tag in <head />'>
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}

function HomepageHeader() {
  const { siteConfig } = useDocusaurusContext();

  return (
    <header className={clsx("hero hero--primary", styles.heroBanner)}>
      <div className='container'>
        <Heading as='h1' className='hero__title'>
          {siteConfig.title}
        </Heading>
        <p className='hero__subtitle' dangerouslySetInnerHTML={{ __html: siteConfig.tagline }} />
        <div className={styles.buttons}>
          <Link
            className=' button button--secondary button--lg justify-center gap-1'
            to='/docs/getting-started'>
            Getting started
          </Link>
          <Link
            className='button button--secondary button--lg justify-center gap-1'
            href={"https://github.com/codecentric/spring-boot-admin"}>
            GitHub
            <Icon icon='fa:github-square' />
          </Link>
        </div>
      </div>
    </header>
  );
}
