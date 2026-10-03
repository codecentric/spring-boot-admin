import { DependencyTreeData } from '@/views/instances/sbomdependencytrees/dependencyTree';

export type SbomDependency = {
  ref: string;
  dependsOn?: string[];
};

// remove the path before the first forward slash and remove any query parameters after the first question mark from a ref.
export const normalizeNodeName = (name: string): string =>
  name.replace(/^[^\/]*\//, '').replace(/\?.*$/, '');

function getChildren(sbomDependencies: SbomDependency[], item: string) {
  return retrieveChildren(
    sbomDependencies
      .filter((node) => node.ref === item && node.dependsOn?.length)
      .flatMap((node) => node.dependsOn),
    sbomDependencies,
  );
}

export const retrieveChildren = (
  dependsOn: string[],
  sbomDependencies: SbomDependency[],
): DependencyTreeData[] | undefined => {
  if (!dependsOn.length) return undefined;

  return dependsOn.map((item) => ({
    name: normalizeNodeName(item),
    children: getChildren(sbomDependencies, item),
  }));
};

export const normalizeData = (
  sbomDependencies: SbomDependency[],
  rootRef: string | undefined,
): DependencyTreeData | null => {
  if (!rootRef) return null;

  const root = sbomDependencies.find((node) => node.ref === rootRef);
  if (!root) return null;

  return {
    name: normalizeNodeName(root.ref),
    children: retrieveChildren(root.dependsOn ?? [], sbomDependencies),
  };
};

export const filterTree = (
  treeData: DependencyTreeData | null,
  filter: string,
): DependencyTreeData | null => {
  if (!treeData) return null;

  if (!filter || filter.trim() === '') {
    return treeData;
  }

  const filterLowerCase = filter.trim().toLowerCase();
  const matchesCurrentNode = treeData.name
    .toLowerCase()
    .includes(filterLowerCase);

  const filteredChildren = treeData.children
    ?.map((child) => filterTree(child, filter))
    .filter((child) => child !== null) as DependencyTreeData[];

  if (matchesCurrentNode || (filteredChildren && filteredChildren.length > 0)) {
    return {
      ...treeData,
      children: filteredChildren,
    };
  }

  return null;
};
