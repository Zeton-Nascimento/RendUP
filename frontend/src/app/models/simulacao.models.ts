import type { TipoLiquidez, TipoProduto } from "./produto.models";

export type PerfilSimulacao = "CONSERVADOR" | "MODERADO" | "ARROJADO";
export type TipoDadoSimulacao = "OBSERVADO" | "ESTIMADO" | "MOCKADO";
export type IntencaoChat = "SIMULAR" | "EXPLICAR" | "DESCONHECIDA";

export interface SimulacaoRequest {
  valorInicial: number;
  aporteMensal: number;
  prazoMeses: number;
  produtoIds: string[];
  perfil: PerfilSimulacao;
}

export interface CenarioSimulacao {
  produtoId: string;
  nomeProduto: string;
  tipoProduto: TipoProduto;
  valorFinalBruto: number;
  valorFinalLiquido: number;
  rentabilidade: number;
  liquidez: TipoLiquidez;
  nivelRisco: string;
  evolucaoMensal: number[];
  ranking: number;
  destaque: string | null;
  valorInicial: number;
  aporteMensal: number;
}

export interface SimulacaoResponse {
  id: string;
  cenarios: CenarioSimulacao[];
  origem: "MANUAL";
  fonteDados: string;
  tipoDado: TipoDadoSimulacao;
  dataReferencia: string;
  metodologia: string;
  limitacao: string;
  disponivelParaDecisao: boolean;
  textoEntrada: string | null;
  createdAt: string;
}

export interface ChatRequest {
  mensagem: string;
  simulacaoIdAtual?: string;
  perfil?: PerfilSimulacao;
  produtoIds?: string[];
  prazoMesesRefinamento?: number;
  valorInicialRefinamento?: number;
  aporteMensalRefinamento?: number;
}

export interface ChatResponse {
  resposta: string;
  intencao: IntencaoChat;
  simulacaoAtualizada: SimulacaoResponse | null;
  fallback: boolean;
  sugestoes: string[];
  disclaimer: string;
}
