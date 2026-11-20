export type EventoFinanceiro = "APORTE" | "VALORIZACAO" | "IMPOSTO" | "RESGATE" | "ARREDONDAMENTO";
export type StatusRegraFinanceira = "APROVADA" | "PENDENTE" | "CONFLITANTE";

export interface CenarioReferenciaRegraRequest {
  produtoId: string | null;
  nome: string;
  valorEntrada: number | null;
  aporteMensal: number | null;
  prazoDias: number | null;
  taxaPercentual: number | null;
  valorBrutoEsperado: number | null;
  impostoEsperado: number | null;
  valorLiquidoEsperado: number | null;
  valorArredondadoEsperado: number | null;
  casasDecimais: number | null;
  observacoes: string | null;
}

export interface RegraFinanceiraEventoRequest {
  evento: EventoFinanceiro;
  descricao: string;
  formula: string;
  cenariosReferencia: CenarioReferenciaRegraRequest[];
}

export interface RegraFinanceiraRequest {
  tipoProduto: string;
  versao: string;
  status: StatusRegraFinanceira;
  observacoes: string | null;
  eventos: RegraFinanceiraEventoRequest[];
}

export interface CenarioReferenciaRegra extends CenarioReferenciaRegraRequest {
  id?: string;
  produtoNome?: string | null;
}

export interface RegraFinanceiraEvento extends Omit<RegraFinanceiraEventoRequest, "cenariosReferencia"> {
  cenariosReferencia: CenarioReferenciaRegra[];
}

export interface RegraFinanceira extends RegraFinanceiraRequest {
  id: string;
  createdAt: string;
  updatedAt: string;
  prontaParaAplicacao: boolean;
  pendencias: string[];
  eventos: RegraFinanceiraEvento[];
}

export interface ValidacaoRegraFinanceira {
  tipoProduto: string;
  statusDocumento: "APROVADA" | "PENDENTE" | "CONFLITANTE" | "AUSENTE";
  podeProcessar: boolean;
  pendencias: string[];
}
