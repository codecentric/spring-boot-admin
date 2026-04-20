import userEvent from '@testing-library/user-event';
import { render, screen, waitFor, within } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';
import { defineComponent, h, markRaw } from 'vue';
import { createI18n } from 'vue-i18n';
import { createMemoryHistory, createRouter } from 'vue-router';

import Sidebar from './sidebar.vue';

import Application from '@/services/application';
import Instance from '@/services/instance';
import { VIEW_GROUP } from '@/views/ViewGroup';

const TestComponent = defineComponent({
  render() {
    return h('div');
  },
});

const RouterLinkStub = defineComponent({
  props: {
    to: {
      type: [String, Object],
      required: true,
    },
  },
  setup(_, { slots, attrs }) {
    return () => h('a', attrs, slots.default?.());
  },
});

describe('Sidebar', () => {
  it('sets openGroup to the currently navigated group', async () => {
    const { router, views } = await renderSidebar({
      initialRouteName: 'instances/web/health',
    });

    await waitFor(() => {
      expect(screen.getByText('Overview')).toBeInTheDocument();
      expect(screen.getByText('Health')).toBeInTheDocument();
    });
    expect(screen.queryByText('Environment')).not.toBeInTheDocument();
    expect(screen.queryByText('Config Props')).not.toBeInTheDocument();

    await router.push({
      name: views.dataConfigpropsView.name,
      params: { instanceId: 'instance-1' },
    });

    await waitFor(() => {
      expect(screen.getByText('Environment')).toBeInTheDocument();
      expect(screen.getByText('Config Props')).toBeInTheDocument();
    });
    expect(screen.queryByText('Overview')).not.toBeInTheDocument();
    expect(screen.queryByText('Health')).not.toBeInTheDocument();
  });

  it('toggles the currently navigated group closed by button click', async () => {
    const user = userEvent.setup();

    const { container } = await renderSidebar({
      initialRouteName: 'instances/web/health',
    });

    await waitFor(() => {
      expect(screen.getByText('Overview')).toBeInTheDocument();
      expect(screen.getByText('Health')).toBeInTheDocument();
    });

    await user.click(getGroupToggleButton(container, VIEW_GROUP.WEB));

    await waitFor(() => {
      expect(screen.queryByText('Overview')).not.toBeInTheDocument();
      expect(screen.queryByText('Health')).not.toBeInTheDocument();
    });
  });

  it('opens another non-navigated group by button click', async () => {
    const user = userEvent.setup();

    const { container } = await renderSidebar({
      initialRouteName: 'instances/web/health',
    });

    await waitFor(() => {
      expect(screen.getByText('Overview')).toBeInTheDocument();
      expect(screen.getByText('Health')).toBeInTheDocument();
    });

    await user.click(getGroupToggleButton(container, VIEW_GROUP.DATA));

    await waitFor(() => {
      expect(screen.getByText('Environment')).toBeInTheDocument();
      expect(screen.getByText('Config Props')).toBeInTheDocument();
    });
    expect(screen.queryByText('Overview')).not.toBeInTheDocument();
    expect(screen.queryByText('Health')).not.toBeInTheDocument();
  });
});

async function renderSidebar({
  initialRouteName,
}: {
  initialRouteName: string;
}) {
  const views = createViews();
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/instances/:instanceId/details',
        name: 'instances/details',
        component: TestComponent,
      },
      {
        path: '/instances/:instanceId/web/overview',
        name: views.webOverviewView.name,
        component: TestComponent,
        meta: { view: views.webOverviewView },
      },
      {
        path: '/instances/:instanceId/web/health',
        name: views.webHealthView.name,
        component: TestComponent,
        meta: { view: views.webHealthView },
      },
      {
        path: '/instances/:instanceId/data/env',
        name: views.dataEnvView.name,
        component: TestComponent,
        meta: { view: views.dataEnvView },
      },
      {
        path: '/instances/:instanceId/data/configprops',
        name: views.dataConfigpropsView.name,
        component: TestComponent,
        meta: { view: views.dataConfigpropsView },
      },
    ],
  });

  await router.push({
    name: initialRouteName,
    params: { instanceId: 'instance-1' },
  });
  await router.isReady();

  return {
    router,
    views,
    ...render(Sidebar, {
      props: {
        views: Object.values(views),
        instance: {
          id: 'instance-1',
          registration: { name: 'test-app' },
          statusInfo: { status: 'UP' },
          metadataParsed: {},
        } as Instance,
        application: {} as Application,
      },
      global: {
        plugins: [
          router,
          createI18n({
            locale: 'en',
            messages: { en: {} },
            legacy: false,
            fallbackWarn: false,
            missingWarn: false,
          }),
        ],
        stubs: {
          RouterLink: RouterLinkStub,
          Divider: true,
          FontAwesomeIcon: true,
        },
      },
    }),
  };
}

function createViews() {
  return {
    webOverviewView: createView({
      name: 'instances/web/overview',
      group: VIEW_GROUP.WEB,
      order: 10,
      label: 'sidebar.web.overview',
      handleText: 'Overview',
    }),
    webHealthView: createView({
      name: 'instances/web/health',
      group: VIEW_GROUP.WEB,
      order: 20,
      label: 'sidebar.web.health',
      handleText: 'Health',
    }),
    dataEnvView: createView({
      name: 'instances/data/env',
      group: VIEW_GROUP.DATA,
      order: 30,
      label: 'sidebar.data.env',
      handleText: 'Environment',
    }),
    dataConfigpropsView: createView({
      name: 'instances/data/configprops',
      group: VIEW_GROUP.DATA,
      order: 40,
      label: 'sidebar.data.configprops',
      handleText: 'Config Props',
    }),
  };
}

function getGroupToggleButton(container: Element, groupId: string) {
  const group = container.querySelector(`[data-sba-group="${groupId}"]`);

  if (!group) {
    throw new Error(`Group ${groupId} not found`);
  }

  return within(group as HTMLElement).getByRole('button');
}

function createView({
  name,
  group,
  order,
  label,
  handleText,
}: {
  name: string;
  group: string;
  order: number;
  label: string;
  handleText: string;
}): SbaView {
  return {
    id: name,
    name,
    parent: 'instances',
    handle: markRaw(
      defineComponent({
        render() {
          return h('span', handleText);
        },
      }),
    ),
    order,
    component: markRaw(TestComponent),
    group,
    hasChildren: false,
    props: {},
    isEnabled: () => true,
    label,
  } as SbaView;
}
