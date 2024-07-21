// @vitest-environment happy-dom
import { RenderResult, screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import TreeGraph from '@/views/instances/sbomdependencytrees/tree.vue';

const setUnknownFilter = async (
  dependencyTree: RenderResult,
  instance: Instance,
): Promise<void> => {
  expect(await screen.findAllByText(/SBOM id 'application'/)).toBeDefined();
  expect(screen.getByTestId('treecontainer-svg')).toBeVisible();

  await dependencyTree.rerender({
    sbomId: 'application',
    instance: instance,
    filter: 'unknowndependencyfilter',
  });

  await waitFor(
    () => {
      expect(screen.getByTestId('treecontainer-svg')).not.toBeVisible();
    },
    { timeout: 1100 },
  );
};

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
    await renderComponent();
  });

  it('renders correctly with given props', async () => {
    const expectedTexts = [
      'spring-boot-admin-sample-servlet',
      'spring-boot-admin-sample-custom-ui',
      'spring-boot-admin-starter-server',
      'spring-cloud-starter-config',
      'org.hsqldb',
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

    await waitFor(
      () => {
        expect(
          screen.queryByText(/spring-session-jdbc/i),
        ).not.toBeInTheDocument();
        expect(screen.queryByText(/spring-webmvc/i)).toBeInTheDocument();
      },
      { timeout: 2000 },
    );
  });

  it("shows empty tree if filter doesn't apply to dependencies in tree", async () => {
    expect(await screen.findAllByText(/SBOM id 'application'/)).toBeDefined();

    await dependencyTree.rerender({
      sbomId: 'application',
      instance: instance,
      filter: 'unknowndependencyfilter',
    });

    await waitFor(
      () => {
        expect(
          screen.queryByText(/SBOM id 'application'/i),
        ).not.toBeInTheDocument();
      },
      { timeout: 1100 },
    );
  });

  it('should hide svg if no dependencies found for filter', async () => {
    await setUnknownFilter(dependencyTree, instance);

    await waitFor(
      () => {
        expect(screen.getByTestId('treecontainer-svg')).not.toBeVisible();
      },
      { timeout: 1100 },
    );
  });

  it('should toggle svg visibility if dependencies found for filter', async () => {
    await setUnknownFilter(dependencyTree, instance);

    await dependencyTree.rerender({
      sbomId: 'application',
      instance: instance,
      filter: 'webmvc',
    });

    await waitFor(
      () => {
        expect(screen.getByTestId('treecontainer-svg')).toBeVisible();
        expect(screen.queryByText(/spring-webmvc/i)).toBeInTheDocument();
      },
      { timeout: 2000 },
    );
  });
});
