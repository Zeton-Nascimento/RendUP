import { CommonModule } from "@angular/common";
import { Component, DestroyRef, OnInit, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";

import { PerfilInvestidor } from "../../../models/auth.models";
import { AuthService } from "../../../core/auth/auth.service";
import { AuthState } from "../../../core/state/auth.state";

const CPF_REGEX = /^\d{11}$/;
const TELEFONE_REGEX = /^\d{10,11}$/;
const AGENCIA_REGEX = /^\d{4,6}$/;
const CONTA_REGEX = /^\d{5,12}$/;
const REGISTRO_PATTERN_ERROR_MESSAGES: Record<string, string> = {
  senha: "Senha precisa ter 8+ caracteres, com letra e número.",
  cpf: "CPF deve ter 11 dígitos numéricos.",
  telefone: "Telefone deve ter 10 ou 11 dígitos.",
  agencia: "Agência deve ter entre 4 e 6 dígitos.",
  numeroConta: "Conta deve ter entre 5 e 12 dígitos.",
};
const REGISTRO_FIELD_HELP_MESSAGES: Partial<Record<string, string>> = {
  senha: "Use pelo menos 8 caracteres, com ao menos uma letra e um número.",
};

@Component({
  selector: "app-registro",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: "./registro.component.html",
  styleUrl: "./registro.component.scss",
})
export class RegistroComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authApi = inject(AuthService);
  private readonly authState = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = this.authState.loading;
  readonly error = signal<string | null>(null);
  readonly sucesso = signal<string | null>(null);

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
    email: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl("", {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[A-Za-z])(?=.*\d).+$/),
      ],
    }),
    cpf: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(CPF_REGEX)],
    }),
    telefone: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(TELEFONE_REGEX)],
    }),
    dataNascimento: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required],
    }),
    banco: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3)],
    }),
    agencia: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(AGENCIA_REGEX)],
    }),
    numeroConta: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(CONTA_REGEX)],
    }),
    perfilInvestidor: new FormControl<PerfilInvestidor>("CONSERVADOR", {
      nonNullable: true,
      validators: [Validators.required],
    }),
  });

  ngOnInit(): void {
    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.error()) this.error.set(null);
      if (this.sucesso()) this.sucesso.set(null);
    });
  }

  criarConta(): void {
    if (this.loading()) return;

    this.form.controls.nome.setValue(this.form.controls.nome.value.trim());
    this.form.controls.email.setValue(this.form.controls.email.value.trim());
    this.form.controls.cpf.setValue(this.normalizarSomenteDigitos(this.form.controls.cpf.value));
    this.form.controls.telefone.setValue(this.normalizarSomenteDigitos(this.form.controls.telefone.value));
    this.form.controls.banco.setValue(this.form.controls.banco.value.trim());
    this.form.controls.agencia.setValue(this.normalizarSomenteDigitos(this.form.controls.agencia.value));
    this.form.controls.numeroConta.setValue(this.normalizarSomenteDigitos(this.form.controls.numeroConta.value));
    this.form.updateValueAndValidity();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.error.set(null);
    this.sucesso.set(null);

    const {
      nome,
      email,
      senha,
      cpf,
      telefone,
      dataNascimento,
      banco,
      agencia,
      numeroConta,
      perfilInvestidor,
    } = this.form.getRawValue();

    this.authApi
      .registro({
        nome,
        email,
        senha,
        cpf,
        telefone,
        dataNascimento,
        banco,
        agencia,
        numeroConta,
        perfilInvestidor,
      })
      .subscribe({
        next: () => {
          this.sucesso.set("Conta criada. Agora e so fazer login.");
          this.router.navigateByUrl("/login");
        },
        error: (err) => {
          const msg = String(err?.error?.message ?? err?.error ?? "").toLowerCase();
          if (msg.includes("email") || msg.includes("e-mail") || err?.status === 422) {
            this.error.set("Este e-mail já está em uso.");
            return;
          }
          this.error.set("Não foi possível criar a conta. Revise os dados e tente novamente.");
        },
      });
  }

  mensagemErro(campo: string): string | null {
    const control = this.form.get(campo);
    if (!control) return null;
    if (!control.touched || !control.errors) return null;

    if (control.errors["required"]) return "Campo obrigatório.";
    if (control.errors["email"]) return "Informe um e-mail válido.";
    if (control.errors["minlength"]) return "Quantidade mínima de caracteres não atendida.";
    if (control.errors["pattern"]) {
      return REGISTRO_PATTERN_ERROR_MESSAGES[campo] ?? "Formato inválido.";
    }
    return "Campo inválido.";
  }

  mensagemAjuda(campo: string): string | null {
    return REGISTRO_FIELD_HELP_MESSAGES[campo] ?? null;
  }

  private normalizarSomenteDigitos(valor: string): string {
    return (valor ?? "").replaceAll(/\D/g, "");
  }
}
