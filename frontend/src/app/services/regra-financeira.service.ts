import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type {
  RegraFinanceira,
  RegraFinanceiraRequest,
  ValidacaoRegraFinanceira,
} from "../models/regra-financeira.models";

@Injectable({ providedIn: "root" })
export class RegraFinanceiraService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/regras-financeiras`;

  listar() {
    return this.http.get<RegraFinanceira[]>(this.base);
  }

  buscar(tipoProduto: string) {
    return this.http.get<RegraFinanceira>(`${this.base}/${tipoProduto}`);
  }

  salvar(tipoProduto: string, request: RegraFinanceiraRequest) {
    return this.http.put<RegraFinanceira>(`${this.base}/${tipoProduto}`, request);
  }

  validar(tipoProduto: string) {
    return this.http.get<ValidacaoRegraFinanceira>(`${this.base}/${tipoProduto}/validacao`);
  }
}
