import SbaDropdownDivider from '@/components/sba-dropdown/sba-dropdown-divider.vue';
import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaDropdown from '@/components/sba-dropdown/sba-dropdown.vue';

export default {
  component: SbaDropdown,
  title: 'Components/Dropdown',
};

const Template = (args) => {
  return {
    components: {
      SbaDropdown,
      SbaDropdownItem,
      SbaDropdownDivider,
    },
    setup() {
      const click = () => alert('Clicked button!');
      return { ...args, click };
    },
    template: `
      <div class='flex p-2 justify-center'>
        <sba-dropdown text='Simple Dropdown'>
          <sba-dropdown-item @click='click'>On Click</sba-dropdown-item>
          <sba-dropdown-item href='#'>A link!</sba-dropdown-item>
          <sba-dropdown-item to='name'>router-link</sba-dropdown-item>
          <sba-dropdown-divider></sba-dropdown-divider>
          <sba-dropdown-item active>Active action</sba-dropdown-item>
          <sba-dropdown-item disabled>Disabled action</sba-dropdown-item>
          <sba-dropdown-divider></sba-dropdown-divider>
          <sba-dropdown-item as='button'>Button</sba-dropdown-item>
        </sba-dropdown>
      </div>

    `,
  };
};

export const Default = Template.bind({});
Default.args = {};
