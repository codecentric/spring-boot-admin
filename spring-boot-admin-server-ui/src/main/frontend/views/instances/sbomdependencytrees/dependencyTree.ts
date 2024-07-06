import * as d3 from 'd3';

const extractGroupId = (dependency: string): string => {
  const match = dependency.match(/^(.*?)\//);
  return match?.[1] ?? dependency;
};

const extractArtifactId = (dependency: string): string => {
  const match = dependency.match(/^[^/]+\/([^@]+)@?.*$/);
  return match?.[1] ?? dependency;
};

export type DependencyTreeData = {
  name: string;
  children?: DependencyTreeData[];
};

export const createDependencyTree = (
  treeContainer: HTMLElement,
  treeData: DependencyTreeData,
  elementsWidth: number,
): void => {
  // Clear any previous SVG
  d3.select(treeContainer).select('svg').remove();

  const margin = { top: 20, bottom: 20, left: 10 };
  const maxItemsInFrame = 5;
  const width = elementsWidth * 4;
  const dx = 48;
  const dy = width / 4 / maxItemsInFrame;

  const root = d3.hierarchy(treeData);
  const treeLayout = d3.tree<DependencyTreeData>().nodeSize([dx, dy]);

  root.sort((a, b) => d3.ascending(a.data.name, b.data.name));

  const diagonal = d3
    .linkHorizontal()
    .x((d) => d.y)
    .y((d) => d.x);

  const svg = d3
    .select(treeContainer)
    .append('svg')
    .attr('width', width)
    .attr('height', dx)
    .attr('viewBox', [-margin.left, -margin.top, width, dx])
    .attr(
      'style',
      'white-space: pre-wrap; height: auto; font: 14px sans-serif; user-select: none;',
    );

  d3.select(treeContainer)
    .append('div')
    .attr('id', 'tooltip')
    .attr('class', 'bg-sba-100 rounded')
    .attr('style', 'position: absolute; opacity: 0;');

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

  const update = (source) => {
    const nodes = root.descendants().reverse();
    const links = root.links();

    treeLayout(root);

    const [left, right] = nodes.reduce(
      ([left, right], node) => [
        node.x < left.x ? node : left,
        node.x > right.x ? node : right,
      ],
      [root, root],
    );

    const height = right.x - left.x + margin.top + margin.bottom;

    svg
      .transition()
      .duration(300)
      .attr('height', height)
      .attr('viewBox', [-margin.left, left.x - margin.top, width, height])
      .tween(
        'resize',
        window.ResizeObserver ? null : () => () => svg.dispatch('toggle'),
      );

    const node = gNode
      .selectAll<SVGGElement, typeof root>('g')
      .data(nodes, (d) => d.id);

    const nodeEnter = node
      .enter()
      .append('g')
      .attr('transform', () => `translate(${source.y0},${source.x0})`)
      .attr('fill-opacity', 0)
      .on('click', (event, d) => {
        d.children = d.children ? null : d._children;
        update(d);
      });

    nodeEnter
      .append('rect')
      .attr('width', elementsWidth / (maxItemsInFrame + 1))
      .attr('height', 40)
      .attr('y', '-1.5em')
      .attr('stroke-width', 1)
      .attr('fill-opacity', 0.8)
      .style('fill', (d) => (d._children ? '#91E8E0' : '#d0f7df'));

    nodeEnter
      .append('circle')
      .attr('r', 3.5)
      .attr('fill', (d) => (d._children ? '#48c78e' : '#999999'));

    nodeEnter
      .append('text')
      .attr('dy', (d) => (d.parent ? '-.25em' : '.35em'))
      .attr('x', 12)
      .attr('text-anchor', 'start')
      .text((d) => extractGroupId(d.data.name))
      .attr('paint-order', 'stroke');

    nodeEnter
      .append('text')
      .attr('dy', '.75em')
      .attr('x', 12)
      .attr('text-anchor', 'start')
      .text((d) => (d.parent ? extractArtifactId(d.data.name) : null));

    nodeEnter
      .on('mouseover', (event, d) => {
        d3.select('#tooltip')
          .transition()
          .duration(300)
          .style('opacity', 1)
          .text(d.data.name);
      })
      .on('mouseout', () => {
        d3.select('#tooltip').style('opacity', 0);
      })
      .on('mousemove', (event) => {
        d3.select('#tooltip')
          .style('left', `${event.layerX + 10}px`)
          .style('top', `${event.layerY + 10}px`);
      });

    node
      .merge(nodeEnter)
      .transition()
      .duration(300)
      .attr('transform', (d) => `translate(${d.y},${d.x})`)
      .attr('fill-opacity', 1)
      .attr('stroke-opacity', 1);

    node
      .exit()
      .transition()
      .duration(300)
      .remove()
      .attr('transform', () => `translate(${source.y},${source.x})`)
      .attr('fill-opacity', 0)
      .attr('stroke-opacity', 0);

    const link = gLink.selectAll('path').data(links, (d) => d.target.id);

    const linkEnter = link
      .enter()
      .append('path')
      .attr('d', () =>
        diagonal({
          source: { x: source.x0, y: source.y0 },
          target: { x: source.x0, y: source.y0 },
        }),
      );

    link.merge(linkEnter).transition().duration(300).attr('d', diagonal);

    link
      .exit()
      .transition()
      .duration(300)
      .remove()
      .attr('d', () =>
        diagonal({
          source: { x: source.x, y: source.y },
          target: { x: source.x, y: source.y },
        }),
      );

    root.eachBefore((d) => {
      d.x0 = d.x;
      d.y0 = d.y;
    });
  };

  root.x0 = dy / 2;
  root.y0 = 0;
  root.descendants().forEach((d, i) => {
    d.id = i;
    d._children = d.children;
    if (d.depth >= 2) d.children = null;
  });

  update(root);
};
