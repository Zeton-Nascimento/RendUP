export type TipoProduto =
  | "CDB"
  | "LCI"
  | "LCA"
  | "LC"
  | "TESOURO_SELIC"
  | "POUPANCA"
  | "FUNDO"
  | "ACAO";

export type TipoLiquidez = "DIARIA" | "D_PLUS_N" | "NO_VENCIMENTO";
export type Indexador = "CDI" | "SELIC" | "IPCA" | "PREFIXADO" | "IBOVESPA";
export type NivelRisco = "BAIXO" | "MEDIO" | "ALTO";
export type Ordenacao =
  | "NOME"
  | "RENTABILIDADE"
  | "MENOR_VALOR_MINIMO"
  | "MAIOR_LIQUIDEZ"
  | "MENOR_RISCO";

export interface ProdutoFinanceiro {
  id: string;
  tipoProduto: TipoProduto;
  nome: string;
  descricao: string;
  indexador: Indexador | null;
  percentualIndexador: number | null;
  taxaPrefixada: number | null;
  carenciaDias: number | null;
  taxas: string | null;
  tributacao: string | null;
  rentabilidadePercentual: number;
  valorMinimo: number;
  nivelRisco: NivelRisco;
  liquidez: TipoLiquidez;
  diasLiquidez: number;
  prazoDias: number;
  ativo: boolean;
}

export interface Pagina<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ProdutoFiltros {
  nome?: string;
  tipo?: TipoProduto;
  nivelRisco?: NivelRisco;
  liquidez?: TipoLiquidez;
  ordenacao?: Ordenacao;
  page?: number;
  size?: number;
}
