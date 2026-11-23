import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { finalize } from "rxjs";

import { ProdutoService } from "../../services/produto.service";
import { SimulacaoService } from "../../services/simulacao.service";
import type { ProdutoFinanceiro, TipoLiquidez, TipoProduto } from "../../models/produto.models";
import type { CenarioSimulacao, PerfilSimulacao, SimulacaoResponse } from "../../models/simulacao.models";

@Component({
  selector: "app-simulador",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./simulador.component.html",
  styleUrl: "./simulador.component.scss",
})
export class SimuladorComponent implements OnInit {
  private readonly produtoService = inject(ProdutoService);
  private readonly simulacaoService = inject(SimulacaoService);
  private readonly route = inject(ActivatedRoute);

  readonly produtos = signal<ProdutoFinanceiro[]>([]);
  readonly resultado = signal<SimulacaoResponse | null>(null);
  readonly carregandoProdutos = signal(true);
  readonly carregando = signal(false);
  readonly erroProdutos = signal<string | null>(null);
  readonly erro = signal<string | null>(null);

  readonly tipos: ReadonlyArray<{ valor: TipoProduto | ""; label: string }> = [
    { valor: "", label: "Todos" },
    { valor: "CDB", label: "CDB" },
    { valor: "LCI", label: "LCI" },
    { valor: "LCA", label: "LCA" },
    { valor: "LC", label: "LC" },
    { valor: "TESOURO_SELIC", label: "Tesouro Selic" },
    { valor: "POUPANCA", label: "Poupança" },
    { valor: "FUNDO", label: "Fundo" },
    { valor: "ACAO", label: "Ação" },
  ];
  readonly prazosDisponiveis = [
    { label: "3 meses", value: 3 },
    { label: "6 meses", value: 6 },
    { label: "12 meses", value: 12 },
    { label: "24 meses", value: 24 },
    { label: "36 meses", value: 36 },
    { label: "60 meses", value: 60 },
  ];

  valorInicial = 1000;
  aporteMensal = 0;
  prazoMeses = 12;
  perfil: PerfilSimulacao = "CONSERVADOR";
  produtosSelecionados: string[] = [];
  modalAberto = false;
  buscaProduto = "";
  filtroTipo: TipoProduto | "" = "";

  ngOnInit(): void {
    this.carregarProdutos();
  }

  carregarProdutos(): void {
    this.carregandoProdutos.set(true);
    this.erroProdutos.set(null);
    this.produtoService
      .listar({ page: 0, size: 50, ordenacao: "NOME" })
      .pipe(finalize(() => this.carregandoProdutos.set(false)))
      .subscribe({
        next: (pagina) => {
          const produtos = pagina.content ?? [];
          this.produtos.set(produtos);
          const produtoId = this.route.snapshot.queryParamMap.get("produtoId");
          if (produtoId && produtos.some((produto) => produto.id === produtoId)) {
            this.produtosSelecionados = [produtoId];
          }
        },
        error: (err) => {
          this.produtos.set([]);
          this.erroProdutos.set(String(err?.error?.erro ?? "Não foi possível carregar os produtos."));
        },
      });
  }

  alternarProduto(id: string, selecionado: boolean): void {
    if (selecionado && !this.produtosSelecionados.includes(id)) {
      if (this.produtosSelecionados.length >= 4) return;
      this.produtosSelecionados = [...this.produtosSelecionados, id];
      return;
    }
    if (!selecionado) {
      this.produtosSelecionados = this.produtosSelecionados.filter((produtoId) => produtoId !== id);
    }
  }

  estaSelecionado(id: string): boolean {
    return this.produtosSelecionados.includes(id);
  }

  get produtosSelecionadosDetalhes(): ProdutoFinanceiro[] {
    return this.produtosSelecionados
      .map((id) => this.produtos().find((produto) => produto.id === id))
      .filter((produto): produto is ProdutoFinanceiro => produto !== undefined);
  }

  get produtosFiltrados(): ProdutoFinanceiro[] {
    const busca = this.buscaProduto.trim().toLowerCase();
    return this.produtos().filter((produto) => {
      const correspondeTipo = !this.filtroTipo || produto.tipoProduto === this.filtroTipo;
      const correspondeBusca = !busca || produto.nome.toLowerCase().includes(busca);
      return correspondeTipo && correspondeBusca;
    });
  }

  abrirModal(): void {
    if (this.produtosSelecionados.length < 4) this.modalAberto = true;
  }

  fecharModal(): void {
    this.modalAberto = false;
  }

  adicionarProduto(produto: ProdutoFinanceiro): void {
    if (!this.estaSelecionado(produto.id) && this.produtosSelecionados.length < 4) {
      this.produtosSelecionados = [...this.produtosSelecionados, produto.id];
    }
    this.fecharModal();
  }

  removerProduto(id: string): void {
    this.produtosSelecionados = this.produtosSelecionados.filter((produtoId) => produtoId !== id);
    this.resultado.set(null);
  }

  selecionarTipo(tipo: TipoProduto | ""): void {
    this.filtroTipo = tipo;
  }

  get cenariosOrdenados(): CenarioSimulacao[] {
    return this.resultado()?.cenarios ?? [];
  }

  get melhorCenario(): CenarioSimulacao | null {
    return this.cenariosOrdenados.length > 0 ? this.cenariosOrdenados[0] : null;
  }

  get cenarioMaiorLiquidez(): CenarioSimulacao | null {
    const ordem: Record<TipoLiquidez, number> = { DIARIA: 3, D_PLUS_N: 2, NO_VENCIMENTO: 1 };
    const cenarios = [...this.cenariosOrdenados].sort((a, b) => ordem[b.liquidez] - ordem[a.liquidez]);
    return cenarios.length > 0 ? cenarios[0] : null;
  }

  get cenarioMenorRisco(): CenarioSimulacao | null {
    const ordem: Record<string, number> = { BAIXO: 1, MEDIO: 2, ALTO: 3 };
    const cenarios = [...this.cenariosOrdenados].sort(
      (a, b) => (ordem[a.nivelRisco] ?? 4) - (ordem[b.nivelRisco] ?? 4),
    );
    return cenarios.length > 0 ? cenarios[0] : null;
  }

  simular(): void {
    this.erro.set(null);
    this.resultado.set(null);

    if (this.produtosSelecionados.length === 0) {
      this.erro.set("Selecione pelo menos um produto para simular.");
      return;
    }
    if (this.produtosSelecionados.length > 4) {
      this.erro.set("É possível comparar no máximo 4 produtos.");
      return;
    }
    if (this.valorInicial < 0 || this.aporteMensal < 0 || this.valorInicial + this.aporteMensal <= 0) {
      this.erro.set("Informe um aporte inicial ou mensal maior que zero.");
      return;
    }
    if (!Number.isInteger(this.prazoMeses) || this.prazoMeses < 1 || this.prazoMeses > 600) {
      this.erro.set("O prazo deve estar entre 1 e 600 meses.");
      return;
    }

    this.carregando.set(true);
    this.simulacaoService
      .simular({
        valorInicial: this.valorInicial,
        aporteMensal: this.aporteMensal,
        prazoMeses: this.prazoMeses,
        produtoIds: this.produtosSelecionados,
        perfil: this.perfil,
      })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (resultado) => this.resultado.set(resultado),
        error: (err) => this.erro.set(String(err?.error?.erro ?? "Não foi possível executar a simulação.")),
      });
  }

  limpar(): void {
    this.valorInicial = 1000;
    this.aporteMensal = 0;
    this.prazoMeses = 12;
    this.perfil = "CONSERVADOR";
    this.produtosSelecionados = [];
    this.modalAberto = false;
    this.buscaProduto = "";
    this.filtroTipo = "";
    this.resultado.set(null);
    this.erro.set(null);
  }

  formatarMoeda(valor: number): string {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarPercentual(valor: number): string {
    return `${valor.toFixed(2).replace(".", ",")}%`;
  }

  formatarTipo(tipo: TipoProduto): string {
    return {
      CDB: "CDB",
      LCI: "LCI",
      LCA: "LCA",
      LC: "LC",
      TESOURO_SELIC: "Tesouro Selic",
      POUPANCA: "Poupança",
      FUNDO: "Fundo",
      ACAO: "Ação",
    }[tipo] ?? tipo;
  }

  formatarLiquidez(liquidez: TipoLiquidez): string {
    return { DIARIA: "Diária", D_PLUS_N: "D+N", NO_VENCIMENTO: "No vencimento" }[liquidez] ?? liquidez;
  }

  formatarRisco(risco: string): string {
    return { BAIXO: "Baixo", MEDIO: "Médio", ALTO: "Alto" }[risco] ?? risco;
  }

  formatarTipoDado(tipo: SimulacaoResponse["tipoDado"]): string {
    return { OBSERVADO: "Observado", ESTIMADO: "Estimado", MOCKADO: "Mockado" }[tipo];
  }

  formatarData(data: string): string {
    const dataFormatada = new Date(data);
    if (Number.isNaN(dataFormatada.getTime())) return "Não informada";
    return new Intl.DateTimeFormat("pt-BR", {
      dateStyle: "short",
      timeStyle: "short",
    }).format(dataFormatada);
  }

}
