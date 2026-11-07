import { inject, Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type { Pagina, ProdutoFinanceiro, ProdutoFiltros } from "../models/produto.models";
import type {
  ItemCatalogoProdutoImportRequest,
  ProdutoAdminFiltros,
  ProdutoFinanceiroAdminRequest,
  ResultadoImportacaoResponse,
} from "../models/admin-produto.models";

@Injectable({ providedIn: "root" })
export class ProdutoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/produtos`;
  private readonly adminBase = `${this.base}/admin`;

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

  buscarPorId(id: string) {
    return this.http.get<ProdutoFinanceiro>(`${this.base}/${id}`);
  }

  buscarRelacionados(id: string) {
    return this.http.get<ProdutoFinanceiro[]>(`${this.base}/${id}/relacionados`);
  }

  listarAdmin(filtros: ProdutoAdminFiltros = {}) {
    let params = new HttpParams()
      .set("page", String(filtros.page ?? 0))
      .set("size", String(filtros.size ?? 10));

    if (filtros.nome) params = params.set("nome", filtros.nome);
    if (filtros.tipo) params = params.set("tipo", filtros.tipo);
    if (filtros.nivelRisco) params = params.set("nivelRisco", filtros.nivelRisco);
    if (filtros.liquidez) params = params.set("liquidez", filtros.liquidez);
    if (typeof filtros.ativo === "boolean") params = params.set("ativo", String(filtros.ativo));

    return this.http.get<Pagina<ProdutoFinanceiro>>(this.adminBase, { params });
  }

  criarAdmin(request: ProdutoFinanceiroAdminRequest) {
    return this.http.post<ProdutoFinanceiro>(this.base, request);
  }

  atualizarAdmin(id: string, request: ProdutoFinanceiroAdminRequest) {
    return this.http.put<ProdutoFinanceiro>(`${this.base}/${id}`, request);
  }

  desativarAdmin(id: string) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  importarCatalogoAdmin(itens: ItemCatalogoProdutoImportRequest[]) {
    return this.http.post<ResultadoImportacaoResponse>(`${this.base}/import`, itens);
  }
}
