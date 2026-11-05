import { CommonModule } from "@angular/common";
import { Component, inject, signal } from "@angular/core";
import { Router, RouterLink } from "@angular/router";

import { AuthService } from "../../core/auth/auth.service";
import { AuthState } from "../../core/state/auth.state";

@Component({
  selector: "app-header",
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: "./header.component.html",
  styleUrl: "./header.component.scss",
})
export class HeaderComponent {
  private readonly authService = inject(AuthService);
  private readonly authState = inject(AuthState);
  private readonly router = inject(Router);

  readonly usuario = this.authState.usuario;
  readonly menuAberto = signal(false);

  toggleMenu(): void {
    this.menuAberto.update((aberto) => !aberto);
  }

  fecharMenu(): void {
    this.menuAberto.set(false);
  }

  sair(): void {
    this.fecharMenu();
    this.authService.logout();
    this.router.navigateByUrl("/login");
  }
}
