// @vitest-environment happy-dom
import { RenderResult, screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import TreeGraph from '@/views/instances/sbomdependencytrees/tree.vue';

describe(
  'TreeGraph',
  () => {
    const application = new Application(applications[0]);
    const instance: Instance = application.instances[0];

    let dependencyTree: RenderResult;

    beforeEach(async () => {
      dependencyTree = render(TreeGraph, {
        props: {
          sbomId: 'application',
          instance: instance,
        },
      });
    });

    it('renders correctly with given props', async () => {
      expect(
        await screen.findAllByText(/spring-boot-admin-sample-servlet/),
      ).toBeDefined();
      expect(
        await screen.findAllByText(/spring-boot-admin-sample-custom-ui/),
      ).toBeDefined();
      expect(
        await screen.findAllByText(/spring-boot-admin-starter-server/),
      ).toBeDefined();
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
        async () => {
          expect(
            screen.queryByText(/spring-session-jdbc/),
          ).not.toBeInTheDocument();
          expect(screen.queryByText(/spring-webmvc/)).toBeInTheDocument();
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
        async () => {
          expect(
            screen.queryByText(/SBOM id 'application'/),
          ).not.toBeInTheDocument();
        },
        { timeout: 2000 },
      );
    });

    it('should hide svg if no dependencies found for filter', async () => {
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
        { timeout: 2000 },
      );
    });

    it('should toggle svg visibility if dependencies found for filter', async () => {
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
        { timeout: 2000 },
      );

      await dependencyTree.rerender({
        sbomId: 'application',
        instance: instance,
        filter: 'webmvc',
      });

      await waitFor(
        () => {
          expect(screen.getByTestId('treecontainer-svg')).toBeVisible();
          expect(screen.queryByText(/spring-webmvc/)).toBeInTheDocument();
        },
        { timeout: 2000 },
      );
    });
  },
  { timeout: 60000 },
);
