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
