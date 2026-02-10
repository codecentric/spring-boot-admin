import React from "react";
import clsx from "clsx";
import Link from "@docusaurus/Link";
import { findFirstSidebarItemLink, useDocById } from "@docusaurus/plugin-content-docs/client";
import { usePluralForm } from "@docusaurus/theme-common";
import isInternalUrl from "@docusaurus/isInternalUrl";
import { translate } from "@docusaurus/Translate";
import Heading from "@theme/Heading";
import styles from "./styles.module.css";
import { Icon } from "@iconify/react";

const ICON_MAP = {
  apps: <Icon icon='mdi:apps' height='24' />,
  "arrow-up": <Icon icon='mdi:arrow-up' height='24' />,
  bell: <Icon icon='mdi:bell' height='24' />,
  book: <Icon icon='mdi:book-open-page-variant' height='24' />,
  category: <Icon icon='mdi:folder-outline' height='24' />,
  cloud: <Icon icon='mdi:cloud' height='24' />,
  configuration: <Icon icon='mdi:wrench' height='24' />,
  database: <Icon icon='mdi:database' height='24' />,
  features: <Icon icon='mdi:function-variant' height='24' />,
  "file-code": <Icon icon='mdi:file-code' height='24' />,
  home: <Icon icon='mdi:home' height='24' />,
  http: <Icon icon='mdi:web' height='24' />,
  link: <Icon icon='mdi:link-variant' height='24' />,
  notifications: <Icon icon='mdi:bell-ring' height='24' />,
  package: <Icon icon='mdi:package-variant' height='24' />,
  properties: <Icon icon='mdi:cog' height='24' />,
  puzzle: <Icon icon='mdi:puzzle' height='24' />,
  python: <Icon icon='mdi:language-python' height='24' />,
  rocket: <Icon icon='mdi:rocket-launch' height='24' />,
  server: <Icon icon='mdi:server-outline' height='24' />,
  shield: <Icon icon='mdi:shield' height='24' />,
  ui: <Icon icon='mdi:monitor-dashboard' height='24' />,
  wrench: <Icon icon='mdi:wrench' height='24' />
};

function useCategoryItemsPlural() {
  const { selectMessage } = usePluralForm();
  return (count) =>
    selectMessage(
      count,
      translate(
        {
          message: "1 item|{count} items",
          id: "theme.docs.DocCard.categoryDescription.plurals",
          description:
            "The default description for a category card in the generated index about how many items this category includes"
        },
        { count }
      )
    );
}

function CardContainer({ href, children }) {
  return (
    <Link
      href={href}
      className={clsx("card padding--lg", styles.cardContainer)}
    >
      {children}
    </Link>
  );
}

function CardLayout({ href, icon, title, description }) {
  return (
    <CardContainer href={href}>
      <Heading
        as='h2'
        className={clsx("text--truncate", styles.cardTitle)}
        title={title}
      >
        {icon} {title}
      </Heading>
      {description && (
        <p
          className={clsx("text--truncate", styles.cardDescription)}
          title={description}
        >
          {description}
        </p>
      )}
    </CardContainer>
  );
}

export default function DocCard({ item }) {
  switch (item.type) {
    case "link":
      return <CardLink item={item} />;
    case "category":
      return <CardCategory item={item} />;
    default:
      throw new Error(`unknown item type ${JSON.stringify(item)}`);
  }
}

function CardCategory({ item }) {
  const href = findFirstSidebarItemLink(item);
  const categoryItemsPlural = useCategoryItemsPlural();
  // Unexpected: categories that don't have a link have been filtered upfront
  if (!href) {
    return null;
  }
  return (
    <CardLayout
      href={href}
      icon={ICON_MAP["category"]}
      title={item.label}
      description={item.description ?? categoryItemsPlural(item.items.length)}
    />
  );
}

function CardLink({ item }) {
  const doc = useDocById(item.docId ?? undefined);

  return (
    <>
      <CardLayout
        href={item.href}
        icon={ICON_MAP[item?.customProps?.icon] ?? (isInternalUrl(item.href) ? "📄️" : "🔗")}
        title={item.label}
        description={item.description ?? doc?.description}
      />
    </>
  );
}
