import { DependencyTreeData } from '@/views/instances/sbomdependencytrees/dependencyTree';

export type SbomDependency = {
  ref: string;
  dependsOn?: string[];
};

// remove the path before the first forward slash and remove any query parameters after the first question mark from a ref.
export const normalizeNodeName = (name: string): string =>
  name.replace(/^[^\/]*\//, '').replace(/\?.*$/, '');

function expandDependency(
  ref: string,
  dependenciesByRef: Map<string, SbomDependency>,
  ancestors: Set<string>,
): DependencyTreeData {
  const name = normalizeNodeName(ref);
  if (ancestors.has(ref)) return { name, cycle: true };

  // Only ancestors on this path form a cycle; shared dependencies in other
  // branches must still be expanded. Keep full references for identity.
  const path = new Set(ancestors).add(ref);
  const dependsOn = dependenciesByRef.get(ref)?.dependsOn ?? [];
  return {
    name,
    children: dependsOn.length
      ? dependsOn.map((child) =>
          expandDependency(child, dependenciesByRef, path),
        )
      : undefined,
  };
}

export const retrieveChildren = (
  dependsOn: string[],
  sbomDependencies: SbomDependency[],
  ancestors: Set<string> = new Set(),
): DependencyTreeData[] | undefined => {
  if (!dependsOn.length) return undefined;

  const dependenciesByRef = new Map(
    sbomDependencies.map((dependency) => [dependency.ref, dependency]),
  );
  return dependsOn.map((item) =>
    expandDependency(item, dependenciesByRef, ancestors),
  );
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
    children: retrieveChildren(
      root.dependsOn ?? [],
      sbomDependencies,
      new Set([root.ref]),
    ),
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
