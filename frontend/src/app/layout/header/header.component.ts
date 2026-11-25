import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { Router, RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import { AuthService } from "../../core/auth/auth.service";
import { AuthState } from "../../core/state/auth.state";
import { SaldoState } from "../../core/state/saldo.state";
import { DepositarModalComponent } from "../../shared/components/depositar-modal/depositar-modal.component";
import { SaldoService } from "../../services/saldo.service";

@Component({
  selector: "app-header",
  standalone: true,
  imports: [CommonModule, RouterLink, DepositarModalComponent],
  templateUrl: "./header.component.html",
  styleUrl: "./header.component.scss",
})
export class HeaderComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly authState = inject(AuthState);
  private readonly saldoService = inject(SaldoService);
  private readonly saldoState = inject(SaldoState);
  private readonly router = inject(Router);

  readonly usuario = this.authState.usuario;
  readonly saldoDisponivel = this.saldoState.saldoDisponivel;
  readonly carregandoSaldo = this.saldoState.loading;
  readonly menuAberto = signal(false);
  readonly depositoAberto = signal(false);
  readonly depositando = signal(false);
  readonly erroDeposito = signal<string | null>(null);

  ngOnInit(): void {
    if (this.ehInvestidor()) this.recarregarSaldo();
  }

  ehInvestidor(): boolean {
    const usuario = this.usuario();
    return usuario?.perfilUsuario === "INVESTIDOR" || usuario?.permissoes?.includes("ROLE_INVESTIDOR") === true;
  }

  recarregarSaldo(): void {
    this.saldoState.setLoading(true);
    this.saldoState.setErro(null);
    this.saldoService
      .obterSaldo()
      .pipe(finalize(() => this.saldoState.setLoading(false)))
      .subscribe({
        next: (resposta) => {
          this.saldoState.setSaldoDisponivel(resposta.saldoDisponivel);
          this.saldoState.setErro(null);
        },
        error: () => {
          this.saldoState.setSaldoDisponivel(0);
          this.saldoState.setErro("Não foi possível consultar o saldo.");
        },
      });
  }

  abrirDeposito(): void {
    this.erroDeposito.set(null);
    this.depositoAberto.set(true);
  }

  fecharDeposito(): void {
    if (this.depositando()) return;
    this.depositoAberto.set(false);
    this.erroDeposito.set(null);
  }

  confirmarDeposito(valor: number): void {
    if (this.depositando()) return;

    this.depositando.set(true);
    this.erroDeposito.set(null);
    this.saldoService
      .depositar({ valor, confirmar: true })
      .pipe(finalize(() => this.depositando.set(false)))
      .subscribe({
        next: (resposta) => {
          this.saldoState.setSaldoDisponivel(resposta.saldoDisponivel);
          this.saldoState.setErro(null);
          this.depositoAberto.set(false);
        },
        error: (err) => {
          this.erroDeposito.set(
            String(err?.error?.erro ?? err?.message ?? "Não foi possível realizar o depósito."),
          );
        },
      });
  }

  toggleMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  fecharMenu(): void {
    this.menuAberto.set(false);
  }

  sair(): void {
    this.fecharMenu();
    this.authService.logout();
    this.router.navigateByUrl("/login");
  }
}
