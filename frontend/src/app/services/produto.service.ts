import { inject, Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type { Pagina, ProdutoFinanceiro, ProdutoFiltros } from "../models/produto.models";

@Injectable({ providedIn: "root" })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/produtos`;

  listar(filtros: ProdutoFiltros = {}) {
    let params = new HttpParams()
      .set("page", String(filtros.page ?? 0))
      .set("size", String(filtros.size ?? 6))
      .set("ordenacao", filtros.ordenacao ?? "NOME");

    if (filtros.nome) params = params.set("nome", filtros.nome);
    if (filtros.tipo) params = params.set("tipo", filtros.tipo);
    if (filtros.nivelRisco) params = params.set("nivelRisco", filtros.nivelRisco);
    if (filtros.liquidez) params = params.set("liquidez", filtros.liquidez);

    return this.http.get<Pagina<ProdutoFinanceiro>>(this.base, { params });
  }
}
