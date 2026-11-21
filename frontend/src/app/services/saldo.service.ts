import { inject, Injectable } from "@angular/core";
import { HttpClient, HttpContext } from "@angular/common/http";

import { environment } from "../../environments/environment";
import { SKIP_UNAUTHORIZED_REDIRECT } from "../core/api/api.interceptor";
import type { DepositoRequest, SaldoResponse } from "../models/saldo.models";

@Injectable({ providedIn: "root" })
export class SaldoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1`;

  obterSaldo() {
    const context = new HttpContext().set(SKIP_UNAUTHORIZED_REDIRECT, true);
    return this.http.get<SaldoResponse>(`${this.base}/saldo`, { context });
  }

  depositar(request: DepositoRequest) {
    return this.http.post<SaldoResponse>(`${this.base}/depositos`, request);
  }
}
