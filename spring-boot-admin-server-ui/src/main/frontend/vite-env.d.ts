/// <reference types="vite/client" />
/// <reference types="./global" />
declare module '*.vue' {
  import { DefineComponent } from 'vue';
  const component: DefineComponent<object, object, any>;
  export default component;
}
