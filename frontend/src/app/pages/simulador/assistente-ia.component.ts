import { CommonModule } from "@angular/common";
import { Component, input, output, signal, inject } from "@angular/core";
import { FormsModule } from "@angular/forms";

import type { CenarioSimulacao, PerfilSimulacao, SimulacaoResponse } from "../../models/simulacao.models";
import { SimulacaoService } from "../../services/simulacao.service";
import { AssistenteRespostaPipe } from "./assistente-resposta.pipe";

interface MensagemChat {
  remetente: "usuario" | "assistente";
  texto: string;
}

@Component({
  selector: "app-assistente-ia",
  standalone: true,
  imports: [CommonModule, FormsModule, AssistenteRespostaPipe],
  templateUrl: "./assistente-ia.component.html",
  styleUrl: "./assistente-ia.component.scss",
})
export class AssistenteIaComponent {
  private readonly simulacaoService = inject(SimulacaoService);

  readonly cenarios = input<CenarioSimulacao[]>([]);
  readonly simulacaoId = input<string | null>(null);
  readonly perfil = input<PerfilSimulacao>("CONSERVADOR");
  readonly produtoIds = input<string[]>([]);
  readonly valorInicial = input(1000);
  readonly aporteMensal = input(0);
  readonly prazoMeses = input(12);
  readonly simulacaoAtualizada = output<SimulacaoResponse>();

  readonly mensagens = signal<MensagemChat[]>([]);
  readonly enviando = signal(false);
  readonly sugestoes = signal<string[]>([
    "Explique o resultado desta simulação",
    "O que muda se eu aumentar o prazo?",
    "Compare risco, liquidez e rentabilidade",
  ]);
  mensagem = "";

  enviarMensagem(): void {
    const texto = this.mensagem.trim();
    if (!texto || this.enviando()) return;

    this.mensagens.update((mensagens) => [...mensagens, { remetente: "usuario", texto }]);
    this.mensagem = "";
    this.enviando.set(true);

    this.simulacaoService
      .chatIA({
        mensagem: texto,
        simulacaoIdAtual: this.simulacaoId() ?? undefined,
        perfil: this.perfil(),
        produtoIds: this.produtoIds(),
      })
      .subscribe({
        next: (resposta) => {
          this.mensagens.update((mensagens) => [
            ...mensagens,
            { remetente: "assistente", texto: resposta.resposta },
          ]);
          this.sugestoes.set(resposta.sugestoes ?? []);
          if (resposta.simulacaoAtualizada) {
            this.simulacaoAtualizada.emit(resposta.simulacaoAtualizada);
          }
          this.enviando.set(false);
        },
        error: (erro) => {
          const mensagemErro = erro?.error?.message ?? erro?.error?.erro
            ?? "Não foi possível consultar o assistente agora.";
          this.mensagens.update((mensagens) => [
            ...mensagens,
            { remetente: "assistente", texto: mensagemErro },
          ]);
          this.enviando.set(false);
        },
      });
  }

  usarSugestao(sugestao: string): void {
    this.mensagem = sugestao;
    this.enviarMensagem();
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      this.enviarMensagem();
    }
  }
}
