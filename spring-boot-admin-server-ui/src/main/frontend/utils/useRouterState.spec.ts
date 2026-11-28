import { flushPromises, mount } from '@vue/test-utils';
import { expect, it } from 'vitest';
import { defineComponent, nextTick } from 'vue';
import { createMemoryHistory, createRouter } from 'vue-router';

import { useRouterState } from './useRouterState';

async function mountFilters(url: string) {
  const Dashboard = defineComponent({
    setup: () => ({ filters: useRouterState({ q: '', wordWrap: true }) }),
    template: '<input v-model="filters.q" />',
  });
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [{ name: 'dashboard', path: '/', component: Dashboard }],
  });
  await router.push(url);
  const wrapper = mount(Dashboard, { global: { plugins: [router] } });
  return { router, wrapper, filters: wrapper.vm.filters };
}

it('keeps a numeric-looking application or instance search intact on reload', async () => {
  const { filters } = await mountFilters('/?q=007-orders&wordWrap=false');
  expect(filters.q).toBe('007-orders');
  expect(filters.wordWrap).toBe(false);
});

it('restores filters on back navigation and keeps the URL editable afterwards', async () => {
  const { router, wrapper, filters } = await mountFilters('/?other=keep');
  await router.push('/?q=orders&wordWrap=false&other=keep');
  await nextTick();
  expect(filters.q).toBe('orders');
  expect(filters.wordWrap).toBe(false);
  router.back();
  await flushPromises();
  expect(filters.q).toBe('');
  expect(filters.wordWrap).toBe(true);
  await wrapper.get('input').setValue('billing');
  await flushPromises();
  expect(router.currentRoute.value.query).toMatchObject({
    q: 'billing',
    other: 'keep',
  });
});

it('retains unrelated query parameters and decodes scalar values without defaults', async () => {
  const { router, wrapper, filters } = await mountFilters(
    '/?page=2&ratio=0.25&enabled=false&tag=a&tag=b',
  );
  expect(filters).toMatchObject({
    page: 2,
    ratio: 0.25,
    enabled: false,
    tag: ['a', 'b'],
  });
  await wrapper.get('input').setValue('api');
  await flushPromises();
  expect(router.currentRoute.value.query).toMatchObject({
    page: '2',
    ratio: '0.25',
    enabled: 'false',
    tag: ['a', 'b'],
    q: 'api',
  });
});
