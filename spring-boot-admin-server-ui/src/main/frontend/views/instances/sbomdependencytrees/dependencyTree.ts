/**
 * Copyright 2014-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import * as d3 from 'd3';
import { HierarchyNode, HierarchyPointNode, Selection, TreeLayout } from 'd3';

const MARGIN = { top: 20, bottom: 20, left: 10 };
const MAX_ITEMS_IN_FRAME = 4;

const extractGroupId = (dependency: string): string =>
  dependency?.match(/^(.*?)\//)?.[1] ?? dependency;

const extractArtifactId = (dependency: string): string =>
  dependency?.match(/^[^/]+\/([^@]+)@?.*$/)?.[1] ?? dependency;

export type DependencyTreeData = {
  name: string;
  children?: DependencyTreeData[];
};

type HierarchyNodeData = HierarchyNode<DependencyTreeData>;

type MyHierarchyNode = Omit<
  HierarchyNodeData,
  'id' | 'descendants' | 'children' | 'links' | 'ancestors' | 'leaves' | 'data'
> & {
  new (data: DependencyTreeData): MyHierarchyNode;
  id?: string;
  children?: MyHierarchyNode[];
  _children?: MyHierarchyNode[];
  data: DependencyTreeData;
  x0?: number;
  y0?: number;
  links(): Array<MyHierarchyLink>;
  ancestors(): MyHierarchyNode[];
  descendants(): MyHierarchyNode[];
  leaves(): MyHierarchyNode[];
};

type MyHierarchyPointNode = HierarchyPointNode<DependencyTreeData> & {
  links(): Array<MyHierarchyPointLink>;
};

type MyHierarchyLink = {
  source: MyHierarchyNode;
  target: MyHierarchyNode;
};

type MyHierarchyPointLink = {
  source: MyHierarchyPointNode;
  target: MyHierarchyPointNode;
};

export type D3DependencyTree = {
  treeContainer: HTMLElement;
  root: MyHierarchyNode;
  treeLayout: TreeLayout<DependencyTreeData>;
  svg: Selection<SVGGElement, unknown, null, undefined>;
  gNode: Selection<SVGGElement, unknown, null, undefined>;
  gLink: Selection<SVGGElement, unknown, null, undefined>;
};

// Utility Functions
const linkNodesHorizontal = (hierarchyLink: MyHierarchyLink) =>
  d3
    .linkHorizontal<MyHierarchyLink, MyHierarchyPointNode>()
    .x((d) => d.y)
    .y((d) => d.x)(hierarchyLink);

const createGlobalLinkAndNode = (
  svg: Selection<SVGGElement, unknown, null, undefined>,
) => {
  const gLink = svg
    .append('g')
    .attr('fill', 'none')
    .attr('stroke', '#999')
    .attr('stroke-opacity', 0.4)
    .attr('stroke-width', 2);

  const gNode = svg
    .append('g')
    .attr('cursor', 'pointer')
    .attr('pointer-events', 'all');

  return { gLink, gNode };
};

const initRootAndDescendants = (
  tree: D3DependencyTree,
  initFolding: boolean,
): void => {
  tree.root.x0 = tree.treeContainer.getBoundingClientRect().width / 2;
  tree.root.y0 = 0;
  tree.root.descendants().forEach((d, i) => {
    d.id = '' + i;
    d._children = d.children;

    if (initFolding && d.depth >= 1) {
      d.children = null;
    }
  });
};

const updateDependencyTree = async (
  dependencyTree: D3DependencyTree,
  source: MyHierarchyNode,
  removeNodes = false,
): Promise<void> => {
  const { root, treeLayout, svg, gNode, gLink } = dependencyTree;
  const nodes = root.descendants().reverse();
  const links = root.links();

  treeLayout(root);

  const [left, right, leftWidth, rightWidth] = nodes.reduce(
    ([left, right, leftWidth, rightWidth], node) => [
      node.x < left.x ? node : left,
      node.x > right.x ? node : right,
      node.y < leftWidth.y ? node : leftWidth,
      node.y > rightWidth.y ? node : rightWidth,
    ],
    [root, root, root, root],
  );

  const height = right.x - left.x + MARGIN.top + MARGIN.bottom;
  const treeContainerWidth =
    dependencyTree.treeContainer.getBoundingClientRect().width;
  const width =
    rightWidth.y -
    leftWidth.y +
    MARGIN.left +
    treeContainerWidth / MAX_ITEMS_IN_FRAME;

  svg
    .transition()
    .attr('height', height)
    .attr('width', width)
    .attr(
      'viewBox',
      `${-MARGIN.left}, ${left.x - MARGIN.top}, ${width}, ${height}`,
    )
    .tween(
      'resize',
      window.ResizeObserver ? null : () => () => svg.dispatch('toggle'),
    );

  if (removeNodes) {
    if (!source.children) {
      svg.attr('style', 'visibility: hidden');
    } else {
      svg.attr('style', 'font-size: .75rem;');
    }

    gNode.selectAll<SVGGElement, any>('g').remove();
  }

  const subGNodeSelection = gNode
    .selectAll<SVGGElement, typeof root>('g')
    .data(nodes, (d) => d.id);

  const nodeEnter = subGNodeSelection
    .enter()
    .append('g')
    .attr('transform', () => `translate(${source.y0},${source.x0})`)
    .attr('fill-opacity', 0)
    .on('click', (event, d) => {
      d.children = d.children ? null : d._children;
      updateDependencyTree(dependencyTree, d, false);
    });

  nodeEnter
    .append('rect')
    .attr('width', treeContainerWidth / (MAX_ITEMS_IN_FRAME + 1))
    .attr('height', '2.5rem')
    .attr('y', '-1.25rem')
    .attr('rx', 6)
    .attr('ry', 6)
    .attr('stroke-width', 1)
    .attr('fill-opacity', 0.8)
    .style('fill', (d) => (d._children ? '#91E8E0' : '#d0f7df'));

  nodeEnter
    .append('circle')
    .attr('r', 3.5)
    .attr('fill', (d) => (d._children ? '#48c78e' : '#999999'));

  nodeEnter
    .append('text')
    .attr('dy', () => '-.25rem')
    .attr('x', 12)
    .attr('text-anchor', 'start')
    .text((d) =>
      d && d.data && d.data.name ? extractGroupId(d.data.name) : '',
    );

  nodeEnter
    .append('text')
    .attr('dy', '.75rem')
    .attr('x', 12)
    .attr('text-anchor', 'start')
    .text((d) => extractArtifactId(d.data.name));

  nodeEnter
    .on('mouseover', (event, d) => {
      d3.select('#tooltip').transition().style('opacity', 1).text(d.data.name);
    })
    .on('mouseout', () => {
      d3.select('#tooltip').transition().style('opacity', 0);
    })
    .on('mousemove', (event) => {
      d3.select('#tooltip')
        .style('left', `${event.layerX + 10}px`)
        .style('top', `${event.layerY + 10}px`);
    });

  subGNodeSelection
    .merge(nodeEnter)
    .transition()
    .attr('transform', (d) => `translate(${d.y},${d.x})`)
    .attr('fill-opacity', 1)
    .attr('stroke-opacity', 1);

  subGNodeSelection
    .exit()
    .transition()
    .remove()
    .attr('transform', () => `translate(${source.y},${source.x})`)
    .attr('fill-opacity', 0)
    .attr('stroke-opacity', 0);

  const link = gLink
    .selectAll('path')
    .data(links, (d: MyHierarchyLink) => d.target.id);

  const linkEnter = link.enter().append('path').attr('d', linkNodesHorizontal);

  link.merge(linkEnter).transition().attr('d', linkNodesHorizontal);

  link.exit().transition().remove().attr('d', linkNodesHorizontal);

  root.eachBefore((d: MyHierarchyNode) => {
    d.x0 = d.x;
    d.y0 = d.y;
  });
};

export const createDependencyTree = async (
  treeContainer: HTMLElement,
  treeData: DependencyTreeData,
  initFolding = true,
): Promise<D3DependencyTree> => {
  d3.select(treeContainer).select('svg').remove();

  if (!treeData) return;

  const elementsWidth = treeContainer.getBoundingClientRect().width;
  const dx = 48;
  const dy = elementsWidth / MAX_ITEMS_IN_FRAME;

  const root = d3.hierarchy(treeData) as MyHierarchyNode;
  const treeLayout = d3.tree<DependencyTreeData>().nodeSize([dx, dy]);

  root.sort((a, b) => d3.ascending(a.data.name, b.data.name));

  const svg = d3
    .select(treeContainer)
    .append('svg')
    .attr('data-testid', 'treecontainer-svg')
    .attr('width', elementsWidth)
    .attr('height', dx)
    .attr('viewBox', [-MARGIN.left, -MARGIN.top, elementsWidth, dx])
    .attr('style', 'font-size: .75rem;');

  d3.select(treeContainer)
    .append('div')
    .attr('id', 'tooltip')
    .attr('class', 'bg-sba-100 rounded')
    .attr('style', 'position: absolute; opacity: 0; font-size: 0.85rem;');

  const { gLink, gNode } = createGlobalLinkAndNode(svg);

  const d3DependencyTree: D3DependencyTree = {
    treeContainer,
    gNode,
    root,
    svg,
    treeLayout,
    gLink,
  };

  initRootAndDescendants(d3DependencyTree, initFolding);
  await updateDependencyTree(d3DependencyTree, root);

  return d3DependencyTree;
};

export const rerenderDependencyTree = async (
  dependencyTree: D3DependencyTree,
  data: DependencyTreeData,
): Promise<void> => {
  dependencyTree.root = d3.hierarchy(
    data?.children ? data : {},
  ) as MyHierarchyNode;

  initRootAndDescendants(dependencyTree, false);
  await updateDependencyTree(dependencyTree, dependencyTree.root, true);
};
