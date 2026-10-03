import { RenderResult, screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import TreeGraph from '@/views/instances/sbomdependencytrees/tree.vue';

const setUnknownFilter = async (
  dependencyTree: RenderResult,
  instance: Instance,
): Promise<void> => {
  expect(
    await screen.findAllByText(/spring-boot-admin-sample-servlet/),
  ).toBeDefined();
  expect(screen.getByTestId('treecontainer-svg')).toBeVisible();

  await dependencyTree.rerender({
    sbomId: 'application',
    instance: instance,
    filter: 'unknowndependencyfilter',
  });

  vi.advanceTimersByTime(2000);

  await waitFor(() => {
    expect(screen.getByTestId('treecontainer-svg')).not.toBeVisible();
  });
};

describe('TreeGraph root selection', () => {
  beforeEach(() => vi.useFakeTimers({ ignoreMissingTimers: true }));
  afterEach(() => vi.useRealTimers());

  const application = new Application(applications[0]);
  const instance: Instance = application.instances[0];
  const dependencies = [
    { ref: 'library-a', dependsOn: ['library-c'] },
    { ref: 'root-app', dependsOn: ['library-a', 'library-b'] },
    { ref: 'library-b', dependsOn: [] },
    { ref: 'library-c', dependsOn: [] },
  ];

  it('uses the metadata root from the fetched SBOM', async () => {
    server.use(
      http.get('/instances/:instanceId/actuator/sbom/application', () =>
        HttpResponse.json({
          metadata: { component: { 'bom-ref': 'root-app' } },
          dependencies,
        }),
      ),
    );
    render(TreeGraph, { props: { instance, sbomId: 'application' } });

    expect(await screen.findAllByText('root-app')).not.toHaveLength(0);
    expect(screen.getAllByText('library-a')).not.toHaveLength(0);
    expect(screen.getAllByText('library-b')).not.toHaveLength(0);
  });

  it('renders the resolved root after clearing an initially unmatched filter', async () => {
    server.use(
      http.get('/instances/:instanceId/actuator/sbom/application', () =>
        HttpResponse.json({
          metadata: { component: { 'bom-ref': 'root-app' } },
          dependencies,
        }),
      ),
    );
    const component = render(TreeGraph, {
      props: { instance, sbomId: 'application', filter: 'no-match' },
    });
    await waitFor(() =>
      expect(
        screen.queryByTestId('instance-section-loading-spinner'),
      ).not.toBeInTheDocument(),
    );
    expect(screen.queryByTestId('treecontainer-svg')).not.toBeInTheDocument();
    expect(
      screen.queryByText('instances.dependencies.no_data_provided'),
    ).not.toBeInTheDocument();

    await component.rerender({ filter: '' });
    vi.advanceTimersByTime(2000);

    expect(await screen.findAllByText('root-app')).not.toHaveLength(0);
    expect(screen.getAllByText('library-b')).not.toHaveLength(0);
  });

  it.each([
    { dependencies },
    { metadata: { component: {} }, dependencies },
    { metadata: { component: { 'bom-ref': 'missing' } }, dependencies },
    { metadata: { component: { 'bom-ref': 'root-app' } } },
    { metadata: { component: { 'bom-ref': 'root-app' } }, dependencies: [] },
  ])(
    'shows an empty state when the root cannot be resolved: %j',
    async (sbom) => {
      server.use(
        http.get('/instances/:instanceId/actuator/sbom/application', () =>
          HttpResponse.json(sbom),
        ),
      );
      render(TreeGraph, { props: { instance, sbomId: 'application' } });

      expect(
        await screen.findByText('instances.dependencies.no_data_provided'),
      ).toBeInTheDocument();
      expect(screen.queryByTestId('treecontainer-svg')).not.toBeInTheDocument();
      expect(screen.queryByText('library-a')).not.toBeInTheDocument();
    },
  );
});

describe('TreeGraph', () => {
  const application = new Application(applications[0]);
  const instance: Instance = application.instances[0];

  let dependencyTree: RenderResult;

  const renderComponent = async (filter = '') => {
    dependencyTree = render(TreeGraph, {
      props: {
        sbomId: 'application',
        instance: instance,
        filter,
      },
    });
    await waitFor(() =>
      expect(screen.getByTestId('treecontainer-svg')).toBeInTheDocument(),
    );
  };

  beforeEach(async () => {
    vi.useFakeTimers({ ignoreMissingTimers: true });
    await renderComponent();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders correctly with given props', async () => {
    const expectedTexts = [
      'spring-boot-admin-sample-servlet',
      'spring-boot-admin-sample-custom-ui',
      'spring-boot-admin-starter-server',
      'org.hsqldb',
      'spring-boot-starter-mail',
      'spring-boot-starter-security',
      'spring-boot-starter-webmvc',
      'spring-cloud-starter-config',
      'spring-session-core',
      'spring-session-jdbc',
    ];

    for (const text of expectedTexts) {
      expect(
        await screen.findByText(new RegExp(text, 'i')),
      ).toBeInTheDocument();
    }

    expect(screen.getByTestId('treecontainer-svg')).toBeVisible();
  });

  it('filters the tree by filter prop', async () => {
    expect(await screen.findAllByText(/spring-session-jdbc/)).toBeDefined();
    await dependencyTree.rerender({
      sbomId: 'application',
      instance: instance,
      filter: 'webmvc',
    });

    vi.advanceTimersByTime(2000);

    await waitFor(() => {
      expect(
        screen.queryByText(/spring-session-jdbc/i),
      ).not.toBeInTheDocument();
      expect(screen.queryByText(/spring-webmvc/i)).toBeInTheDocument();
    });
  });

  it("shows empty tree if filter doesn't apply to dependencies in tree", async () => {
    expect(
      await screen.findAllByText(/spring-boot-admin-sample-servlet/),
    ).toBeDefined();

    await dependencyTree.rerender({
      sbomId: 'application',
      instance: instance,
      filter: 'unknowndependencyfilter',
    });

    vi.advanceTimersByTime(2000);

    await waitFor(() => {
      expect(
        screen.queryByText(/spring-boot-admin-sample-servlet/i),
      ).not.toBeInTheDocument();
    });
  });

  it('should hide svg if no dependencies found for filter', async () => {
    await setUnknownFilter(dependencyTree, instance);

    await waitFor(() => {
      expect(screen.getByTestId('treecontainer-svg')).not.toBeVisible();
    });
  });

  it('should toggle svg visibility if dependencies found for filter', async () => {
    await setUnknownFilter(dependencyTree, instance);

    await dependencyTree.rerender({
      sbomId: 'application',
      instance: instance,
      filter: 'webmvc',
    });

    vi.advanceTimersByTime(2000);

    await waitFor(() => {
      expect(screen.getByTestId('treecontainer-svg')).toBeVisible();
      expect(screen.queryByText(/spring-webmvc/i)).toBeInTheDocument();
    });
  });
});
