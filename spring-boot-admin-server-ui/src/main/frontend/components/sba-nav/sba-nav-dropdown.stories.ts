import SbaDropdownDivider from '@/components/sba-dropdown/sba-dropdown-divider.vue';
import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaNavDropdown from '@/components/sba-nav/sba-nav-dropdown.vue';
import SbaNavItem from '@/components/sba-nav/sba-nav-item.vue';

export default {
  component: SbaNavDropdown,
  title: 'Components/Navbar/Nav/Dropdown',
};

const Template = (args) => {
  return {
    components: {
      SbaDropdownItem,
      SbaNavDropdown,
      SbaNavItem,
      SbaDropdownDivider,
    },
    setup() {
      return { ...args };
    },
    template: `
      <div class='gap-2 flex flex-col p-2 bg-black'>
      <sba-nav-dropdown text='Simple Dropdown'>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
      </sba-nav-dropdown>
      <sba-nav-dropdown text='Dropdown Is Link' href='#'>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
        <sba-dropdown-item>A Menu Item</sba-dropdown-item>
      </sba-nav-dropdown>
      <sba-nav-dropdown>
        <template #label>
          <font-awesome-icon
            color='white'
            class='w-10 rounded-full white mr-2'
            icon='user-circle'
          />
          <strong v-text='username' />
        </template>
        <sba-dropdown-item>Signed in as: <strong v-text='username' /></sba-dropdown-item>
        <sba-dropdown-divider />
        <sba-dropdown-item>
          <font-awesome-icon icon='sign-out-alt' />
          &nbsp;
          Logout
        </sba-dropdown-item>
      </sba-nav-dropdown>
      </div>
    `,
  };
};

export const Default = Template.bind({});
Default.args = {};
