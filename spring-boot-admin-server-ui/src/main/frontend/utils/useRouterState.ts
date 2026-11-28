import { isEqual } from 'lodash-es';
import { UnwrapNestedRefs, reactive, watch } from 'vue';
import {
  LocationQuery,
  LocationQueryRaw,
  useRoute,
  useRouter,
} from 'vue-router';

function decodeQuery(query: LocationQuery, defaults: object) {
  return Object.fromEntries(
    Object.entries(query).map(([key, value]) => {
      if (
        typeof value !== 'string' ||
        typeof Reflect.get(defaults, key) === 'string'
      )
        return [key, value];
      if (value === 'true' || value === 'false') return [key, value === 'true'];
      if (value.trim() && Number.isFinite(Number(value)))
        return [key, Number(value)];
      return [key, value];
    }),
  );
}

/** Keep dashboard controls in sync with the current route's query parameters. */
export function useRouterState<T extends object>(
  initialState: T = {} as T,
): UnwrapNestedRefs<T> {
  const route = useRoute();
  const router = useRouter();
  const readState = () => ({
    ...initialState,
    ...decodeQuery(route.query, initialState),
  });
  const state = reactive(readState());

  watch(
    () => route.query,
    () => {
      const next = readState();
      for (const key of Object.keys(state)) {
        if (!(key in next)) Reflect.deleteProperty(state, key);
      }
      Object.assign(state, next);
    },
  );

  watch(state, () => {
    const current = readState();
    const query: LocationQueryRaw = { ...route.query };
    let push = false;
    for (const [key, value] of Object.entries(state)) {
      if (isEqual(value, current[key])) continue;
      query[key] =
        Array.isArray(value) || value == null ? value : String(value);
      if (!(key in route.query)) push = true;
    }
    if (isEqual(query, route.query)) return;
    const target = { name: route.name, query };
    if (push) router.push(target);
    else router.replace(target);
  });

  return state;
}
