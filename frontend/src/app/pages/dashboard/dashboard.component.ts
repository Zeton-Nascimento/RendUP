import { CommonModule } from "@angular/common";
import { Component, inject } from "@angular/core";
import { RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import { AuthState } from "../../core/state/auth.state";
import { SaldoState } from "../../core/state/saldo.state";
import { SaldoService } from "../../services/saldo.service";

@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./dashboard.component.html",
  styleUrl: "./dashboard.component.scss",
})
export class DashboardComponent {
  private readonly authState = inject(AuthState);
  private readonly saldoState = inject(SaldoState);
  private readonly saldoService = inject(SaldoService);

  readonly usuario = this.authState.usuario;
  readonly saldo = this.saldoState.saldoDisponivel;
  readonly carregandoSaldo = this.saldoState.loading;
  readonly erroSaldo = this.saldoState.erro;

  ehInvestidor(): boolean {
    const usuario = this.usuario();
    return usuario?.perfilUsuario === "INVESTIDOR" || usuario?.permissoes?.includes("ROLE_INVESTIDOR") === true;
  }

  carregarSaldo(): void {
    this.saldoState.setLoading(true);
    this.saldoState.setErro(null);
    this.saldoService
      .obterSaldo()
      .pipe(finalize(() => this.saldoState.setLoading(false)))
      .subscribe({
        next: (resposta) => this.saldoState.setSaldoDisponivel(resposta.saldoDisponivel),
        error: () => {
          this.saldoState.setSaldoDisponivel(0);
          this.saldoState.setErro("Não foi possível consultar o saldo.");
        },
      });
  }
}
