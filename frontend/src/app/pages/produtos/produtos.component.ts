import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { finalize } from "rxjs";

import { ProdutoService } from "../../services/produto.service";
import type {
  NivelRisco,
  Ordenacao,
  ProdutoFinanceiro,
  TipoLiquidez,
  TipoProduto,
} from "../../models/produto.models";

@Component({
  selector: "app-produtos",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./produtos.component.html",
  styleUrl: "./produtos.component.scss",
})
export class ProdutosComponent implements OnInit {
  private readonly produtoService = inject(ProdutoService);

  readonly produtos = signal<ProdutoFinanceiro[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly totalPaginas = signal(0);
  readonly totalProdutos = signal(0);

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

  busca = "";
  tipoSelecionado: TipoProduto | "" = "";
  riscoSelecionado: NivelRisco | "" = "";
  liquidezSelecionada: TipoLiquidez | "" = "";
  ordenacao: Ordenacao = "NOME";
  pagina = 0;
  readonly tamanhoPagina = 6;

  ngOnInit(): void {
    this.buscarProdutos();
  }

  buscarProdutos(): void {
    this.carregando.set(true);
    this.erro.set(null);

    this.produtoService
      .listar({
        nome: this.busca.trim() || undefined,
        tipo: this.tipoSelecionado || undefined,
        nivelRisco: this.riscoSelecionado || undefined,
        liquidez: this.liquidezSelecionada || undefined,
        ordenacao: this.ordenacao,
        page: this.pagina,
        size: this.tamanhoPagina,
      })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (pagina) => {
          this.produtos.set(pagina.content ?? []);
          this.totalPaginas.set(pagina.totalPages ?? 0);
          this.totalProdutos.set(pagina.totalElements ?? 0);
        },
        error: (err) => {
          const mensagem = err?.error?.erro ?? "Não foi possível carregar os produtos.";
          this.erro.set(String(mensagem));
          this.produtos.set([]);
          this.totalPaginas.set(0);
          this.totalProdutos.set(0);
        },
      });
  }

  aplicarFiltros(): void {
    this.pagina = 0;
    this.buscarProdutos();
  }

  selecionarTipo(tipo: TipoProduto | ""): void {
    this.tipoSelecionado = tipo;
    this.aplicarFiltros();
  }

  limparFiltros(): void {
    this.busca = "";
    this.tipoSelecionado = "";
    this.riscoSelecionado = "";
    this.liquidezSelecionada = "";
    this.ordenacao = "NOME";
    this.aplicarFiltros();
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas() || pagina === this.pagina) return;
    this.pagina = pagina;
    this.buscarProdutos();
  }

  formatarTipo(tipo: TipoProduto): string {
    return this.tipos.find((item) => item.valor === tipo)?.label ?? tipo;
  }

  formatarRisco(risco: NivelRisco): string {
    return { BAIXO: "Baixo", MEDIO: "Médio", ALTO: "Alto" }[risco] ?? risco;
  }

  formatarLiquidez(liquidez: TipoLiquidez, dias: number): string {
    if (liquidez === "DIARIA") return "Diária";
    if (liquidez === "D_PLUS_N") return `D+${dias}`;
    return "No vencimento";
  }

  formatarMoeda(valor: number): string {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarRentabilidade(valor: number): string {
    return `${valor.toFixed(2).replace(".", ",")}% ao ano`;
  }

  get paginaAtual(): number {
    return this.pagina + 1;
  }
}
