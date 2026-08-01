import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  // The project keeps a single .env at the repository root, one level up.
  // Without this Vite looks only in frontend/ and every VITE_* variable
  // silently falls back to its default.
  //
  // Safe despite backend secrets (JWT_SECRET, DB password) living in that
  // file: Vite only ever exposes VITE_-prefixed variables to client code,
  // so the rest are not bundled.
  envDir: '..',
})
