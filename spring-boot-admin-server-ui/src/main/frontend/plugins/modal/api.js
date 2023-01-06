import { h } from 'vue';

import ConfirmButtons from './ConfirmButtons';
import Modal from './Modal';
import eventBus from './bus';
import { createComponent } from './helpers';

export const useModal = (globalProps = {}) => {
  const t =
    globalProps.i18n?.global.t ||
    function (value) {
      return value;
    };

  return {
    open(options, slots = {}) {
      let title = null;
      if (typeof options === 'string') title = options;

      const defaultProps = {
        title,
      };

      const propsData = Object.assign({}, defaultProps, globalProps, options);
      createComponent(Modal, propsData, document.body, slots);
    },
    async confirm(title, body) {
      this.open(
        { title },
        {
          buttons: () =>
            h(ConfirmButtons, {
              labelOk: t('term.ok'),
              labelCancel: t('term.cancel'),
            }),
          body: () => h('span', { innerHTML: body }),
        }
      );

      return new Promise((resolve) => {
        eventBus.on('sba-modal-close', resolve);
      });
    },
  };
};
