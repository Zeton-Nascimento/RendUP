import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import { SaldoState } from "../../core/state/saldo.state";
import { SaldoService } from "../../services/saldo.service";
import { ContratacaoService } from "../../services/contratacao.service";
import { ProdutoService } from "../../services/produto.service";
import type { ContratacaoResponse, PreviewContratacaoResponse } from "../../models/contratacao.models";
import type { ProdutoFinanceiro } from "../../models/produto.models";

@Component({
  selector: "app-contratacao",
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: "./contratacao.component.html",
  styleUrl: "./contratacao.component.scss",
})
export class ContratacaoComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly produtoService = inject(ProdutoService);
  private readonly saldoService = inject(SaldoService);
  private readonly saldoState = inject(SaldoState);
  private readonly contratacaoService = inject(ContratacaoService);

  readonly produto = signal<ProdutoFinanceiro | null>(null);
  readonly previa = signal<PreviewContratacaoResponse | null>(null);
  readonly contratacao = signal<ContratacaoResponse | null>(null);
  readonly carregando = signal(true);
  readonly consultandoPrevia = signal(false);
  readonly contratando = signal(false);
  readonly sucesso = signal(false);
  readonly erro = signal<string | null>(null);
  readonly saldoDisponivel = this.saldoState.saldoDisponivel;

  valor: number | null = null;
  aceiteCiencia = false;
  private produtoId = "";
  private chaveIdempotencia = "";

  ngOnInit(): void {
    this.produtoId = this.route.snapshot.paramMap.get("produtoId") ?? "";
    if (!this.produtoId) {
      this.router.navigateByUrl("/produtos");
      return;
    }

    this.produtoService
      .buscarPorId(this.produtoId)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (produto) => {
          this.produto.set(produto);
          this.valor = produto.valorMinimo;
          this.carregarSaldo();
        },
        error: (err) => {
          this.erro.set(String(err?.error?.erro ?? "Não foi possível carregar o produto."));
        },
      });
  }

  carregarSaldo(): void {
    this.saldoService.obterSaldo().subscribe({
      next: (resposta) => this.saldoState.setSaldoDisponivel(resposta.saldoDisponivel),
    });
  }

  visualizarPrevia(): void {
    if (!this.valor || this.valor <= 0) {
      this.erro.set("Informe um valor maior que zero.");
      return;
    }

    this.consultandoPrevia.set(true);
    this.erro.set(null);
    this.previa.set(null);
    this.contratacaoService
      .visualizarPrevia({ produtoFinanceiroId: this.produtoId, valor: this.valor })
      .pipe(finalize(() => this.consultandoPrevia.set(false)))
      .subscribe({
        next: (resposta) => this.previa.set(resposta),
        error: (err) =>
          this.erro.set(String(err?.error?.erro ?? err?.error?.message ?? "Não foi possível gerar a prévia.")),
      });
  }

  confirmarContratacao(): void {
    const previa = this.previa();
    if (!previa) {
      this.erro.set("Visualize a prévia antes de confirmar a contratação.");
      return;
    }
    if (!previa.validoParaConfirmacao) {
      this.erro.set(previa.erros.join(" ") || "A contratação não atende às regras do produto.");
      return;
    }
    if (!this.aceiteCiencia || this.contratando()) {
      if (!this.aceiteCiencia) this.erro.set("Confirme que leu as regras do produto antes de contratar.");
      return;
    }

    this.contratando.set(true);
    this.erro.set(null);
    if (!this.chaveIdempotencia) this.chaveIdempotencia = this.gerarChaveIdempotencia();

    this.contratacaoService
      .contratar(
        { produtoFinanceiroId: this.produtoId, valor: this.valor ?? 0, confirmar: true },
        this.chaveIdempotencia,
      )
      .pipe(finalize(() => this.contratando.set(false)))
      .subscribe({
        next: (resposta) => {
          this.contratacao.set(resposta);
          this.saldoState.setSaldoDisponivel(resposta.saldoDisponivel);
          this.sucesso.set(true);
        },
        error: (err) =>
          this.erro.set(
            String(err?.error?.erro ?? err?.error?.message ?? "Não foi possível contratar o investimento."),
          ),
      });
  }

  voltarParaNovaPrevia(): void {
    this.previa.set(null);
    this.aceiteCiencia = false;
    this.erro.set(null);
    this.chaveIdempotencia = "";
  }

  formatarMoeda(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) return "Não informado";
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarPercentual(valor: number | null | undefined): string {
    if (valor === null || valor === undefined) return "Não informado";
    return `${valor.toFixed(2).replace(".", ",")}% a.a.`;
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

  formatarTipo(tipo: string | null | undefined): string {
    return { TESOURO_SELIC: "Tesouro Selic", POUPANCA: "Poupança", ACAO: "Ação" }[tipo ?? ""] ?? tipo ?? "";
  }

  formatarRisco(risco: string | null | undefined): string {
    return { BAIXO: "Baixo", MEDIO: "Médio", ALTO: "Alto" }[risco ?? ""] ?? risco ?? "Não informado";
  }

  private gerarChaveIdempotencia(): string {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) return crypto.randomUUID();
    return `contratacao-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  }
}
