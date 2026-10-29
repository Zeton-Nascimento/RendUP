describe("RendUP — API real — inativação administrativa opcional", () => {
  it("CT-API-ADMIN-001 / US-003 — admin inativa investidor sintético e login posterior é recusado", () => {
    cy.task("rendup:verifyAdminInactivation", null, { timeout: 60000 }).then((result) => {
      expect(result.configured, "credenciais de administrador de teste disponíveis").to.equal(true);
      expect(result.completed, "fluxo de teste completado").to.equal(true);
      expect(result.registrationStatus).to.equal(201);
      expect(result.adminLoginStatus).to.equal(200);
      expect(result.deactivationStatus).to.equal(200);
      expect(result.deactivatedFlag).to.equal(true);
      expect(result.loginAfterDeactivationStatus).to.equal(401);
    });
  });
});
