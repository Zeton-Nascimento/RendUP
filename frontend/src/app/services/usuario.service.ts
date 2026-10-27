import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";

import { environment } from "../../environments/environment";
import type { AtualizarUsuarioRequest, UsuarioResponse } from "../models/auth.models";

@Injectable({ providedIn: "root" })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/v1/usuarios`;

  obterMe() {
    return this.http.get<UsuarioResponse>(`${this.base}/me`);
  }

  listarTodos() {
    return this.http.get<UsuarioResponse[]>(this.base);
  }

  atualizarMe(request: AtualizarUsuarioRequest) {
    return this.http.put<UsuarioResponse>(`${this.base}/me`, request);
  }

  inativar(id: string) {
    return this.http.delete<UsuarioResponse>(`${this.base}/${id}`, {
      params: { confirmar: true },
    });
  }
}
