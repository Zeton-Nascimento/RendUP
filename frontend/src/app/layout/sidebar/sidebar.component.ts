import { CommonModule } from "@angular/common";
import { Component, inject } from "@angular/core";
import { RouterLink, RouterLinkActive } from "@angular/router";

import { AuthState } from "../../core/state/auth.state";

@Component({
  selector: "app-sidebar",
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: "./sidebar.component.html",
  styleUrl: "./sidebar.component.scss",
})
export class SidebarComponent {
  private readonly authState = inject(AuthState);

  readonly usuario = this.authState.usuario;

  get isAdmin(): boolean {
    const usuario = this.usuario();
    return usuario?.perfilUsuario === "ADMIN" || usuario?.permissoes?.includes("ROLE_ADMIN") === true;
  }
}
