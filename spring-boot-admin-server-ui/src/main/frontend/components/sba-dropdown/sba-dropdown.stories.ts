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
      return { ...args };
    },
    template: `
      <div class='flex p-2 justify-center'>
        <sba-dropdown text='Simple Dropdown'>
          <sba-dropdown-item>First Action</sba-dropdown-item>
          <sba-dropdown-item>Second Action</sba-dropdown-item>
          <sba-dropdown-item>Third Action</sba-dropdown-item>
          <sba-dropdown-divider></sba-dropdown-divider>
          <sba-dropdown-item active>Active action</sba-dropdown-item>
          <sba-dropdown-item disabled>Disabled action</sba-dropdown-item>
        </sba-dropdown>
      </div>
    `,
  };
};

export const Default = Template.bind({});
Default.args = {};
