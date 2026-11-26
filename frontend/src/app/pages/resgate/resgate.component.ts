import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import { SaldoState } from "../../core/state/saldo.state";
import { SaldoService } from "../../services/saldo.service";
import { ResgateService } from "../../services/resgate.service";
import type {
  PreviewResgateResponse,
  ResgateInvestimentoResponse,
  ResgateResponse,
} from "../../models/resgate.models";

@Component({
  selector: "app-resgate",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./resgate.component.html",
  styleUrl: "./resgate.component.scss",
})
export class ResgateComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly resgateService = inject(ResgateService);
  private readonly saldoService = inject(SaldoService);
  private readonly saldoState = inject(SaldoState);

  readonly investimento = signal<ResgateInvestimentoResponse | null>(null);
  readonly previa = signal<PreviewResgateResponse | null>(null);
  readonly resultado = signal<ResgateResponse | null>(null);
  readonly carregando = signal(true);
  readonly consultandoPrevia = signal(false);
  readonly processando = signal(false);
  readonly sucesso = signal(false);
  readonly erro = signal<string | null>(null);

  valorResgate: number | null = null;
  tipoResgate: "total" | "parcial" = "total";
  aceiteCiencia = false;
  private investimentoId = "";
  private chaveIdempotencia = "";

  ngOnInit(): void {
    this.investimentoId = this.route.snapshot.paramMap.get("investimentoId") ?? "";
    if (!this.investimentoId) {
      this.router.navigateByUrl("/dashboard");
      return;
    }

    this.resgateService
      .consultar(this.investimentoId)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (investimento) => {
          this.investimento.set(investimento);
          this.valorResgate = investimento.valorResgatavel;
        },
        error: (err) => {
          this.erro.set(String(err?.error?.erro ?? "Não foi possível carregar o investimento."));
        },
      });
  }

  visualizarPrevia(): void {
    const investimento = this.investimento();
    if (!investimento) return;

    if (this.tipoResgate === "parcial" && (!this.valorResgate || this.valorResgate <= 0)) {
      this.erro.set("Informe um valor maior que zero para o resgate parcial.");
      return;
    }

    this.consultandoPrevia.set(true);
    this.erro.set(null);
    this.previa.set(null);
    this.aceiteCiencia = false;
    this.resgateService
      .visualizarPrevia(this.investimentoId, {
        valorResgate: this.tipoResgate === "total" ? null : this.valorResgate,
      })
      .pipe(finalize(() => this.consultandoPrevia.set(false)))
      .subscribe({
        next: (resposta) => this.previa.set(resposta),
        error: (err) =>
          this.erro.set(String(err?.error?.erro ?? err?.error?.message ?? "Não foi possível gerar a prévia.")),
      });
  }

  confirmarResgate(): void {
    const previa = this.previa();
    if (!previa) {
      this.erro.set("Consulte a prévia antes de confirmar o resgate.");
      return;
    }
    if (!previa.validoParaConfirmacao) {
      this.erro.set(previa.erros.join(" ") || "O resgate não atende às regras do investimento.");
      return;
    }
    if (!this.aceiteCiencia) {
      this.erro.set("Confirme que leu os descontos, impostos e condições do resgate.");
      return;
    }
    if (this.processando()) return;

    this.processando.set(true);
    this.erro.set(null);
    if (!this.chaveIdempotencia) this.chaveIdempotencia = this.gerarChaveIdempotencia();

    this.resgateService
      .confirmar(
        this.investimentoId,
        {
          valorResgate: this.tipoResgate === "total" ? null : this.valorResgate,
          confirmar: true,
        },
        this.chaveIdempotencia,
      )
      .pipe(finalize(() => this.processando.set(false)))
      .subscribe({
        next: (resposta) => {
          this.resultado.set(resposta);
          this.sucesso.set(true);
          this.saldoState.setSaldoDisponivel(resposta.saldoDisponivel);
          this.saldoService.obterSaldo().subscribe({
            next: (saldo) => this.saldoState.setSaldoDisponivel(saldo.saldoDisponivel),
          });
        },
        error: (err) =>
          this.erro.set(String(err?.error?.erro ?? err?.error?.message ?? "Não foi possível confirmar o resgate.")),
      });
  }

  alterarResgate(): void {
    this.previa.set(null);
    this.aceiteCiencia = false;
    this.erro.set(null);
    this.chaveIdempotencia = "";
  }

  formatarMoeda(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) return "Não informado";
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarData(data: string | null | undefined): string {
    if (!data) return "Não informado";
    const partes = data.slice(0, 10).split("-");
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : data;
  }

  formatarLiquidez(valor: string | null | undefined): string {
    return {
      DIARIA: "Diária",
      D_PLUS_N: "D+N",
      NO_VENCIMENTO: "No vencimento",
    }[valor ?? ""] ?? "Não informada";
  }

  voltar(): void {
    this.router.navigateByUrl("/dashboard");
  }

  private gerarChaveIdempotencia(): string {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) return crypto.randomUUID();
    return `resgate-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }
}
