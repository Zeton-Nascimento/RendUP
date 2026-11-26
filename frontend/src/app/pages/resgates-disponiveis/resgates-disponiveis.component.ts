import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import { ResgateService } from "../../services/resgate.service";
import type { ResgateInvestimentoResponse } from "../../models/resgate.models";

@Component({
  selector: "app-resgates-disponiveis",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./resgates-disponiveis.component.html",
  styleUrl: "./resgates-disponiveis.component.scss",
})
export class ResgatesDisponiveisComponent implements OnInit {
  private readonly resgateService = inject(ResgateService);

  readonly investimentos = signal<ResgateInvestimentoResponse[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.resgateService
      .listarDisponiveis()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (investimentos) => this.investimentos.set(investimentos ?? []),
        error: (err) => this.erro.set(String(err?.error?.erro ?? "Não foi possível carregar seus investimentos.")),
      });
  }

  formatarMoeda(valor: number): string {
    return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarData(data: string | null): string {
    if (!data) return "Não informado";
    const partes = data.slice(0, 10).split("-");
    return partes.length === 3 ? `${partes[2]}/${partes[1]}/${partes[0]}` : data;
  }

  formatarLiquidez(valor: string): string {
    return { DIARIA: "Diária", D_PLUS_N: "D+N", NO_VENCIMENTO: "No vencimento" }[valor] ?? "Não informada";
  }
}
