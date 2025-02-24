import SbaPanel from './sba-panel.vue';

export default {
  component: SbaPanel,
  title: 'Components/Panel',
};

const Template = (args) => {
  return {
    components: { SbaPanel },
    setup() {
      return { args };
    },
    methods: {
      onClose(event) {
        alert('Close clicked! ' + JSON.stringify(event));
      },
    },
    template: `
      <sba-panel @close="onClose" v-bind="args">
      <template #actions v-if="${'actions' in args}">
        ${args.actions}
      </template>
      <template #default>
        ${args.slot}
      </template>
      <template #footer v-if="${'footer' in args}">
        ${args.footer}
      </template>
      </sba-panel>
    `,
  };
};

export const WithTitle = {
  render: Template,

  args: {
    title: 'Title',
    slot: `<article class="prose max-w-none">
  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus vitae dolor ac ante ornare pharetra. Proin
              laoreet ex et lacinia hendrerit. Fusce sed justo at nulla pellentesque maximus sed at diam. Suspendisse sem
              lorem,
              lobortis vel orci quis, efficitur porta massa. In vel neque justo. Maecenas dapibus quam ut nisl porta,
              molestie
              egestas felis maximus. Proin vehicula, lacus vehicula lacinia tristique, dui turpis sodales orci, ac pretium
              nibh
              nisl sed est. Vivamus pharetra tristique mi. Nam libero lorem, pharetra eu sagittis ac, elementum quis quam.
              Integer
              sed feugiat dui. In euismod, ante id lobortis vehicula, libero leo pellentesque orci, ac consectetur leo sem
              nec
              erat. Nunc dapibus eu est at pretium. Curabitur eget elementum risus.</p>

            <p>Aenean convallis tempus dolor. Mauris eget ipsum tortor. Mauris congue facilisis eros. Phasellus tortor urna,
              semper
              congue nisl maximus, pulvinar luctus justo. Vestibulum dignissim malesuada magna, imperdiet blandit est
              commodo
              vitae. Sed a suscipit nisi, non imperdiet orci. Nulla rutrum ligula ut velit ultrices, non tincidunt lacus
              elementum. Etiam vitae blandit arcu, nec congue felis. Praesent fermentum vehicula risus, vitae finibus felis
              vestibulum ac. In ullamcorper tellus vitae ante euismod, eget consectetur nibh efficitur. Donec iaculis
              placerat
              erat a rutrum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.
              Donec
              semper erat nec ipsum molestie, eu commodo dui lobortis.</p>

            <p>Aenean convallis tempus dolor. Mauris eget ipsum tortor. Mauris congue facilisis eros. Phasellus tortor urna,
              semper
              congue nisl maximus, pulvinar luctus justo. Vestibulum dignissim malesuada magna, imperdiet blandit est
              commodo
              vitae. Sed a suscipit nisi, non imperdiet orci. Nulla rutrum ligula ut velit ultrices, non tincidunt lacus
              elementum. Etiam vitae blandit arcu, nec congue felis. Praesent fermentum vehicula risus, vitae finibus felis
              vestibulum ac. In ullamcorper tellus vitae ante euismod, eget consectetur nibh efficitur. Donec iaculis
              placerat
              erat a rutrum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.
              Donec
              semper erat nec ipsum molestie, eu commodo dui lobortis.</p>
  </article>`,
  },
};

export const WithTable = {
  render: Template,

  args: {
    ...WithTitle.args,
    slot: `
      <div class="-mx-4 -my-3">
        <div class="bg-white px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt class="text-sm font-medium text-gray-500">Count</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">97</dd>
        </div>
        <div class="bg-gray-50 px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt class="text-sm font-medium text-gray-500">Time total</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">0.4890s</dd>
        </div>
        <div class="bg-white px-4 py-3 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt class="text-sm font-medium text-gray-500">Max duration</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">0.0040s</dd>
        </div>
      </div>`,
  },
};

export const Closable = {
  render: Template,

  args: {
    ...WithTitle.args,
    closeable: true,
  },
};

export const ClosableWithActions = {
  render: Template,

  args: {
    ...WithTitle.args,
    closeable: true,
    actions: '<i>Action Slot</i>',
  },
};

export const StickyHeader = {
  render: Template,

  args: {
    ...WithTitle.args,
    closeable: true,
  },
};

export const NoTitle = {
  render: Template,

  args: {
    ...WithTitle.args,
    title: undefined,
  },
};

export const WithTitleAndFooter = {
  render: Template,

  args: {
    ...WithTitle.args,
    footer: 'Hello from the footer!',
  },
};

export const LoadingContent = {
  render: Template,

  args: {
    ...WithTitle.args,
    loading: true,
  },
};
