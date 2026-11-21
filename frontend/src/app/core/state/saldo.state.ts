import { Injectable, signal } from "@angular/core";

@Injectable({ providedIn: "root" })
export class SaldoState {
  private readonly _saldoDisponivel = signal(0);
  private readonly _loading = signal(false);
  private readonly _erro = signal<string | null>(null);

  readonly saldoDisponivel = this._saldoDisponivel.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly erro = this._erro.asReadonly();

  setSaldoDisponivel(valor: number): void {
    this._saldoDisponivel.set(valor ?? 0);
  }

  setLoading(loading: boolean): void {
    this._loading.set(loading);
  }

  setErro(mensagem: string | null): void {
    this._erro.set(mensagem);
  }
}
