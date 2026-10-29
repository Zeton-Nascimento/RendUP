const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: false,
  screenshotOnRunFailure: true,
  retries: { runMode: 0, openMode: 0 },
  defaultCommandTimeout: 10000,
  e2e: {
    baseUrl: "http://localhost:8080",
    specPattern: "cypress/api/**/*.cy.js",
    supportFile: "cypress/support/e2e.js",
    setupNodeEvents(on, config) {
      const priorExclude = config.excludeSpecPattern
        ? Array.isArray(config.excludeSpecPattern)
          ? config.excludeSpecPattern
          : [config.excludeSpecPattern]
        : [];
      const hasAdminCredentials = Boolean(
        process.env.RENDUP_ADMIN_EMAIL && process.env.RENDUP_ADMIN_PASSWORD,
      );

      if (!hasAdminCredentials) {
        config.excludeSpecPattern = [
          ...priorExclude,
          "cypress/api/admin-inativacao.cy.js",
        ];
      }

      on("task", {
        async "rendup:verifyAdminInactivation"() {
          const email = process.env.RENDUP_ADMIN_EMAIL;
          const password = process.env.RENDUP_ADMIN_PASSWORD;
          if (!email || !password) return { configured: false };

          const base = process.env.RENDUP_API_BASE || "http://localhost:8080";
          const postJson = (url, body, token) => fetch(`${base}${url}`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              ...(token ? { Authorization: `Bearer ${token}` } : {}),
            },
            body: JSON.stringify(body),
          });

          // Validar a conta administrativa antes de criar qualquer usuário sintético.
          const adminLogin = await postJson("/api/v1/auth/login", { email, senha: password });
          const adminBody = await adminLogin.json().catch(() => ({}));
          if (adminLogin.status !== 200 || !adminBody.accessToken) {
            return { configured: true, adminLoginStatus: adminLogin.status, completed: false };
          }

          const unique = `${Date.now()}${Math.floor(Math.random() * 10000)}`;
          const digits = unique.replace(/\D/g, "");
          const user = {
            nome: "Alvo Sintético para Inativação",
            email: `qa-alvo-${unique}@example.test`,
            senha: "SenhaTeste123",
            cpf: digits.slice(-11).padStart(11, "0"),
            telefone: "11987654321",
            dataNascimento: "1990-01-10",
            banco: "Banco QA",
            agencia: "1234",
            numeroConta: digits.slice(-7).padStart(7, "0"),
            perfilInvestidor: "CONSERVADOR",
          };
          const registration = await postJson("/api/v1/auth/registrar", user);
          const registrationBody = await registration.json().catch(() => ({}));
          if (registration.status !== 201 || !registrationBody.id) {
            return { configured: true, registrationStatus: registration.status, completed: false };
          }

          const deletion = await fetch(`${base}/api/v1/usuarios/${registrationBody.id}?confirmar=true`, {
            method: "DELETE",
            headers: { Authorization: `Bearer ${adminBody.accessToken}` },
          });
          const deletionBody = await deletion.json().catch(() => ({}));
          const loginAfter = await postJson("/api/v1/auth/login", { email: user.email, senha: user.senha });
          return {
            configured: true,
            completed: true,
            registrationStatus: registration.status,
            adminLoginStatus: adminLogin.status,
            deactivationStatus: deletion.status,
            deactivatedFlag: deletionBody.ativo === false,
            loginAfterDeactivationStatus: loginAfter.status,
          };
        },
      });

      return config;
    },
  },
});
