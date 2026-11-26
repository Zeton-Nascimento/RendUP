import type { TipoLiquidez, TipoProduto } from "./produto.models";

export type StatusInvestimento = "ATIVO" | "RESGATADO";

export interface ResgateInvestimentoResponse {
  investimentoId: string;
  nomeProduto: string;
  tipoProduto: TipoProduto;
  status: StatusInvestimento;
  valorAplicado: number;
  valorAtual: number;
  valorResgatavel: number;
  dataAplicacao: string;
  dataVencimento: string | null;
  carenciaDias: number;
  liquidez: TipoLiquidez;
  nivelRisco: string;
  bloqueado: boolean;
}

export interface PreviewResgateRequest {
  valorResgate: number | null;
}

export interface ResgateRequest {
  valorResgate: number | null;
  confirmar: boolean;
}

export interface PreviewResgateResponse {
  validoParaConfirmacao: boolean;
  erros: string[];
  investimentoId: string;
  nomeProduto: string;
  status: StatusInvestimento;
  valorAtual: number;
  valorResgatavel: number;
  valorBruto: number | null;
  impostoEstimado: number | null;
  descontoAntecipacao: number | null;
  valorLiquido: number | null;
  saldoRemanescente: number | null;
  antecipado: boolean;
  dataAplicacao: string;
  dataVencimento: string | null;
  carenciaDias: number;
  liquidez: TipoLiquidez;
  nivelRisco: string;
  bloqueado: boolean;
}

export interface ResgateResponse {
  investimentoId: string;
  valorBruto: number;
  imposto: number;
  descontoAntecipacao: number;
  valorLiquido: number;
  saldoRemanescente: number;
  saldoDisponivel: number;
  antecipado: boolean;
  resgatadoEm: string;
}
