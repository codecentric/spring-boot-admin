import * as d3 from 'd3';
import {
  BaseType,
  HierarchyNode,
  HierarchyPointNode,
  Selection,
  TreeLayout,
} from 'd3';

const margin = { top: 20, bottom: 20, left: 10 };
const maxItemsInFrame = 4;

const extractGroupId = (dependency: string): string => {
  return dependency.match(/^(.*?)\//)?.[1] ?? dependency;
};

const extractArtifactId = (dependency: string): string => {
  return dependency.match(/^[^/]+\/([^@]+)@?.*$/)?.[1] ?? dependency;
};

type MyHierarchyNode = Omit<
  HierarchyNode<DependencyTreeData>,
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

const linkNodesHorizontal = (hierarchyLink: MyHierarchyLink) => {
  return d3
    .linkHorizontal<MyHierarchyLink, MyHierarchyPointNode>()
    .x((d) => d.y)
    .y((d) => d.x)(hierarchyLink);
};

export type D3DependencyTree = {
  treeContainer: HTMLElement;
  root: MyHierarchyNode;
  treeLayout: TreeLayout<DependencyTreeData>;
  svg: Selection<SVGGElement, unknown, null, undefined>;
  gNode: Selection<SVGGElement, unknown, null, undefined>;
  gLink: Selection<SVGGElement, unknown, null, undefined>;
};

export type DependencyTreeData = {
  name: string;
  children?: DependencyTreeData[];
};

export const createDependencyTree = (
  treeContainer: HTMLElement,
  treeData: DependencyTreeData,
  initFolding: boolean = true,
): D3DependencyTree => {
  d3.select(treeContainer).select('svg').remove();

  if (!treeData) {
    return;
  }

  const elementsWidth = treeContainer.getBoundingClientRect().width;
  const dx = 48;
  const dy = elementsWidth / maxItemsInFrame;

  const root: MyHierarchyNode = d3.hierarchy(treeData);
  const treeLayout: TreeLayout<DependencyTreeData> = d3
    .tree<DependencyTreeData>()
    .nodeSize([dx, dy]);

  root.sort((a, b) => d3.ascending(a.data.name, b.data.name));

  const svg: Selection<SVGGElement, unknown, null, undefined> = d3
    .select(treeContainer)
    .append('svg')
    .attr('width', elementsWidth)
    .attr('height', dx)
    .attr('viewBox', [-margin.left, -margin.top, elementsWidth, dx])
    .attr('style', 'font-size: .75rem;');

  d3.select(treeContainer)
    .append('div')
    .attr('id', 'tooltip')
    .attr('class', 'bg-sba-100 rounded')
    .attr('style', 'position: absolute; opacity: 0; font-size: 0.85rem;');

  const gLink: Selection<SVGGElement, unknown, null, undefined> = svg
    .append('g')
    .attr('fill', 'none')
    .attr('stroke', '#999')
    .attr('stroke-opacity', 0.4)
    .attr('stroke-width', 2);

  const gNode: Selection<SVGGElement, unknown, null, undefined> = svg
    .append('g')
    .attr('cursor', 'pointer')
    .attr('pointer-events', 'all');

  const d3DependencyTree: D3DependencyTree = {
    treeContainer,
    gNode,
    root,
    svg,
    treeLayout,
    gLink,
  };

  initRootAndDescendants(d3DependencyTree, initFolding);

  updateDependencyTree(d3DependencyTree, root);

  return d3DependencyTree;
};

const initRootAndDescendants = (
  tree: D3DependencyTree,
  initFolding: boolean,
): void => {
  tree.root.x0 = tree.treeContainer.getBoundingClientRect().width / 2;
  tree.root.y0 = 0;
  tree.root.descendants().forEach((d: MyHierarchyNode, i: number) => {
    d.id = '' + i;

    d._children = d.children;

    if (initFolding && d.depth >= 2) {
      d.children = null;
    }
  });
};

const updateDependencyTree = (
  dependencyTree: D3DependencyTree,
  source: MyHierarchyNode,
  removeNodes: boolean = false,
): void => {
  const { root, treeLayout, svg, gNode, gLink } = dependencyTree;
  const nodes: MyHierarchyNode[] = root.descendants().reverse();
  const links: Array<MyHierarchyLink> = root.links();

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

  const height = right.x - left.x + margin.top + margin.bottom;

  const treeContainerWidth =
    dependencyTree.treeContainer.getBoundingClientRect().width;

  const width =
    rightWidth.y -
    leftWidth.y +
    margin.left +
    treeContainerWidth / maxItemsInFrame;

  svg
    .transition()
    .duration(300)
    .attr('height', height)
    .attr('width', width)
    .attr(
      'viewBox',
      `${-margin.left}, ${left.x - margin.top}, ${width}, ${height}`,
    )
    .tween(
      'resize',
      window.ResizeObserver ? null : () => () => svg.dispatch('toggle'),
    );

  if (removeNodes) {
    gNode.selectAll<SVGGElement, typeof root>('g').remove();
  }

  const subGNodeSelection: Selection<
    SVGGElement,
    MyHierarchyNode,
    SVGGElement,
    unknown
  > = gNode.selectAll<SVGGElement, typeof root>('g').data(nodes, (d) => d.id);

  const nodeEnter: Selection<
    SVGGElement,
    MyHierarchyNode,
    SVGGElement,
    MyHierarchyNode
  > = subGNodeSelection
    .enter()
    .append('g')
    .attr('transform', () => `translate(${source.y0},${source.x0})`)
    .attr('fill-opacity', 0)
    .on('click', (event, d: MyHierarchyNode) => {
      d.children = d.children ? null : d._children;
      updateDependencyTree(dependencyTree, d, false);
    });

  nodeEnter
    .append('rect')
    .attr('width', treeContainerWidth / (maxItemsInFrame + 1))
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
    .attr('dy', (d) => (d.parent ? '-.25rem' : '.35rem'))
    .attr('x', 12)
    .attr('text-anchor', 'start')
    .text((d) => extractGroupId(d.data.name));

  nodeEnter
    .append('text')
    .attr('dy', '.75rem')
    .attr('x', 12)
    .attr('text-anchor', 'start')
    .text((d) => (d.parent ? extractArtifactId(d.data.name) : null));

  nodeEnter
    .on('mouseover', (event, d: HierarchyNode<DependencyTreeData>) => {
      d3.select('#tooltip')
        .transition()
        .duration(300)
        .style('opacity', 1)
        .text(d.data.name)
        .transition()
        .duration(5000)
        .style('opacity', 0);
    })
    .on('mouseout', () => {
      d3.select('#tooltip').style('opacity', 0);
    })
    .on('mousemove', (event) => {
      d3.select('#tooltip')
        .style('left', `${event.layerX + 10}px`)
        .style('top', `${event.layerY + 10}px`);
    });

  subGNodeSelection
    .merge(nodeEnter)
    .transition()
    .duration(300)
    .attr('transform', (d) => `translate(${d.y},${d.x})`)
    .attr('fill-opacity', 1)
    .attr('stroke-opacity', 1);

  subGNodeSelection
    .exit()
    .transition()
    .duration(300)
    .remove()
    .attr('transform', () => `translate(${source.y},${source.x})`)
    .attr('fill-opacity', 0)
    .attr('stroke-opacity', 0);

  const link: Selection<BaseType, MyHierarchyLink, SVGGElement, unknown> = gLink
    .selectAll('path')
    .data(links, (d: MyHierarchyLink) => d.target.id);

  const linkEnter: Selection<
    SVGGElement,
    MyHierarchyLink,
    SVGGElement,
    unknown
  > = link.enter().append('path').attr('d', linkNodesHorizontal);

  link
    .merge(linkEnter)
    .transition()
    .duration(300)
    .attr('d', linkNodesHorizontal);

  link
    .exit()
    .transition()
    .duration(300)
    .remove()
    .attr('d', linkNodesHorizontal);

  root.eachBefore((d: MyHierarchyNode) => {
    d.x0 = d.x;
    d.y0 = d.y;
  });
};

export const rerenderDependencyTree = (
  dependencyTree: D3DependencyTree,
  data: DependencyTreeData,
): void => {
  const root: MyHierarchyNode = d3.hierarchy(data);

  dependencyTree.root = root;
  initRootAndDescendants(dependencyTree, false);

  updateDependencyTree(dependencyTree, root, true);
};
