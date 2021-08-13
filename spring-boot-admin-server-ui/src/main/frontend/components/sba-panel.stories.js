import SbaPanel from './sba-panel';

export default {
  component: SbaPanel,
  title: 'SBA Components/Panel',
};

const Template = (args, {argTypes}) => ({
  components: {SbaPanel},
  props: Object.keys(argTypes),
  template: `
    <sba-panel v-bind="$props">
    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus vitae dolor ac ante ornare pharetra. Proin
      laoreet ex et lacinia hendrerit. Fusce sed justo at nulla pellentesque maximus sed at diam. Suspendisse sem lorem,
      lobortis vel orci quis, efficitur porta massa. In vel neque justo. Maecenas dapibus quam ut nisl porta, molestie
      egestas felis maximus. Proin vehicula, lacus vehicula lacinia tristique, dui turpis sodales orci, ac pretium nibh
      nisl sed est. Vivamus pharetra tristique mi. Nam libero lorem, pharetra eu sagittis ac, elementum quis quam.
      Integer
      sed feugiat dui. In euismod, ante id lobortis vehicula, libero leo pellentesque orci, ac consectetur leo sem nec
      erat. Nunc dapibus eu est at pretium. Curabitur eget elementum risus.</p>

    <p>Aenean convallis tempus dolor. Mauris eget ipsum tortor. Mauris congue facilisis eros. Phasellus tortor urna,
      semper
      congue nisl maximus, pulvinar luctus justo. Vestibulum dignissim malesuada magna, imperdiet blandit est commodo
      vitae. Sed a suscipit nisi, non imperdiet orci. Nulla rutrum ligula ut velit ultrices, non tincidunt lacus
      elementum. Etiam vitae blandit arcu, nec congue felis. Praesent fermentum vehicula risus, vitae finibus felis
      vestibulum ac. In ullamcorper tellus vitae ante euismod, eget consectetur nibh efficitur. Donec iaculis placerat
      erat a rutrum. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Donec
      semper erat nec ipsum molestie, eu commodo dui lobortis.</p>
    </sba-panel>
  `,
});

export const WithTitle = Template.bind({});
WithTitle.args = {
  title: 'Titel'
};

export const Closable = Template.bind({});
Closable.args = {
  ...WithTitle.args,
  closeable: true
};

export const StickyHeader = Template.bind({});
StickyHeader.args = {
  ...WithTitle.args,
  closeable: true,
  headerSticksBelow: ['.os-content']
};
