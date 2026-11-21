import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type { DepositoRequest, SaldoResponse } from "../models/saldo.models";

@Injectable({ providedIn: "root" })
export class SaldoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1`;

  obterSaldo() {
    return this.http.get<SaldoResponse>(`${this.base}/saldo`);
  }

  depositar(request: DepositoRequest) {
    return this.http.post<SaldoResponse>(`${this.base}/depositos`, request);
  }
}
