import Login from './login.vue';

export default {
  component: Login,
  title: 'Components/Form/Login',
};

const Template = (args) => ({
  components: { Login },
  setup() {
    return { args };
  },
  template: '<Login v-bind="args" />',
});

export const LoginForm = {
  render: Template,

  args: {
    title: 'Spring Boot Admin',
    param: {},
  },
};
