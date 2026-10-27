import { CommonModule } from "@angular/common";
import { Component, DestroyRef, OnInit, inject, signal } from "@angular/core";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from "@angular/forms";
import { ActivatedRoute, Router, RouterLink } from "@angular/router";

import { AuthService } from "../../../core/auth/auth.service";
import { AuthState } from "../../../core/state/auth.state";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: "./login.component.html",
  styleUrl: "./login.component.scss",
})
export class LoginComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authApi = inject(AuthService);
  private readonly authState = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);

  readonly loading = this.authState.loading;
  readonly error = signal<string | null>(null);

  readonly form = new FormGroup({
    email: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl("", {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(4)],
    }),
  });

  ngOnInit(): void {
    const token = this.authApi.getToken();
    if (token && !this.authApi.isTokenExpired(token)) {
      this.router.navigateByUrl("/perfil");
      return;
    }

    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.error()) this.error.set(null);
    });
  }

  entrar(): void {
    if (this.loading()) return;

    this.form.controls.email.setValue(this.form.controls.email.value.trim());
    this.form.controls.email.updateValueAndValidity();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.error.set(null);

    const email = this.form.controls.email.value;
    const senha = this.form.controls.senha.value;

    const destinoPadrao = this.authApi.hasRole("ROLE_ADMIN") ? "/admin/usuarios" : "/perfil";
    const returnUrl = this.route.snapshot.queryParamMap.get("returnUrl") ?? destinoPadrao;

    this.authApi.login({ email, senha }).subscribe({
      next: () => {
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        const msg = String(err?.error?.message ?? err?.error?.erro ?? err?.error ?? "");
        this.error.set(msg || "Email ou senha inválidos.");
      },
    });
  }
}
