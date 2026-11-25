import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { RouterLink } from "@angular/router";
import { finalize } from "rxjs";

import type { OrigemSimulacao, PerfilSimulacao, SimulacaoHistorico } from "../../models/simulacao.models";
import { SimulacaoService } from "../../services/simulacao.service";

@Component({
  selector: "app-simulacoes",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./simulacoes.component.html",
  styleUrl: "./simulacoes.component.scss",
})
export class SimulacoesComponent implements OnInit {
  private readonly simulacaoService = inject(SimulacaoService);

  readonly historico = signal<SimulacaoHistorico[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly compartilhadoId = signal<string | null>(null);
  readonly erroCompartilhamento = signal<string | null>(null);

  ngOnInit(): void {
    this.carregarHistorico();
  }

  carregarHistorico(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.simulacaoService
      .listarHistorico()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (historico) => this.historico.set(historico),
        error: (err) => this.erro.set(String(err?.error?.erro ?? "Não foi possível carregar o histórico.")),
      });
  }

  compartilhar(id: string): void {
    this.compartilhadoId.set(null);
    this.erroCompartilhamento.set(null);
    this.simulacaoService.obterResumo(id).subscribe({
      next: async (resumo) => {
        try {
          await this.copiarTexto(resumo.textoCompartilhavel);
          this.compartilhadoId.set(id);
        } catch {
          this.erroCompartilhamento.set("Não foi possível copiar o resumo. Tente novamente.");
        }
      },
      error: (err) => {
        this.erroCompartilhamento.set(String(err?.error?.erro ?? "Não foi possível gerar o resumo."));
      },
    });
  }

  private async copiarTexto(texto: string): Promise<void> {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(texto);
      return;
    }

    const area = document.createElement("textarea");
    area.value = texto;
    area.setAttribute("readonly", "true");
    area.style.position = "fixed";
    area.style.opacity = "0";
    document.body.appendChild(area);
    area.select();
    if (!document.execCommand("copy")) throw new Error("Falha ao copiar");
    area.remove();
  }

  origemLabel(origem: OrigemSimulacao): string {
    return origem === "ASSISTENTE_IA" ? "Assistente de IA" : "Simulação manual";
  }

  perfilLabel(perfil: PerfilSimulacao): string {
    return { CONSERVADOR: "Conservador", MODERADO: "Moderado", ARROJADO: "Arrojado" }[perfil];
  }

  formatarMoeda(valor: number | null): string {
    return valor === null
      ? "Não informado"
      : new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(valor);
  }

  formatarData(data: string): string {
    const dataFormatada = new Date(data);
    return Number.isNaN(dataFormatada.getTime())
      ? "Data não informada"
      : new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(dataFormatada);
  }
}
