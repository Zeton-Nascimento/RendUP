import { HttpClient, HttpHeaders } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";

import { environment } from "../../environments/environment";
import type {
  PreviewResgateRequest,
  PreviewResgateResponse,
  ResgateInvestimentoResponse,
  ResgateRequest,
  ResgateResponse,
} from "../models/resgate.models";

@Injectable({ providedIn: "root" })
export class ResgateService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/investimentos`;

  listarDisponiveis() {
    return this.http.get<ResgateInvestimentoResponse[]>(`${this.base}/resgataveis`);
  }

  consultar(investimentoId: string) {
    return this.http.get<ResgateInvestimentoResponse>(`${this.base}/${investimentoId}/resgate`);
  }

  visualizarPrevia(investimentoId: string, request: PreviewResgateRequest) {
    return this.http.post<PreviewResgateResponse>(`${this.base}/${investimentoId}/resgate/preview`, request);
  }

  confirmar(investimentoId: string, request: ResgateRequest, chaveIdempotencia: string) {
    const headers = new HttpHeaders({ "Idempotency-Key": chaveIdempotencia });
    return this.http.post<ResgateResponse>(`${this.base}/${investimentoId}/resgate`, request, { headers });
  }
}
