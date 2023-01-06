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

export const LoginForm = Template.bind({});
LoginForm.args = {
  title: 'Spring Boot Admin',
  param: {},
};
