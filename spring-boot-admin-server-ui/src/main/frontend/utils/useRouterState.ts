import _ from 'lodash';
import qs from 'qs';
import { UnwrapNestedRefs, reactive, watch } from 'vue';
import { LocationQuery, useRoute, useRouter } from 'vue-router';

/**
 * Hook to synchronize the query parameters of the current route with a reactive object.
 *
 * @param initialState The initial state of the reactive object.
 * @returns The reactive object.
 */
export function useRouterState<T extends object>(
  initialState: T = {} as T,
): UnwrapNestedRefs<T> {
  const route = useRoute();
  const router = useRouter();

  let routerState = reactive({
    ...initialState,
    ...correctTypesInRouterQuery(route.query),
  });

  watch(route, (_route) => {
    const queryParams = JSON.stringify(_route.query);
    routerState = parseQueryParams(queryParams);
  });

  watch(routerState, (newValue: any) => {
    const to = {
      name: route.name,
      query: {
        ...route.query,
        ...newValue,
      },
    };
    const routerQueryKeys = Object.keys(route.query);
    const newRouterQueryKeys = Object.keys({ ...route.query, ...newValue });
    if (_.isEqual(routerQueryKeys, newRouterQueryKeys)) {
      router.replace(to);
    } else {
      router.push(to);
    }
  });

  return routerState;
}

function parseQueryParams(queryParams: string) {
  return qs.parse(queryParams, {
    decoder: (str, defaultDecoder, charset, type) => {
      const bools = {
        true: true,
        false: false,
      };
      if (type === 'value' && typeof bools[str] === 'boolean') {
        return bools[str];
      } else {
        return defaultDecoder(str);
      }
    },
  });
}

function correctTypesInRouterQuery(query: LocationQuery) {
  return (
    query !== undefined &&
    JSON.parse(JSON.stringify(query), (_, value) => {
      if (value === 'false') return false;
      if (value === 'true') return true;

      const float = Number.parseFloat(value);
      if (!isNaN(float)) return float;

      const number = Number.parseInt(value);
      if (!isNaN(number)) return number;

      return value;
    })
  );
}
