import { HttpClient, HttpHeaders } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";

import { environment } from "../../environments/environment";
import type {
  ContratacaoRequest,
  ContratacaoResponse,
  PreviewContratacaoRequest,
  PreviewContratacaoResponse,
} from "../models/contratacao.models";

@Injectable({ providedIn: "root" })
export class ContratacaoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/contratacao`;

  visualizarPrevia(request: PreviewContratacaoRequest) {
    return this.http.post<PreviewContratacaoResponse>(`${this.base}/preview`, request);
  }

  contratar(request: ContratacaoRequest, chaveIdempotencia: string) {
    const headers = new HttpHeaders({ "Idempotency-Key": chaveIdempotencia });
    return this.http.post<ContratacaoResponse>(this.base, request, { headers });
  }
}
