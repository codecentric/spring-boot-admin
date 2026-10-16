import { describe, expect, it } from 'vitest';

import {
  SbomDependency,
  filterTree,
  normalizeData,
  normalizeNodeName,
  retrieveChildren,
} from './sbomUtils';

import { DependencyTreeData } from '@/views/instances/sbomdependencytrees/dependencyTree';

describe('normalizeData', () => {
  const root = { ref: 'app', dependsOn: ['a', 'b'] };
  const libraries = [
    { ref: 'a', dependsOn: ['c'] },
    { ref: 'b', dependsOn: [] },
    { ref: 'c', dependsOn: [] },
  ];

  it.each([0, 1, 3])('uses the metadata root at index %s', (index) => {
    const dependencies = [...libraries];
    dependencies.splice(index, 0, root);
    const original = structuredClone(dependencies);

    expect(normalizeData(dependencies, 'app')).toEqual({
      name: 'app',
      children: [
        { name: 'a', children: [{ name: 'c', children: undefined }] },
        { name: 'b', children: undefined },
      ],
    });
    expect(dependencies).toEqual(original);
  });

  it('matches the full reference before normalizing its label', () => {
    const jar = 'pkg:maven/example/app@1.0?type=jar';
    const pom = 'pkg:maven/example/app@1.0?type=pom';
    expect(
      normalizeData(
        [
          { ref: pom, dependsOn: ['wrong'] },
          { ref: jar, dependsOn: ['a'] },
        ],
        jar,
      ),
    ).toEqual({
      name: 'example/app@1.0',
      children: [{ name: 'a', children: undefined }],
    });
  });

  it.each([undefined, '', 'missing'])('does not guess a root for %s', (ref) => {
    expect(normalizeData([root, ...libraries], ref)).toBeNull();
  });

  it('handles an empty dependency graph', () => {
    expect(normalizeData([], 'app')).toBeNull();
  });

  it.each([[], undefined])('handles root dependencies %s', (dependsOn) => {
    expect(normalizeData([{ ref: 'app', dependsOn }], 'app')).toEqual({
      name: 'app',
      children: undefined,
    });
  });

  it('handles a child whose dependency list is absent', () => {
    expect(
      normalizeData([{ ref: 'app', dependsOn: ['a'] }, { ref: 'a' }], 'app'),
    ).toEqual({
      name: 'app',
      children: [{ name: 'a', children: undefined }],
    });
  });
});

describe('cyclic dependencies', () => {
  const graph = [
    { ref: 'app', dependsOn: ['a'] },
    { ref: 'a', dependsOn: ['b'] },
    { ref: 'b', dependsOn: ['a'] },
  ];

  it.each([0, 1, 2])(
    'detects a cycle back to the metadata root at index %s',
    (index) => {
      const dependencies = [
        { ref: 'a', dependsOn: ['b'] },
        { ref: 'b', dependsOn: ['app'] },
      ];
      dependencies.splice(index, 0, { ref: 'app', dependsOn: ['a'] });
      expect(normalizeData(dependencies, 'app')).toEqual({
        name: 'app',
        children: [
          {
            name: 'a',
            children: [{ name: 'b', children: [{ name: 'app', cycle: true }] }],
          },
        ],
      });
    },
  );

  it('terminates a reachable cycle without modifying the graph', () => {
    const original = structuredClone(graph);
    const tree = normalizeData(graph, 'app');
    expect(tree).toEqual({
      name: 'app',
      children: [
        {
          name: 'a',
          children: [{ name: 'b', children: [{ name: 'a', cycle: true }] }],
        },
      ],
    });
    expect(() => JSON.stringify(tree)).not.toThrow();
    expect(graph).toEqual(original);
  });

  it('terminates a self-reference at the root', () => {
    expect(normalizeData([{ ref: 'app', dependsOn: ['app'] }], 'app')).toEqual({
      name: 'app',
      children: [{ name: 'app', cycle: true }],
    });
  });

  it('includes the root in the ancestor path', () => {
    expect(
      normalizeData(
        [
          { ref: 'app', dependsOn: ['a'] },
          { ref: 'a', dependsOn: ['app'] },
        ],
        'app',
      ),
    ).toEqual({
      name: 'app',
      children: [{ name: 'a', children: [{ name: 'app', cycle: true }] }],
    });
  });

  it('retains shared dependencies under independent parents', () => {
    expect(
      normalizeData(
        [
          { ref: 'app', dependsOn: ['a', 'b'] },
          { ref: 'a', dependsOn: ['c'] },
          { ref: 'b', dependsOn: ['c'] },
          { ref: 'c', dependsOn: ['d'] },
          { ref: 'd', dependsOn: [] },
        ],
        'app',
      ),
    ).toEqual({
      name: 'app',
      children: ['a', 'b'].map((name) => ({
        name,
        children: [
          { name: 'c', children: [{ name: 'd', children: undefined }] },
        ],
      })),
    });
  });

  it('compares full references rather than normalized labels', () => {
    const jar = 'pkg:maven/example/lib@1?type=jar';
    const pom = 'pkg:maven/example/lib@1?type=pom';
    expect(
      normalizeData(
        [
          { ref: jar, dependsOn: [pom] },
          { ref: pom, dependsOn: [jar] },
        ],
        jar,
      ),
    ).toEqual({
      name: 'example/lib@1',
      children: [
        {
          name: 'example/lib@1',
          children: [{ name: 'example/lib@1', cycle: true }],
        },
      ],
    });
  });

  it('also protects direct calls to retrieveChildren', () => {
    expect(retrieveChildren(['a'], [{ ref: 'a', dependsOn: ['a'] }])).toEqual([
      { name: 'a', children: [{ name: 'a', cycle: true }] },
    ]);
  });

  it('keeps noncyclic siblings and tolerates missing dependency records', () => {
    expect(
      normalizeData(
        [
          { ref: 'app', dependsOn: ['a', 'missing', 'leaf'] },
          { ref: 'a', dependsOn: ['a'] },
          { ref: 'leaf' },
        ],
        'app',
      ),
    ).toEqual({
      name: 'app',
      children: [
        { name: 'a', children: [{ name: 'a', cycle: true }] },
        { name: 'missing', children: undefined },
        { name: 'leaf', children: undefined },
      ],
    });
  });

  it('filters a finite tree while preserving cycle markers', () => {
    const tree = normalizeData(graph, 'app');
    expect(filterTree(tree, 'a')?.children[0].children[0].children[0]).toEqual({
      name: 'a',
      cycle: true,
      children: undefined,
    });
    expect(filterTree(tree, 'no-match')).toBeNull();
    expect(filterTree(tree, '')).toBe(tree);
  });
});

describe('normalizeNodeName', () => {
  it('should remove the path before the first forward slash', () => {
    const input = 'path/to/resource';
    const expectedOutput = 'to/resource';
    expect(normalizeNodeName(input)).toBe(expectedOutput);
  });

  it('should remove query parameters after the first question mark', () => {
    const input = 'resource?query=param';
    const expectedOutput = 'resource';
    expect(normalizeNodeName(input)).toBe(expectedOutput);
  });

  it('should remove both the path and query parameters', () => {
    const input =
      'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar';
    const expectedOutput =
      'org.springframework.boot/spring-boot-starter@3.3.0-RC1';
    expect(normalizeNodeName(input)).toBe(expectedOutput);
  });

  it('should handle strings without a forward slash', () => {
    const input = 'resource';
    const expectedOutput = 'resource';
    expect(normalizeNodeName(input)).toBe(expectedOutput);
  });

  it('should handle strings without a question mark', () => {
    const input = 'path/to/resource';
    const expectedOutput = 'to/resource';
    expect(normalizeNodeName(input)).toBe(expectedOutput);
  });

  describe('retrieveChildren', () => {
    const sbomDependencies: SbomDependency[] = [
      { ref: 'A', dependsOn: ['B', 'C'] },
      { ref: 'B', dependsOn: ['D'] },
      { ref: 'C', dependsOn: [] },
      { ref: 'D', dependsOn: [] },
    ];

    it('should return undefined if dependsOn is empty', () => {
      const result = retrieveChildren([], sbomDependencies);
      expect(result).toBeUndefined();
    });

    it('should return children data correctly', () => {
      const result = retrieveChildren(['B', 'C'], sbomDependencies);
      expect(result).toEqual([
        { name: 'B', children: [{ name: 'D', children: undefined }] },
        { name: 'C', children: undefined },
      ]);
    });

    it('should handle nested dependencies correctly', () => {
      const result = retrieveChildren(['A'], sbomDependencies);
      expect(result).toEqual([
        {
          name: 'A',
          children: [
            { name: 'B', children: [{ name: 'D', children: undefined }] },
            { name: 'C', children: undefined },
          ],
        },
      ]);
    });

    it('should return node with undefined children if the reference is not found', () => {
      const result = retrieveChildren(['E'], sbomDependencies);
      expect(result).toEqual([
        {
          name: 'E',
          children: undefined,
        },
      ]);
    });
  });

  describe('filterTree', () => {
    const treeData: DependencyTreeData = {
      name: 'A',
      children: [
        { name: 'B', children: [{ name: 'D', children: undefined }] },
        { name: 'C', children: undefined },
      ],
    };

    it('should return the same tree if filter is empty', () => {
      const result = filterTree(treeData, '');
      expect(result).toEqual(treeData);
    });

    it('should filter the tree by node name', () => {
      const result = filterTree(treeData, 'B');
      expect(result).toEqual({
        children: [
          {
            children: [],
            name: 'B',
          },
        ],
        name: 'A',
      });
    });

    it('should return null if no nodes match the filter', () => {
      const result = filterTree(treeData, 'E');
      expect(result).toBeNull();
    });
  });
});
