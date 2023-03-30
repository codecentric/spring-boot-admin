import { ref } from 'vue';

import SbaNavbar from './sba-navbar.vue';

import SbaDropdownDivider from '@/components/sba-dropdown/sba-dropdown-divider.vue';
import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaNavDropdown from '@/components/sba-nav/sba-nav-dropdown.vue';
import SbaNavItem from '@/components/sba-nav/sba-nav-item.vue';
import SbaNavbarNav from '@/components/sba-navbar/sba-navbar-nav.vue';

export default {
  component: SbaNavbar,
  title: 'Components/Navbar',
};

const Template = (args) => {
  return {
    components: {
      SbaNavbar,
      SbaDropdownItem,
      SbaNavDropdown,
      SbaNavItem,
      SbaNavbarNav,
      SbaDropdownDivider,
    },
    setup() {
      const random = ref(0);
      setInterval(() => {
        random.value = Math.round(Math.random() * 10);
      }, 1000);
      return { ...args, random, username: 'Admin' };
    },
    template: `
      <sba-navbar :brand='brand'>
      <sba-navbar-nav>
        <sba-nav-item>Wallboard</sba-nav-item>
        <sba-nav-item>Applications</sba-nav-item>
        <sba-nav-item>About</sba-nav-item>
        <sba-nav-item href='#'>UP: {{ random }}</sba-nav-item>
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
      </sba-navbar-nav>

      <sba-navbar-nav class='ml-auto'>
        <sba-nav-dropdown text='English'>
          <sba-dropdown-item>한국어</sba-dropdown-item>
          <sba-dropdown-item>简体中文</sba-dropdown-item>
          <sba-dropdown-item>繁體中文</sba-dropdown-item>
          <sba-dropdown-item>português (Brasil)</sba-dropdown-item>
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
      </sba-navbar-nav>
      </sba-navbar>
      <article class='mt-16 p-4 prose max-w-full '>
      <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur mollis vulputate elit eget hendrerit.
        Suspendisse pretium odio sit amet nibh rhoncus, non cursus magna mollis. Mauris blandit aliquet consequat. Donec
        et volutpat mauris, in volutpat tortor. Ut sed nisl sed est sodales interdum eget ac sem. Mauris neque mauris,
        fringilla quis dictum quis, aliquam ac ipsum. Phasellus congue eu massa pharetra finibus. Aenean massa libero,
        egestas sit amet sem consequat, vestibulum porta sem. Duis nisi nibh, gravida a molestie a, gravida a turpis.
        Quisque sagittis nisl eu ultricies mollis. Etiam rutrum faucibus dolor faucibus hendrerit. Curabitur volutpat
        fermentum dolor, nec consectetur urna blandit non. Proin sed eleifend tellus, aliquam semper mauris.</p>
      <p>Duis sapien diam, tempus vel quam rhoncus, commodo pulvinar magna. Suspendisse condimentum, lectus quis varius
        iaculis, nibh lectus mattis neque, aliquam ornare mauris diam quis leo. Nunc efficitur mollis accumsan. Fusce
        eget lacinia nisl. Sed congue leo sem, non fermentum mauris blandit ut. Nulla aliquam mattis erat at venenatis.
        Donec nec nisl nec tortor convallis rhoncus. Class aptent taciti sociosqu ad litora torquent per conubia nostra,
        per inceptos himenaeos. Maecenas id tempor orci. Quisque sit amet nisi quis neque maximus facilisis quis rutrum
        mi. Aenean vel blandit mauris. In quis lobortis diam. Quisque fringilla mauris sit amet magna aliquet, vulputate
        hendrerit sapien blandit.</p>
      </article>
    `,
  };
};

export const Default = Template.bind({});
Default.args = {
  brand:
    '<img src=".storybook/img/icon-spring-boot-admin.svg">Spring Boot Admin',
};
