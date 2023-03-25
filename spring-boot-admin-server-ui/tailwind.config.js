module.exports = {
  mode: 'jit',
  darkMode: 'class',
  content: ['./index.html', './login.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  safelist: [
    {
      pattern: /.*-sba-.*/,
    },
  ],
  theme: {
    extend: {
      colors: {
        sba: {
          DEFAULT: 'var(--main-500)',
          50: withOpacity('--main-50'),
          100: withOpacity('--main-100'),
          200: withOpacity('--main-200'),
          300: withOpacity('--main-300'),
          400: withOpacity('--main-400'),
          500: withOpacity('--main-500'),
          600: withOpacity('--main-600'),
          700: withOpacity('--main-700'),
          800: withOpacity('--main-800'),
          900: withOpacity('--main-900'),
        },
        orange: {
          DEFAULT: '#ED8936',
          50: '#FCECDF',
          100: '#FAE1CC',
          200: '#F7CBA6',
          300: '#F4B581',
          400: '#F09F5B',
          500: '#ED8936',
          600: '#D86C13',
          700: '#A4520F',
          800: '#71390A',
          900: '#3D1F05',
        },
        teal: {
          DEFAULT: '#38B2AC',
          50: '#B9E9E7',
          100: '#A9E4E1',
          200: '#8ADAD6',
          300: '#6BD0CB',
          400: '#4CC7C1',
          500: '#38B2AC',
          600: '#2B8783',
          700: '#1D5D5A',
          800: '#103230',
          900: '#020707',
        },
      },
      maxWidth: {
        sm: '24rem',
      },
      columns: {
        2: '14rem',
      },
      fontSize: {
        '9xl': '7rem',
      },
    },
  },
  plugins: [require('@tailwindcss/forms'), require('@tailwindcss/typography')],
};

function withOpacity(variableName) {
  return ({ opacityValue }) => {
    if (opacityValue !== undefined) {
      return `rgba(var(${variableName}), ${opacityValue})`;
    }
    return `rgb(var(${variableName}))`;
  };
}
