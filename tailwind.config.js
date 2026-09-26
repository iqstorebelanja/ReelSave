/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        neon: '#00D1FF',
        neonHover: '#00B8E6',
        neonGlow: 'rgba(0, 209, 255, 0.35)',
        gold: '#FFD700',
        darkBg: '#090A0F',
        darkCard: '#12141C',
        darkBorder: 'rgba(255, 255, 255, 0.08)',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
      },
      boxShadow: {
        'neon': '0 0 25px rgba(0, 209, 255, 0.3)',
        'neon-lg': '0 0 45px rgba(0, 209, 255, 0.45)',
        'gold': '0 0 25px rgba(255, 215, 0, 0.3)',
      },
    },
  },
  plugins: [],
}
