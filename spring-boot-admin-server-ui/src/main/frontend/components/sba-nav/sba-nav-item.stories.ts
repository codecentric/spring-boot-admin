import SbaNavItem from '@/components/sba-nav/sba-nav-item.vue';

export default {
  component: SbaNavItem,
  title: 'Components/Navbar/Nav/Item',
};

const Template = (args) => {
  return {
    components: {
      SbaNavItem,
    },
    setup() {
      return { ...args };
    },
    template: `
      <sba-nav-item>Just an item</sba-nav-item>
      <sba-nav-item href='https://www.codecentric.de'>Link</sba-nav-item>
      <sba-nav-item to='routeName'>Router Link</sba-nav-item>
    `,
  };
};

export const Default = Template.bind({});
Default.args = {};
