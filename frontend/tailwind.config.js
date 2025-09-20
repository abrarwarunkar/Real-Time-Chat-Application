/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        whatsapp: {
          primary: '#25D366',
          light: '#dcf8c6',
          dark: '#075E54',
          header: '#128C7E',
          sent: '#dcf8c6',
          received: '#ffffff',
          border: '#e0e0e0',
        },
      },
      borderRadius: {
        'message': '18px',
      },
    },
  },
  plugins: [],
}