import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { finalize, tap } from "rxjs";

import { environment } from "../../../environments/environment";
import {
  AuthUsuario,
  LoginRequest,
  LoginResponse,
  RegistroRequest,
  UsuarioResponse,
} from "../../models/auth.models";
import { AuthState } from "../state/auth.state";

@Injectable({ providedIn: "root" })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly authState = inject(AuthState);

  constructor() {
    this.hydrateUsuarioFromTokenIfNeeded();
  }

  getToken(): string | null {
    return this.authState.accessToken();
  }

  isTokenExpired(token: string): boolean {
    const payload = this.decodeJwtPayload(token);
    const expRaw = payload ? payload["exp"] : null;
    const exp = typeof expRaw === "number" ? expRaw : null;
    if (!exp) return false;
    return Date.now() >= exp * 1000;
  }

  logout(): void {
    this.authState.logout();
  }

  private hydrateUsuarioFromTokenIfNeeded(): void {
    if (this.authState.usuario()) return;
    const token = this.getToken();
    if (!token) return;
    this.hydrateUsuarioFromToken(token);
  }

  private hydrateUsuarioFromToken(token: string): void {
    const payload = this.decodeJwtPayload(token);
    if (!payload) return;

    const nome = this.coalesceString(payload["nome"], payload["name"]);
    const email = this.coalesceString(payload["email"], payload["sub"]);
    const perfilInvestidor = this.coalesceString(payload["perfilInvestidor"], payload["perfil"]);
    const permissoes = this.coalesceArray(
      payload["permissoes"],
      payload["permissions"],
      payload["roles"],
      payload["authorities"],
    );

    const usuario = { ...this.authState.usuario() } as AuthUsuario;

    if (nome) usuario.nome = nome;
    if (email) usuario.email = email;
    if (
      perfilInvestidor === "CONSERVADOR" ||
      perfilInvestidor === "MODERADO" ||
      perfilInvestidor === "ARROJADO"
    ) {
      usuario.perfilInvestidor = perfilInvestidor;
    }
    if (permissoes) usuario.permissoes = permissoes;

    if (Object.keys(usuario).length > 0) this.authState.setUsuario(usuario);
  }

  private decodeJwtPayload(token: string): Record<string, unknown> | null {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    const payload = parts[1];

    try {
      const normalized = payload.replaceAll("-", "+").replaceAll("_", "/");
      const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), "=");
      const json = atob(padded);
      return JSON.parse(json) as Record<string, unknown>;
    } catch {
      return null;
    }
  }

  private coalesceString(...values: unknown[]): string | null {
    for (const v of values) {
      if (typeof v === "string" && v.trim().length > 0) return v;
    }
    return null;
  }

  private coalesceArray(...values: unknown[]): string[] | null {
    for (const v of values) {
      if (Array.isArray(v) && v.every((x): x is string => typeof x === "string")) return v;
      if (typeof v === "string" && v.trim().length > 0) return [v];
    }
    return null;
  }

  login(request: LoginRequest) {
    this.authState.setLoading(true);
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/v1/auth/login`, request)
      .pipe(
        tap((resp) => {
          const token = this.coalesceString(resp.accessToken, resp.token);
          if (!token) throw new Error("Resposta de login sem token.");

          this.authState.setAccessToken(token);
          this.authState.setUsuario(resp.usuario ?? null);
          this.hydrateUsuarioFromToken(token);
        }),
        finalize(() => this.authState.setLoading(false)),
      );
  }

  registrar(request: RegistroRequest) {
    this.authState.setLoading(true);
    return this.http
      .post<UsuarioResponse>(`${this.baseUrl}/v1/auth/registrar`, request)
      .pipe(
        tap((resp) => this.authState.setUsuario(resp)),
        finalize(() => this.authState.setLoading(false)),
      );
  }

  registro(request: RegistroRequest) {
    return this.registrar(request);
  }
}
