import {render} from '@/test-utils';
import TriggerRow from './trigger-row';
import {screen} from '@testing-library/vue';
import userEvent from '@testing-library/user-event';


describe('trigger-row.vue', () => {

  beforeEach(async () =>{
    await render(TriggerRow, {
      props: {
        triggerDetail: {
          'group': 'group2',
          'name': 'triggerSampleJob2',
          'state': 'NORMAL',
          'type': 'simple',
          'startTime': 1629451789546,
          'previousFireTime': 1629452183546,
          'nextFireTime': 1629452185546,
          'priority': 0,
          'simple': {'interval': 2000, 'repeatCount': -1, 'timesTriggered': 198}
        }
      }
    })
  })

  it('should render with closed details initially', async () => {
    const intervalElement = await screen.queryByText('interval')
    expect(intervalElement).toBeNull()
  })

  it('should render details when clicking on arrow', async () => {
    const toggleArrow = await screen.findByRole('button')
    await userEvent.click(toggleArrow)
    const intervalElement = await screen.findByText('interval')
    expect(intervalElement).toBeVisible()
  })
})
