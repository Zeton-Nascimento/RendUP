const { defineConfig } = require("cypress");
const createBundler = require("@bahmutov/cypress-esbuild-preprocessor");
const { addCucumberPreprocessorPlugin } = require("@badeball/cypress-cucumber-preprocessor");
const { createEsbuildPlugin } = require("@badeball/cypress-cucumber-preprocessor/esbuild");

module.exports = defineConfig({
  video: true,
  screenshotOnRunFailure: true,
  retries: { runMode: 1, openMode: 0 },
  viewportWidth: 1365,
  viewportHeight: 900,
  defaultCommandTimeout: 8000,
  e2e: {
    baseUrl: "http://localhost:4200",
    specPattern: "cypress/e2e/features/**/*.feature",
    supportFile: "cypress/support/e2e.js",
    async setupNodeEvents(on, config) {
      if (config.env?.tags) {
        config.expose = { ...(config.expose ?? {}), tags: config.env.tags };
      }
      await addCucumberPreprocessorPlugin(on, config);
      on("file:preprocessor", createBundler({ plugins: [createEsbuildPlugin(config)] }));
      return config;
    },
  },
});
