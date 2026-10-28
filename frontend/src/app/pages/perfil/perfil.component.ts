import { CommonModule } from "@angular/common";
import { Component, OnInit, inject, signal } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { finalize } from "rxjs";

import { AuthState } from "../../core/state/auth.state";
import type { AuthUsuario, AtualizarUsuarioRequest, PerfilInvestidor } from "../../models/auth.models";
import { UsuarioService } from "../../services/usuario.service";

@Component({
  selector: "app-perfil",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: "./perfil.component.html",
  styleUrl: "./perfil.component.scss",
})
export class PerfilComponent implements OnInit {
  private readonly authState = inject(AuthState);
  private readonly usuarioService = inject(UsuarioService);

  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly editando = signal(false);
  readonly salvando = signal(false);
  readonly erroAtualizacao = signal<string | null>(null);
  readonly sucessoAtualizacao = signal<string | null>(null);
  readonly usuarioPerfil = signal<AuthUsuario | null>(this.authState.usuario());

  readonly perfis: ReadonlyArray<{ value: PerfilInvestidor; label: string }> = [
    { value: "CONSERVADOR", label: "Conservador" },
    { value: "MODERADO", label: "Moderado" },
    { value: "ARROJADO", label: "Arrojado" },
  ];

  readonly form = new FormGroup({
    nome: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(2)],
    }),
    telefone: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^\d{10,11}$/)],
    }),
    dataNascimento: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    perfilInvestidor: new FormControl<PerfilInvestidor>("CONSERVADOR", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit(): void {
    this.carregarPerfil();
  }

  abrirEdicao(): void {
    const usuario = this.usuarioPerfil();
    if (!usuario) return;
    this.preencherFormulario(usuario);
    this.erroAtualizacao.set(null);
    this.sucessoAtualizacao.set(null);
    this.editando.set(true);
  }

  cancelarEdicao(): void {
    const usuario = this.usuarioPerfil();
    if (usuario) this.preencherFormulario(usuario);
    this.erroAtualizacao.set(null);
    this.editando.set(false);
  }

  recarregar(): void {
    this.carregarPerfil();
  }

  salvar(): void {
    if (this.salvando()) return;

    const nome = this.form.controls.nome.value.trim();
    const telefone = this.normalizarSomenteDigitos(this.form.controls.telefone.value);
    this.form.controls.nome.setValue(nome);
    this.form.controls.telefone.setValue(telefone);
    this.form.updateValueAndValidity();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request: AtualizarUsuarioRequest = {
      nome,
      telefone,
      dataNascimento: this.form.controls.dataNascimento.value,
      perfilInvestidor: this.form.controls.perfilInvestidor.value,
    };

    this.salvando.set(true);
    this.erroAtualizacao.set(null);
    this.sucessoAtualizacao.set(null);

    this.usuarioService
      .atualizarMe(request)
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (usuario) => {
          const permissoesAtuais = this.authState.usuario()?.permissoes ?? [];
          const permissoesServidor = usuario.permissoes ?? [];
          const usuarioAtualizado: AuthUsuario = {
            ...usuario,
            permissoes: [...new Set([...permissoesAtuais, ...permissoesServidor])],
          };
          this.usuarioPerfil.set(usuarioAtualizado);
          this.authState.setUsuario(usuarioAtualizado);
          this.preencherFormulario(usuarioAtualizado);
          this.editando.set(false);
          this.sucessoAtualizacao.set("Dados atualizados com sucesso.");
        },
        error: (err) => {
          this.erroAtualizacao.set(
            String(err?.error?.message ?? err?.error?.erro ?? err?.message ?? "Não foi possível atualizar seus dados."),
          );
        },
      });
  }

  mensagemErro(campo: string): string | null {
    const control = this.form.get(campo);
    if (!control || !control.touched || !control.errors) return null;
    if (control.errors["required"]) return "Campo obrigatório.";
    if (control.errors["minlength"]) return "Informe pelo menos 2 caracteres.";
    if (control.errors["pattern"]) return "Informe telefone com 10 ou 11 dígitos.";
    return "Campo inválido.";
  }

  formatarPerfil(perfil?: PerfilInvestidor): string {
    if (perfil === "CONSERVADOR") return "Conservador";
    if (perfil === "MODERADO") return "Moderado";
    if (perfil === "ARROJADO") return "Arrojado";
    return "Não definido";
  }

  ehAdministrador(): boolean {
    const usuario = this.authState.usuario();
    return usuario?.perfilUsuario === "ADMIN" || usuario?.permissoes?.includes("ROLE_ADMIN") === true;
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

  private carregarPerfil(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.usuarioService.obterMe().pipe(finalize(() => this.carregando.set(false))).subscribe({
      next: (usuario) => {
        if (!usuario) {
          this.usuarioPerfil.set(null);
          return;
        }

        const permissoesAtuais = this.authState.usuario()?.permissoes ?? [];
        const usuarioCompleto: AuthUsuario = {
          ...usuario,
          permissoes: [...new Set([...permissoesAtuais, ...(usuario.permissoes ?? [])])],
        };
        this.usuarioPerfil.set(usuarioCompleto);
        this.authState.setUsuario(usuarioCompleto);
        this.preencherFormulario(usuarioCompleto);
      },
      error: () => {
        this.erro.set("Não foi possível carregar seu perfil.");
      },
    });
  }

  private preencherFormulario(usuario: AuthUsuario): void {
    this.form.reset({
      nome: usuario.nome ?? "",
      telefone: usuario.telefone ?? "",
      dataNascimento: usuario.dataNascimento ?? "",
      perfilInvestidor: usuario.perfilInvestidor ?? "CONSERVADOR",
    });
  }

  private normalizarSomenteDigitos(valor: string): string {
    return (valor ?? "").replaceAll(/\D/g, "");
  }
}
