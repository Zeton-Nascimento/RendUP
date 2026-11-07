import type { Indexador, NivelRisco, Pagina, TipoLiquidez, TipoProduto } from "./produto.models";

export interface ProdutoFinanceiroAdminRequest {
  tipoProduto: TipoProduto;
  nome: string;
  descricao: string;
  indexador: Indexador;
  percentualIndexador: number | null;
  taxaPrefixada: number | null;
  carenciaDias: number | null;
  prazoDias: number;
  liquidez: TipoLiquidez;
  diasLiquidez: number | null;
  valorMinimo: number;
  nivelRisco: NivelRisco;
  rentabilidadePercentual: number;
  taxas: string | null;
  tributacao: string | null;
  ativo: boolean;
}

export interface ItemCatalogoProdutoImportRequest extends ProdutoFinanceiroAdminRequest {
  codigoExterno?: string | null;
  origemCatalogo?: string | null;
  versaoCatalogo?: string | null;
  payloadOriginal?: string | null;
}

export interface ProdutoAdminFiltros {
  tipo?: TipoProduto;
  nivelRisco?: NivelRisco;
  liquidez?: TipoLiquidez;
  nome?: string;
  ativo?: boolean;
  page?: number;
  size?: number;
}

export interface ImportacaoErroResponse {
  linha: number;
  motivo: string;
}

export interface ResultadoImportacaoResponse {
  importacaoId: string;
  dataImportacao: string;
  totalItens: number;
  inseridos: number;
  atualizados: number;
  rejeitados: number;
  versaoCatalogo: string | null;
  hashImportacao: string;
  erros: ImportacaoErroResponse[];
}

export type PaginaProdutosAdmin = Pagina<import("./produto.models").ProdutoFinanceiro>;
