import { ref } from 'vue';

import SbaNavbarToggle from '@/components/sba-navbar/sba-navbar-toggle.vue';

export default {
  component: SbaNavbarToggle,
  title: 'Components/Navbar/Toggle',
};

const Template = (args) => {
  return {
    components: {
      SbaNavbarToggle,
    },
    setup() {
      const showMenu = ref(false);
      // eslint-disable-next-line no-console
      const handleClick = (e) => console.log(e);
      return { ...args, showMenu, onClick: handleClick };
    },
    template: `
      <sba-navbar-toggle :show-menu='showMenu' @click='handleClick' />
    `,
  };
};

export const Default = Template.bind({});
Default.args = {};
