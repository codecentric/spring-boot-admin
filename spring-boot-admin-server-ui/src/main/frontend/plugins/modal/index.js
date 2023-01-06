import { useModal } from './api';

const SbaModalPlugin = {
  install: (app, options = {}) => {
    let instance = useModal(options);
    app.config.globalProperties.$sbaModal = instance;
    app.provide('$sbaModal', instance);
  },
};

export default SbaModalPlugin;
