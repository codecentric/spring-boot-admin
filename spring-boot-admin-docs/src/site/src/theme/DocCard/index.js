import React from "react";
import clsx from "clsx";
import Link from "@docusaurus/Link";
import { findFirstSidebarItemLink, useDocById } from "@docusaurus/plugin-content-docs/client";
import { usePluralForm } from "@docusaurus/theme-common";
import isInternalUrl from "@docusaurus/isInternalUrl";
import { translate } from "@docusaurus/Translate";
import Heading from "@theme/Heading";
import styles from "./styles.module.css";
import { Icon } from '@iconify/react';

const ICON_MAP = {
  ui: <Icon icon="gg:ui-kit" height="24" />,
  http: <Icon icon="mdi:web" height="24" />,
  properties: <Icon icon="ion:options-outline" height="24" />,
  server: <Icon icon="mdi:server-outline" height="24" />,
  notifications: <Icon icon="carbon:notification" height="24" />,
  python: <Icon icon="ion:logo-python" height="24" />,
  features: <Icon icon="ri:function-add-line" height="24" />
}

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
      icon='🗃️'
      title={item.label}
      description={item.description ?? categoryItemsPlural(item.items.length)}
    />
  );
}

function CardLink({ item }) {
  const doc = useDocById(item.docId ?? undefined);

  return (
    <CardLayout
      href={item.href}
      icon={ICON_MAP[item?.customProps?.icon] ?? (isInternalUrl(item.href) ? "📄️" : "🔗")}
      title={item.label}
      description={item.description ?? doc?.description}
    />
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
