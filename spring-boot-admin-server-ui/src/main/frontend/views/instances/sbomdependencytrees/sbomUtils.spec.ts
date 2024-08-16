import { describe, expect, it } from 'vitest';

import {
  SbomDependency,
  filterTree,
  normalizeNodeName,
  retrieveChildren,
} from './sbomUtils';

import { DependencyTreeData } from '@/views/instances/sbomdependencytrees/dependencyTree';

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
