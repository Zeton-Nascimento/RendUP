import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type {
  ChatRequest,
  ChatResponse,
  SimulacaoHistorico,
  SimulacaoRequest,
  SimulacaoResponse,
  SimulacaoResumo,
} from "../models/simulacao.models";

@Injectable({ providedIn: "root" })
export class SimulacaoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/simulacoes`;

  simular(request: SimulacaoRequest) {
    return this.http.post<SimulacaoResponse>(this.base, request);
  }

  buscarPorId(id: string) {
    return this.http.get<SimulacaoResponse>(`${this.base}/${id}`);
  }

  listarHistorico() {
    return this.http.get<SimulacaoHistorico[]>(this.base);
  }

  obterResumo(id: string) {
    return this.http.get<SimulacaoResumo>(`${this.base}/${id}/resumo`);
  }

  chatIA(request: ChatRequest) {
    return this.http.post<ChatResponse>(`${environment.apiUrl}/v1/chat/ia`, request);
  }
}
