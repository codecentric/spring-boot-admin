import { h } from 'vue';

import ConfirmButtons from './ConfirmButtons';
import Modal from './Modal';
import { createComponent } from './helpers';

import eventBus from '@/services/bus';

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
      return createComponent(Modal, propsData, document.body, slots);
    },
    async confirm(title, body) {
      const bodyFn = () =>
        h(
          'span',
          {
            innerHTML: body,
          },
          [],
        );

      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const { vNode, destroy } = this.open(
        { title },
        {
          buttons: () =>
            h(ConfirmButtons, {
              labelOk: t('term.ok'),
              labelCancel: t('term.cancel'),
            }),
          body: bodyFn,
        },
      );

      return new Promise((resolve) => {
        eventBus.on('sba-modal-close', (result) => {
          destroy();
          resolve(result);
        });
      });
    },
  };
};
