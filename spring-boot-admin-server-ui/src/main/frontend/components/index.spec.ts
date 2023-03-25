import micromatch from 'micromatch';

// transient dependency by vite

describe('index.js', () => {
  const isMatch = micromatch.matcher('!(*stories|*spec).(vue|js)');

  it.each`
    filename                                  | isRecognized
    ${'sba-action-button-scoped.spec.js'}     | ${false}
    ${'sba-action-button-scoped.stories.js'}  | ${false}
    ${'sba-action-button-scoped.stories.vue'} | ${false}
    ${'sba-action-button-scoped.vue'}         | ${true}
    ${'sba-action-button-scoped.js'}          | ${true}
  `('$filename: $isRecognized', ({ filename, isRecognized }) => {
    expect(isMatch(filename)).toBe(isRecognized);
  });
});
