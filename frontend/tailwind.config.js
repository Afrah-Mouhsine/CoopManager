/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0fdf4',
          100: '#dcfce7',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
          800: '#166534',
          900: '#14532d',
        },
        coop: {
          green: '#166534',
          olive: '#4d7c0f',
          earth: '#92400e',
          sand: '#fef3c7',
        },
      },
    },
  },
  plugins: [],
}
