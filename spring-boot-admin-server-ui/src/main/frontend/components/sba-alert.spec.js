import sbaAlert from '@/components/sba-alert';
import {render, screen} from '@testing-library/vue';

describe('sba-alert', () => {
  it('should render error message', () => {
    const props = {
      error: new Error('i am a error message'),
      title: 'This is a caption',
    }
    render(sbaAlert, {
      props,
      stubs: {
        'font-awesome-icon': true
      }
    });

    expect(screen.getByText('i am a error message')).toBeDefined();
    expect(screen.getByText('This is a caption')).toBeDefined();
  });
})
