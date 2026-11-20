import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { finalize } from "rxjs";

import type { ProdutoFinanceiro, TipoProduto } from "../../../models/produto.models";
import type {
  CenarioReferenciaRegra,
  CenarioReferenciaRegraRequest,
  EventoFinanceiro,
  RegraFinanceira,
  RegraFinanceiraEvento,
  RegraFinanceiraEventoRequest,
  RegraFinanceiraRequest,
  StatusRegraFinanceira,
  ValidacaoRegraFinanceira,
} from "../../../models/regra-financeira.models";
import { RegraFinanceiraService } from "../../../services/regra-financeira.service";
import { ProdutoService } from "../../../services/produto.service";

@Component({
  selector: "app-admin-regras-financeiras",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin-regras-financeiras.component.html",
  styleUrl: "./admin-regras-financeiras.component.scss",
})
export class AdminRegrasFinanceirasComponent implements OnInit {
  private readonly service = inject(RegraFinanceiraService);
  private readonly produtoService = inject(ProdutoService);

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

  readonly eventos: Array<{ valor: EventoFinanceiro; label: string; ajuda: string }> = [
    { valor: "APORTE", label: "Aporte", ajuda: "Entrada do valor investido." },
    { valor: "VALORIZACAO", label: "Valorização", ajuda: "Regra para evolução do investimento." },
    { valor: "IMPOSTO", label: "Imposto", ajuda: "Regra de IR e demais descontos." },
    { valor: "RESGATE", label: "Resgate", ajuda: "Regra para retirar o investimento." },
    { valor: "ARREDONDAMENTO", label: "Arredondamento", ajuda: "Precisão e regra de arredondamento." },
  ];

  readonly statusOpcoes: Array<{ valor: StatusRegraFinanceira; label: string }> = [
    { valor: "PENDENTE", label: "Pendente de aprovação" },
    { valor: "APROVADA", label: "Aprovada" },
    { valor: "CONFLITANTE", label: "Conflitante" },
  ];

  readonly regras = signal<RegraFinanceira[]>([]);
  readonly produtosDoTipo = signal<ProdutoFinanceiro[]>([]);
  readonly validacao = signal<ValidacaoRegraFinanceira | null>(null);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly sucesso = signal<string | null>(null);

  tipoSelecionado: TipoProduto = "CDB";
  form: RegraFinanceiraRequest = this.criarFormulario("CDB");
  private regraExisteParaTipo = false;

  ngOnInit(): void {
    this.carregarLista();
    this.carregarTipoSelecionado();
  }

  selecionarTipo(): void {
    this.limparMensagens();
    this.carregarTipoSelecionado();
  }

  selecionarRegra(tipoProduto: string): void {
    this.tipoSelecionado = tipoProduto as TipoProduto;
    this.selecionarTipo();
  }

  salvar(): void {
    this.limparMensagens();
    if (!this.formularioValido) {
      this.erro.set("Preencha a versão, a descrição, a fórmula e um cenário para cada evento.");
      return;
    }

    this.salvando.set(true);
    this.service.salvar(this.tipoSelecionado, this.form)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (regra) => {
          this.form = this.mapearParaRequest(regra);
          this.sucesso.set("Regras financeiras salvas com sucesso.");
          this.validarTipoSelecionado();
          this.carregarLista();
        },
        error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível salvar as regras financeiras.")),
      });
  }

  limparFormulario(): void {
    this.form = this.criarFormulario(this.tipoSelecionado);
    this.validacao.set(null);
    this.limparMensagens();
  }

  verificar(): void {
    this.validarTipoSelecionado();
  }

  adicionarCenario(evento: RegraFinanceiraEventoRequest): void {
    evento.cenariosReferencia.push(this.criarCenario());
  }

  removerCenario(evento: RegraFinanceiraEventoRequest, indice: number): void {
    if (evento.cenariosReferencia.length <= 1) return;
    evento.cenariosReferencia.splice(indice, 1);
  }

  statusLabel(status: string | null | undefined): string {
    if (status === "APROVADA") return "Aprovada";
    if (status === "CONFLITANTE") return "Conflitante";
    if (status === "AUSENTE") return "Ausente";
    return "Pendente";
  }

  formatarTipo(tipo: string): string {
    return this.tipos.find((item) => item.valor === tipo)?.label ?? tipo;
  }

  get formularioValido(): boolean {
    return this.form.versao.trim().length > 0
      && this.form.eventos.length === this.eventos.length
      && this.form.eventos.every((evento) =>
        evento.descricao.trim().length > 0
        && evento.formula.trim().length > 0
        && evento.cenariosReferencia.length > 0
        && evento.cenariosReferencia.every((cenario) =>
          Boolean(cenario.produtoId) && cenario.nome.trim().length > 0));
  }

  private carregarLista(): void {
    this.service.listar().subscribe({
      next: (regras) => this.regras.set(regras),
      error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível carregar as regras financeiras.")),
    });
  }

  private carregarTipoSelecionado(): void {
    this.regraExisteParaTipo = false;
    this.carregarProdutosDoTipo();
    this.carregando.set(true);
    this.service.buscar(this.tipoSelecionado)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (regra) => {
          this.regraExisteParaTipo = true;
          this.form = this.mapearParaRequest(regra);
          this.validarTipoSelecionado();
        },
        error: (err) => {
          if (err?.status === 404) {
            this.regraExisteParaTipo = false;
            this.form = this.criarFormulario(this.tipoSelecionado);
            this.validarTipoSelecionado();
            return;
          }
          this.erro.set(this.extrairErro(err, "Não foi possível carregar a regra selecionada."));
        },
      });
  }

  private carregarProdutosDoTipo(): void {
    this.produtoService.listar({ tipo: this.tipoSelecionado, page: 0, size: 50 }).subscribe({
      next: (pagina) => {
        this.produtosDoTipo.set(pagina.content);
        this.preencherProdutosPadrao();
      },
      error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível carregar os produtos do tipo selecionado.")),
    });
  }

  private preencherProdutosPadrao(): void {
    if (this.regraExisteParaTipo) return;
    const produtoPadrao = this.produtosDoTipo()[0]?.id ?? null;
    if (!produtoPadrao) return;
    this.form.eventos.forEach((evento) => evento.cenariosReferencia.forEach((cenario) => {
      if (!cenario.produtoId) cenario.produtoId = produtoPadrao;
    }));
  }

  private validarTipoSelecionado(): void {
    this.service.validar(this.tipoSelecionado).subscribe({
      next: (resultado) => this.validacao.set(resultado),
      error: (err) => this.erro.set(this.extrairErro(err, "Não foi possível validar a regra selecionada.")),
    });
  }

  private criarFormulario(tipoProduto: TipoProduto): RegraFinanceiraRequest {
    return {
      tipoProduto,
      versao: "2026.1",
      status: "PENDENTE",
      observacoes: null,
      eventos: this.eventos.map((evento) => this.criarEvento(evento.valor)),
    };
  }

  private criarEvento(evento: EventoFinanceiro): RegraFinanceiraEventoRequest {
    return {
      evento,
      descricao: "",
      formula: "",
      cenariosReferencia: [this.criarCenario()],
    };
  }

  private criarCenario(): CenarioReferenciaRegraRequest {
    return {
      produtoId: this.produtosDoTipo()[0]?.id ?? null,
      nome: "",
      valorEntrada: null,
      aporteMensal: null,
      prazoDias: null,
      taxaPercentual: null,
      valorBrutoEsperado: null,
      impostoEsperado: null,
      valorLiquidoEsperado: null,
      valorArredondadoEsperado: null,
      casasDecimais: 2,
      observacoes: null,
    };
  }

  private mapearParaRequest(regra: RegraFinanceira): RegraFinanceiraRequest {
    const eventos = this.eventos.map((definicao) => {
      const existente = regra.eventos.find((item) => item.evento === definicao.valor);
      return existente ? this.mapearEvento(existente) : this.criarEvento(definicao.valor);
    });
    return {
      tipoProduto: regra.tipoProduto,
      versao: regra.versao,
      status: regra.status,
      observacoes: regra.observacoes,
      eventos,
    };
  }

  private mapearEvento(evento: RegraFinanceiraEvento): RegraFinanceiraEventoRequest {
    return {
      evento: evento.evento,
      descricao: evento.descricao,
      formula: evento.formula,
      cenariosReferencia: evento.cenariosReferencia.map((cenario) => this.mapearCenario(cenario)),
    };
  }

  private mapearCenario(cenario: CenarioReferenciaRegra): CenarioReferenciaRegraRequest {
    return {
      nome: cenario.nome,
      produtoId: cenario.produtoId,
      valorEntrada: cenario.valorEntrada,
      aporteMensal: cenario.aporteMensal,
      prazoDias: cenario.prazoDias,
      taxaPercentual: cenario.taxaPercentual,
      valorBrutoEsperado: cenario.valorBrutoEsperado,
      impostoEsperado: cenario.impostoEsperado,
      valorLiquidoEsperado: cenario.valorLiquidoEsperado,
      valorArredondadoEsperado: cenario.valorArredondadoEsperado,
      casasDecimais: cenario.casasDecimais,
      observacoes: cenario.observacoes,
    };
  }

  private limparMensagens(): void {
    this.erro.set(null);
    this.sucesso.set(null);
  }

  private extrairErro(err: any, fallback: string): string {
    return err?.error?.erro || err?.error?.message || fallback;
  }
}
