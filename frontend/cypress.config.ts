import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:5173',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.{js,jsx,ts,tsx}',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    setupNodeEvents(on, config) {
      // Exemplu: Înregistrarea unui task personalizat
      on('task', {
        log(message) {
          console.log(message);
          return null;
        },
      });
      return config; // Returnează configul modificat, dacă este cazul
    },
  },
});