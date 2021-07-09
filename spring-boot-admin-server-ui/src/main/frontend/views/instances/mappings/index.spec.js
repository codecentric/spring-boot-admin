import {render, screen} from '@testing-library/vue';

import Mappings from './index'
import Instance from '@/services/instance';
import VueI18n from 'vue-i18n';
import components from '@/components';

describe('Mappings', () => {
  beforeAll(() => {
    render(Mappings, {
      props: {
        instance: new Instance({id: 4711})
      }
    }, vue => {
      vue.use(VueI18n);
      vue.use(components);
      const i18n = new VueI18n()

      return {
        i18n,
      }
    })
  })

  it('should render context names as headers', async () => {
    let heading = await screen.findByText('spring-boot-admin-sample-servlet')
    expect(heading).toBeDefined()
  })
});
