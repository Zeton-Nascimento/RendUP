import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type { SimulacaoRequest, SimulacaoResponse } from "../models/simulacao.models";

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
}
