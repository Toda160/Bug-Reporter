import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
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