// Must be JavaScript as Storybook does not work otherwise.
import autoprefixer from 'autoprefixer';

import tailwindcss from 'tailwindcss';

module.exports = {
  plugins: [tailwindcss, autoprefixer],
};
