import { CommonModule } from "@angular/common";
import { Component, OnDestroy, OnInit, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { finalize } from "rxjs";

import type {
  ItemCatalogoProdutoImportRequest,
  ProdutoAdminFiltros,
  ProdutoFinanceiroAdminRequest,
  ResultadoImportacaoResponse,
} from "../../../models/admin-produto.models";
import type {
  Indexador,
  NivelRisco,
  Pagina,
  ProdutoFinanceiro,
  TipoLiquidez,
  TipoProduto,
} from "../../../models/produto.models";
import { ProdutoService } from "../../../services/produto.service";

@Component({
  selector: "app-admin-produtos",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-produtos.component.html",
  styleUrl: "./admin-produtos.component.scss",
})
export class AdminProdutosComponent implements OnInit, OnDestroy {
  private static readonly MAX_ITENS_IMPORTACAO = 100;
  private readonly produtoService = inject(ProdutoService);
  private sincronizacaoTimer: ReturnType<typeof setTimeout> | null = null;

  readonly produtos = signal<ProdutoFinanceiro[]>([]);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly importando = signal(false);
  readonly sincronizando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly sucesso = signal<string | null>(null);
  readonly resultadoImportacao = signal<ResultadoImportacaoResponse | null>(null);
  readonly resultadoSincronizacao = signal<ResultadoImportacaoResponse | null>(null);
  readonly produtoEmEdicao = signal<ProdutoFinanceiro | null>(null);
  readonly produtoParaInativar = signal<ProdutoFinanceiro | null>(null);
  readonly produtoParaAtivar = signal<ProdutoFinanceiro | null>(null);

  readonly tipos: Array<{ label: string; valor: TipoProduto }> = [
    { label: "CDB", valor: "CDB" },
    { label: "LCI", valor: "LCI" },
    { label: "LCA", valor: "LCA" },
    { label: "LC", valor: "LC" },
    { label: "Tesouro Selic", valor: "TESOURO_SELIC" },
    { label: "Poupança", valor: "POUPANCA" },
    { label: "Fundo", valor: "FUNDO" },
    { label: "Ação", valor: "ACAO" },
  ];

  readonly indexadores: Array<{ label: string; valor: Indexador }> = [
    { label: "CDI", valor: "CDI" },
    { label: "Selic", valor: "SELIC" },
    { label: "IPCA", valor: "IPCA" },
    { label: "Prefixado", valor: "PREFIXADO" },
    { label: "Ibovespa", valor: "IBOVESPA" },
  ];

  readonly liquidezOpcoes: Array<{ label: string; valor: TipoLiquidez }> = [
    { label: "Diária", valor: "DIARIA" },
    { label: "D+N", valor: "D_PLUS_N" },
    { label: "No vencimento", valor: "NO_VENCIMENTO" },
  ];

  readonly riscos: Array<{ label: string; valor: NivelRisco }> = [
    { label: "Baixo", valor: "BAIXO" },
    { label: "Médio", valor: "MEDIO" },
    { label: "Alto", valor: "ALTO" },
  ];

  form: ProdutoFinanceiroAdminRequest = this.modeloVazio();
  tentouSalvar = false;
  importacaoTexto = "";
  filtros: ProdutoAdminFiltros = {};
  nomeFiltro = "";
  tipoFiltro: TipoProduto | "" = "";
  riscoFiltro: NivelRisco | "" = "";
  liquidezFiltro: TipoLiquidez | "" = "";
  ativoFiltro: "" | "true" | "false" = "";
  pagina = 0;
  readonly tamanhoPagina = 10;
  readonly totalPaginas = signal(0);
  readonly totalProdutos = signal(0);

  ngOnInit(): void {
    this.carregar();
  }

  ngOnDestroy(): void {
    this.cancelarLimpezaSincronizacao();
  }

  salvarProduto(): void {
    this.tentouSalvar = true;
    this.normalizarCamposDinamicos();
    if (!this.formularioValido) return;

    this.salvando.set(true);
    this.limparMensagens();
    const produto = this.produtoEmEdicao();
    const request = {
      ...this.form,
      nome: this.form.nome.trim(),
      descricao: this.form.descricao.trim(),
      taxas: this.form.taxas?.trim() || null,
      tributacao: this.form.tributacao?.trim() || null,
    };
    const chamada = produto
      ? this.produtoService.atualizarAdmin(produto.id, request)
      : this.produtoService.criarAdmin(request);

    chamada.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: () => {
        this.sucesso.set(produto ? "Produto atualizado com sucesso." : "Produto criado com sucesso.");
        this.cancelarEdicao();
        this.carregar();
      },
      error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível salvar o produto.")),
    });
  }

  editarProduto(produto: ProdutoFinanceiro): void {
    this.produtoEmEdicao.set(produto);
    this.form = {
      tipoProduto: produto.tipoProduto,
      nome: produto.nome,
      descricao: produto.descricao,
      indexador: produto.indexador ?? "CDI",
      percentualIndexador: produto.percentualIndexador,
      taxaPrefixada: produto.taxaPrefixada,
      carenciaDias: produto.carenciaDias ?? 0,
      prazoDias: produto.prazoDias,
      liquidez: produto.liquidez,
      diasLiquidez: produto.diasLiquidez ?? 0,
      valorMinimo: produto.valorMinimo,
      nivelRisco: produto.nivelRisco,
      rentabilidadePercentual: produto.rentabilidadePercentual,
      taxas: produto.taxas,
      tributacao: produto.tributacao,
      ativo: produto.ativo,
    };
    this.tentouSalvar = false;
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  cancelarEdicao(): void {
    this.produtoEmEdicao.set(null);
    this.form = this.modeloVazio();
    this.tentouSalvar = false;
  }

  desativarProduto(produto: ProdutoFinanceiro): void {
    if (!produto.ativo || this.salvando()) return;
    this.produtoParaInativar.set(produto);
  }

  cancelarInativacao(): void {
    this.produtoParaInativar.set(null);
  }

  confirmarInativacao(): void {
    const produto = this.produtoParaInativar();
    if (!produto || this.salvando()) return;

    this.produtoParaInativar.set(null);
    this.salvando.set(true);
    this.limparMensagens();
    this.produtoService.desativarAdmin(produto.id)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: () => {
          this.sucesso.set("Produto inativado com sucesso.");
          this.carregar();
        },
        error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível inativar o produto.")),
      });
  }

  ativarProduto(produto: ProdutoFinanceiro): void {
    if (produto.ativo || this.salvando()) return;
    this.produtoParaAtivar.set(produto);
  }

  cancelarAtivacao(): void {
    this.produtoParaAtivar.set(null);
  }

  confirmarAtivacao(): void {
    const produto = this.produtoParaAtivar();
    if (!produto || this.salvando()) return;

    this.produtoParaAtivar.set(null);
    this.salvando.set(true);
    this.limparMensagens();
    this.produtoService.atualizarAdmin(produto.id, this.mapearProdutoParaRequest(produto, true))
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: () => {
          this.sucesso.set("Produto ativado com sucesso.");
          this.carregar();
        },
        error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível ativar o produto.")),
      });
  }

  alternarIndexador(): void {
    if (this.form.indexador === "CDI") this.form.taxaPrefixada = null;
    if (this.form.indexador === "PREFIXADO") this.form.percentualIndexador = null;
  }

  alternarLiquidez(): void {
    if (this.form.liquidez !== "D_PLUS_N") this.form.diasLiquidez = 0;
  }

  aplicarFiltros(): void {
    this.filtros = {
      nome: this.nomeFiltro.trim() || undefined,
      tipo: this.tipoFiltro || undefined,
      nivelRisco: this.riscoFiltro || undefined,
      liquidez: this.liquidezFiltro || undefined,
      ativo: this.ativoFiltro === "" ? undefined : this.ativoFiltro === "true",
    };
    this.pagina = 0;
    this.carregar();
  }

  limparFiltros(): void {
    this.nomeFiltro = "";
    this.tipoFiltro = "";
    this.riscoFiltro = "";
    this.liquidezFiltro = "";
    this.ativoFiltro = "";
    this.aplicarFiltros();
  }

  anterior(): void {
    if (this.pagina > 0) {
      this.pagina--;
      this.carregar();
    }
  }

  proxima(): void {
    if (this.pagina < this.totalPaginas() - 1) {
      this.pagina++;
      this.carregar();
    }
  }

  selecionarArquivo(event: Event): void {
    const arquivo = (event.target as HTMLInputElement).files?.[0];
    if (!arquivo) return;
    if (arquivo.size > 1_000_000) {
      this.erro.set("O arquivo de catálogo deve ter no máximo 1 MB.");
      return;
    }
    void arquivo.text().then((texto) => this.importacaoTexto = texto)
      .catch(() => this.erro.set("Não foi possível ler o arquivo selecionado."));
  }

  importar(): void {
    if (!this.importacaoTexto.trim()) {
      this.erro.set("Informe um JSON para importar.");
      return;
    }

    let itens: ItemCatalogoProdutoImportRequest[];
    try {
      const json = JSON.parse(this.importacaoTexto) as unknown;
      if (Array.isArray(json)) {
        itens = json as ItemCatalogoProdutoImportRequest[];
      } else if (typeof json === "object" && json !== null && "itens" in json && Array.isArray((json as { itens?: unknown }).itens)) {
        itens = (json as { itens: ItemCatalogoProdutoImportRequest[] }).itens;
      } else {
        throw new Error("formato");
      }
    } catch {
      this.erro.set('JSON inválido. Envie uma lista ou um objeto no formato { "itens": [] }.');
      return;
    }

    if (itens.length > AdminProdutosComponent.MAX_ITENS_IMPORTACAO) {
      this.erro.set("A importação deve conter no máximo 100 produtos.");
      return;
    }

    this.importando.set(true);
    this.limparMensagens();
    this.produtoService.importarCatalogoAdmin(itens)
      .pipe(finalize(() => this.importando.set(false)))
      .subscribe({
        next: (resultado) => {
          this.resultadoImportacao.set(resultado);
          this.sucesso.set(`Importação concluída: ${resultado.inseridos} inserido(s), ${resultado.atualizados} atualizado(s) e ${resultado.rejeitados} rejeitado(s).`);
          this.carregar();
        },
        error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível importar o catálogo.")),
      });
  }

  sincronizarCatalogo(): void {
    if (this.sincronizando()) return;

    this.sincronizando.set(true);
    this.limparMensagens();
    this.produtoService.sincronizarCatalogoAdmin()
      .pipe(finalize(() => this.sincronizando.set(false)))
      .subscribe({
        next: (resultado) => {
          this.resultadoSincronizacao.set(resultado);
          if (resultado.status === "FALHA") {
            this.erro.set(resultado.mensagem ?? "A sincronização falhou. O catálogo anterior foi preservado.");
            return;
          }

          this.sucesso.set(
            `Sincronização concluída: ${resultado.inseridos} inserido(s), ${resultado.atualizados} atualizado(s) e ${resultado.inativados ?? 0} inativado(s).`,
          );
          this.agendarLimpezaSincronizacao();
          this.carregar();
        },
        error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível sincronizar o catálogo.")),
      });
  }

  limparMensagens(): void {
    this.erro.set(null);
    this.sucesso.set(null);
  }

  get formularioValido(): boolean {
    const textoValido = this.form.nome.trim().length > 0 && this.form.descricao.trim().length > 0;
    const numerosValidos = this.form.rentabilidadePercentual >= 0 && this.form.valorMinimo >= 0 && this.form.prazoDias >= 0;
    const indexadorValido = this.form.indexador !== "CDI" || Number(this.form.percentualIndexador) > 0;
    const prefixadoValido = this.form.indexador !== "PREFIXADO" || Number(this.form.taxaPrefixada) > 0;
    const liquidezValida = this.form.liquidez !== "D_PLUS_N" || Number(this.form.diasLiquidez) > 0;
    return textoValido && numerosValidos && indexadorValido && prefixadoValido && liquidezValida;
  }

  mensagemCampo(campo: "nome" | "descricao" | "rentabilidade" | "indexador" | "prazo"): string | null {
    if (!this.tentouSalvar) return null;
    if (campo === "nome" && !this.form.nome.trim()) return "Informe o nome.";
    if (campo === "descricao" && !this.form.descricao.trim()) return "Informe a descrição.";
    if (campo === "rentabilidade" && this.form.rentabilidadePercentual < 0) return "Informe uma rentabilidade válida.";
    if (campo === "indexador" && this.form.indexador === "CDI" && Number(this.form.percentualIndexador) <= 0) return "Informe o percentual do CDI.";
    if (campo === "prazo" && this.form.prazoDias < 0) return "Informe um prazo válido.";
    return null;
  }

  formatarMoeda(valor: number): string {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarTipo(tipo: TipoProduto): string {
    return this.tipos.find((item) => item.valor === tipo)?.label ?? tipo;
  }

  formatarLiquidez(liquidez: TipoLiquidez): string {
    return this.liquidezOpcoes.find((item) => item.valor === liquidez)?.label ?? liquidez;
  }

  private carregar(): void {
    this.carregando.set(true);
    this.produtoService.listarAdmin({ ...this.filtros, page: this.pagina, size: this.tamanhoPagina })
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (pagina: Pagina<ProdutoFinanceiro>) => {
          this.produtos.set(pagina.content);
          this.totalPaginas.set(pagina.totalPages);
          this.totalProdutos.set(pagina.totalElements);
        },
        error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível carregar os produtos.")),
      });
  }

  private normalizarCamposDinamicos(): void {
    if (this.form.indexador === "CDI") this.form.taxaPrefixada = null;
    if (this.form.indexador === "PREFIXADO") this.form.percentualIndexador = null;
    if (this.form.liquidez !== "D_PLUS_N") this.form.diasLiquidez = 0;
  }

  private agendarLimpezaSincronizacao(): void {
    this.cancelarLimpezaSincronizacao();
    this.sincronizacaoTimer = setTimeout(() => {
      this.sucesso.set(null);
      this.resultadoSincronizacao.set(null);
      this.sincronizacaoTimer = null;
    }, 15_000);
  }

  private cancelarLimpezaSincronizacao(): void {
    if (this.sincronizacaoTimer !== null) {
      clearTimeout(this.sincronizacaoTimer);
      this.sincronizacaoTimer = null;
    }
  }

  private mapearProdutoParaRequest(produto: ProdutoFinanceiro, ativo: boolean): ProdutoFinanceiroAdminRequest {
    return {
      tipoProduto: produto.tipoProduto,
      nome: produto.nome,
      descricao: produto.descricao,
      indexador: produto.indexador ?? "CDI",
      percentualIndexador: produto.percentualIndexador,
      taxaPrefixada: produto.taxaPrefixada,
      carenciaDias: produto.carenciaDias ?? 0,
      prazoDias: produto.prazoDias,
      liquidez: produto.liquidez,
      diasLiquidez: produto.diasLiquidez ?? 0,
      valorMinimo: produto.valorMinimo,
      nivelRisco: produto.nivelRisco,
      rentabilidadePercentual: produto.rentabilidadePercentual,
      taxas: produto.taxas,
      tributacao: produto.tributacao,
      ativo,
    };
  }

  private modeloVazio(): ProdutoFinanceiroAdminRequest {
    return {
      tipoProduto: "CDB",
      nome: "",
      descricao: "",
      indexador: "CDI",
      percentualIndexador: 100,
      taxaPrefixada: null,
      carenciaDias: 0,
      prazoDias: 365,
      liquidez: "DIARIA",
      diasLiquidez: 0,
      valorMinimo: 100,
      nivelRisco: "BAIXO",
      rentabilidadePercentual: 0,
      taxas: null,
      tributacao: null,
      ativo: true,
    };
  }

  private extrairErro(error: unknown, fallback: string): string {
    if (typeof error === "object" && error !== null && "error" in error) {
      const payload = (error as { error?: unknown }).error;
      if (typeof payload === "object" && payload !== null) {
        const mensagem = (payload as { erro?: unknown; message?: unknown }).erro ?? (payload as { message?: unknown }).message;
        if (typeof mensagem === "string" && mensagem.trim()) return mensagem;
      }
    }
    return fallback;
  }
}
