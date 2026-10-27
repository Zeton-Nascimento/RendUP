import { Routes } from "@angular/router";

import { authGuard } from "./core/auth/auth.guard";
import { adminGuard } from "./core/auth/admin.guard";
import { LoginComponent } from "./pages/auth/login/login.component";
import { RegistroComponent } from "./pages/auth/registro/registro.component";

export const routes: Routes = [
  { path: "", pathMatch: "full", redirectTo: "login" },
  { path: "login", component: LoginComponent },
  { path: "registro", component: RegistroComponent },
  {
    path: "perfil",
    canActivate: [authGuard],
    loadComponent: () => import("./pages/perfil/perfil.component").then((m) => m.PerfilComponent),
  },
  {
    path: "admin/usuarios",
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import("./pages/admin/usuarios/admin-usuarios.component").then((m) => m.AdminUsuariosComponent),
  },
  { path: "**", redirectTo: "login" },
];
