import type { TipoLiquidez, TipoProduto } from "./produto.models";

export type PerfilSimulacao = "CONSERVADOR" | "MODERADO" | "ARROJADO";

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
  textoEntrada: string | null;
  createdAt: string;
}
