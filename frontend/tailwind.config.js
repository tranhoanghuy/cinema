/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js}'],
  theme: {
    extend: {
      colors: {
        surface: {
          DEFAULT: '#0f0f1a',
          card:    '#1a1a2e',
          hover:   '#252540',
          border:  '#2e2e4e'
        },
        brand: {
          DEFAULT: '#e50914',
          light:   '#ff3b47',
          dark:    '#b8070f'
        },
        seat: {
          available: '#22c55e',
          held:      '#f59e0b',
          booked:    '#374151',
          selected:  '#3b82f6',
          vip:       '#a855f7',
          couple:    '#ec4899',
          yours:     '#06b6d4'
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif']
      },
      animation: {
        'pulse-slow': 'pulse 3s cubic-bezier(0.4,0,0.6,1) infinite',
        'slide-up':   'slideUp 0.3s ease-out'
      },
      keyframes: {
        slideUp: {
          '0%':   { transform: 'translateY(16px)', opacity: '0' },
          '100%': { transform: 'translateY(0)',    opacity: '1' }
        }
      }
    }
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/line-clamp')
  ]
}
