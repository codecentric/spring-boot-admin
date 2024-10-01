import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import ConditionsListDetails from '@/views/instances/conditions/conditions-list-details.vue';

describe('ConditionsListDetails', () => {
  it('should display condition', async () => {
    render(ConditionsListDetails, {
      props: {
        condition: {
          condition: 'SpringBootAdminServerEnabledCondition',
        },
      },
    });
    expect(
      await screen.findByLabelText('instances.conditions.condition'),
    ).toHaveTextContent('SpringBootAdminServerEnabledCondition');
  });

  it('should display message', async () => {
    render(ConditionsListDetails, {
      props: {
        condition: {
          message: 'matched',
        },
      },
    });
    expect(
      await screen.findByLabelText('instances.conditions.message'),
    ).toHaveTextContent('matched');
  });

  it.each`
    condition
    ${undefined}
    ${null}
    ${''}
  `(
    'should not display condition if condition is $condition',
    async ({ condition }) => {
      render(ConditionsListDetails, {
        props: {
          condition: {
            condition,
          },
        },
      });
      expect(
        screen.queryByLabelText('instances.conditions.condition'),
      ).not.toBeInTheDocument();
    },
  );

  it.each`
    message
    ${undefined}
    ${null}
    ${''}
  `(
    'should not display message if message is $message',
    async ({ message }) => {
      render(ConditionsListDetails, {
        props: {
          condition: {
            message,
          },
        },
      });
      expect(
        screen.queryByLabelText('instances.conditions.message'),
      ).not.toBeInTheDocument();
    },
  );
});
