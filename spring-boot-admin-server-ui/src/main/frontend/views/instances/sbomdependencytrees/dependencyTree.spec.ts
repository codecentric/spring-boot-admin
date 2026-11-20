import { describe, expect, it } from 'vitest';

import {
  type DependencyTreeData,
  createDependencyTree,
} from './dependencyTree';

describe('dependencyTree', () => {
  it('multiple trees have independent tooltips positioned correctly', async () => {
    const container1 = document.createElement('div');
    const container2 = document.createElement('div');
    container1.style.width = '400px';
    container2.style.width = '400px';
    document.body.appendChild(container1);
    document.body.appendChild(container2);

    const treeData: DependencyTreeData = {
      name: 'g/a@1',
      children: [{ name: 'g/b@1' }],
    };

    const tree1 = await createDependencyTree(container1, treeData, false);
    const tree2 = await createDependencyTree(container2, treeData, false);

    expect(tree1).toBeDefined();
    expect(tree2).toBeDefined();

    // Find second tree's node and dispatch mousemove
    const svg2 = container2.querySelector('svg');
    const nodes2 = svg2?.querySelectorAll('g g');
    expect(nodes2?.length).toBeGreaterThan(0);

    const event = new MouseEvent('mousemove', {
      bubbles: true,
      clientX: 100,
      clientY: 200,
    });

    nodes2?.[0].dispatchEvent(event);

    // Check tooltip positioning
    const tooltip2 = container2.querySelector('.sbom-tooltip') as HTMLElement;
    expect(tooltip2).toBeDefined();
    expect(tooltip2.style.left).toBe('110px');
    expect(tooltip2.style.top).toBe('210px');

    // First tree's tooltip should be unaffected
    const tooltip1 = container1.querySelector('.sbom-tooltip') as HTMLElement;
    expect(tooltip1).toBeDefined();
    expect(tooltip1.style.left).toBe('');
    expect(tooltip1.style.top).toBe('');

    document.body.removeChild(container1);
    document.body.removeChild(container2);
  });

  it('rerender removes old tooltip', async () => {
    const container = document.createElement('div');
    container.style.width = '400px';
    document.body.appendChild(container);

    const treeData: DependencyTreeData = {
      name: 'g/a@1',
      children: [{ name: 'g/b@1' }],
    };

    await createDependencyTree(container, treeData, false);
    await createDependencyTree(container, treeData, false);

    const tooltips = container.querySelectorAll('.sbom-tooltip');
    expect(tooltips).toHaveLength(1);

    document.body.removeChild(container);
  });
});
