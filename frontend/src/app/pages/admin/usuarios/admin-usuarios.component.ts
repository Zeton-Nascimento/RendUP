import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { finalize } from "rxjs";

import { AuthState } from "../../../core/state/auth.state";
import type { UsuarioResponse } from "../../../models/auth.models";
import { UsuarioService } from "../../../services/usuario.service";

@Component({
  selector: "app-admin-usuarios",
  standalone: true,
  imports: [CommonModule],
  templateUrl: "./admin-usuarios.component.html",
  styleUrl: "./admin-usuarios.component.scss",
})
export class AdminUsuariosComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly authState = inject(AuthState);

  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly sucesso = signal<string | null>(null);
  readonly inativandoId = signal<string | null>(null);
  readonly consultandoId = signal<string | null>(null);
  readonly usuarioSelecionado = signal<UsuarioResponse | null>(null);

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  inativar(usuario: UsuarioResponse): void {
    if (!usuario.ativo || this.inativandoId()) return;

    const confirmado = window.confirm(
      `Deseja inativar o usuário ${usuario.nome}? Esta ação impedirá novos acessos, mas preservará o histórico.`,
    );
    if (!confirmado) return;

    this.inativandoId.set(usuario.id);
    this.erro.set(null);
    this.sucesso.set(null);

    this.usuarioService
      .inativar(usuario.id)
      .pipe(finalize(() => this.inativandoId.set(null)))
      .subscribe({
        next: (usuarioAtualizado) => {
          this.usuarios.update((usuarios) =>
            usuarios.map((item) => item.id === usuarioAtualizado.id ? usuarioAtualizado : item),
          );
          this.sucesso.set("Usuário inativado com sucesso.");
        },
        error: (err) => {
          this.erro.set(String(err?.error?.message ?? err?.error?.erro ?? "Não foi possível inativar o usuário."));
        },
      });
  }

  consultar(usuario: UsuarioResponse): void {
    if (this.consultandoId()) return;

    this.consultandoId.set(usuario.id);
    this.erro.set(null);
    this.usuarioService
      .obterPorId(usuario.id)
      .pipe(finalize(() => this.consultandoId.set(null)))
      .subscribe({
        next: (usuarioConsultado) => this.usuarioSelecionado.set(usuarioConsultado),
        error: (err) => {
          this.erro.set(String(err?.error?.message ?? err?.error?.erro ?? "Não foi possível consultar o usuário."));
        },
      });
  }

  fecharConsulta(): void {
    this.usuarioSelecionado.set(null);
  }

  formatarData(dataIso?: string): string {
    if (!dataIso) return "-";
    const date = new Date(`${dataIso}T00:00:00`);
    if (Number.isNaN(date.getTime())) return "-";
    return new Intl.DateTimeFormat("pt-BR").format(date);
  }

  formatarTelefone(telefone?: string): string {
    if (!telefone) return "-";
    const digitos = telefone.replaceAll(/\D/g, "");
    if (digitos.length === 11) return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 7)}-${digitos.slice(7)}`;
    if (digitos.length === 10) return `(${digitos.slice(0, 2)}) ${digitos.slice(2, 6)}-${digitos.slice(6)}`;
    return telefone;
  }

  mascararCpf(cpf?: string): string {
    if (!cpf) return "-";
    const digitos = cpf.replaceAll(/\D/g, "");
    if (digitos.length !== 11) return cpf;
    return `***.***.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
  }

  recarregar(): void {
    this.carregarUsuarios();
  }

  ehUsuarioAtual(usuario: UsuarioResponse): boolean {
    const usuarioAutenticado = this.authState.usuario();
    if (!usuarioAutenticado) return false;
    if (usuarioAutenticado.id && usuario.id) return usuarioAutenticado.id === usuario.id;
    return Boolean(
      usuarioAutenticado.email &&
      usuario.email &&
      usuarioAutenticado.email.toLowerCase() === usuario.email.toLowerCase(),
    );
  }

  private carregarUsuarios(): void {
    this.carregando.set(true);
    this.erro.set(null);

    this.usuarioService
      .listarTodos()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (usuarios) => this.usuarios.set(usuarios),
        error: () => this.erro.set("Não foi possível carregar os usuários."),
      });
  }
}