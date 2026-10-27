import { computed, Injectable, signal } from "@angular/core";

import type { AuthUsuario } from "../../models/auth.models";

const STORAGE_KEY = "access_token";

@Injectable({ providedIn: "root" })
export class AuthState {
  private readonly _accessToken = signal<string | null>(localStorage.getItem(STORAGE_KEY));

  private readonly _usuario = signal<AuthUsuario | null>(null);
  private readonly _loading = signal(false);

  readonly accessToken = this._accessToken.asReadonly();
  readonly autenticado = computed(() => Boolean(this._accessToken()));
  readonly isAuthenticated = this.autenticado;

  readonly usuario = this._usuario.asReadonly();
  readonly loading = this._loading.asReadonly();

  setAccessToken(token: string | null): void {
    this._accessToken.set(token);
    if (token) localStorage.setItem(STORAGE_KEY, token);
    else localStorage.removeItem(STORAGE_KEY);
  }

  setUsuario(usuario: AuthUsuario | null): void {
    this._usuario.set(usuario);
  }

  setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  logout(): void {
    this.setAccessToken(null);
    this.setUsuario(null);
  }
}
