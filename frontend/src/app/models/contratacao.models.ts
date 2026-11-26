import type { TipoLiquidez } from "./produto.models";

export interface PreviewContratacaoRequest {
  produtoFinanceiroId: string;
  valor: number;
}

export interface ContratacaoRequest extends PreviewContratacaoRequest {
  confirmar: boolean;
}

export interface PreviewContratacaoResponse {
  validoParaConfirmacao: boolean;
  erros: string[];
  produtoFinanceiroId: string;
  nomeProduto: string;
  valor: number;
  valorMinimo: number;
  saldoDisponivel: number;
  taxaAnualPercentual: number;
  valorBrutoEstimado: number | null;
  impostoEstimado: number | null;
  valorLiquidoEstimado: number | null;
  taxas: string | null;
  tributacao: string | null;
  nivelRisco: string;
  liquidez: TipoLiquidez;
  carenciaDias: number | null;
  prazoDias: number | null;
  dataVencimento: string | null;
}

export interface ContratacaoResponse {
  investimentoId: string;
  produtoFinanceiroId: string;
  nomeProduto: string;
  valorAplicado: number;
  saldoDisponivel: number;
  valorBrutoEstimado: number;
  impostoEstimado: number;
  valorLiquidoEstimado: number;
  taxaAnualPercentual: number;
  nivelRisco: string;
  liquidez: TipoLiquidez;
  carenciaDias: number | null;
  prazoDias: number | null;
  dataVencimento: string | null;
  contratadoEm: string;
}
