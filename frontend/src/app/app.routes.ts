import { Routes } from "@angular/router";

import { authGuard } from "./core/auth/auth.guard";
import { adminGuard } from "./core/auth/admin.guard";
import { produtoAdminGuard } from "./core/auth/produto-admin.guard";
import { LoginComponent } from "./pages/auth/login/login.component";
import { RegistroComponent } from "./pages/auth/registro/registro.component";
import { MainLayoutComponent } from "./layout/main-layout/main-layout.component";

export const routes: Routes = [
  { path: "", pathMatch: "full", redirectTo: "login" },
  { path: "login", component: LoginComponent },
  { path: "registro", component: RegistroComponent },
  {
    path: "",
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: "dashboard",
        loadComponent: () => import("./pages/dashboard/dashboard.component").then((m) => m.DashboardComponent),
      },
      {
        path: "produtos",
        loadComponent: () => import("./pages/produtos/produtos.component").then((m) => m.ProdutosComponent),
      },
      {
        path: "produtos/:id",
        loadComponent: () =>
          import("./pages/produtos/detalhe/produto-detalhe.component").then((m) => m.ProdutoDetalheComponent),
      },
      {
        path: "simulador",
        loadComponent: () => import("./pages/simulador/simulador.component").then((m) => m.SimuladorComponent),
      },
      {
        path: "simulacoes",
        loadComponent: () => import("./pages/simulacoes/simulacoes.component").then((m) => m.SimulacoesComponent),
      },
      {
        path: "perfil",
        loadComponent: () => import("./pages/perfil/perfil.component").then((m) => m.PerfilComponent),
      },
      {
        path: "admin/usuarios",
        canActivate: [adminGuard],
        loadComponent: () =>
          import("./pages/admin/usuarios/admin-usuarios.component").then((m) => m.AdminUsuariosComponent),
      },
      {
        path: "admin/produtos",
        canActivate: [produtoAdminGuard],
        loadComponent: () =>
          import("./pages/admin/produtos/admin-produtos.component").then((m) => m.AdminProdutosComponent),
      },
      {
        path: "admin/regras-financeiras",
        canActivate: [adminGuard],
        loadComponent: () =>
          import("./pages/admin/regras-financeiras/admin-regras-financeiras.component").then(
            (m) => m.AdminRegrasFinanceirasComponent,
          ),
      },
    ],
  },
  { path: "**", redirectTo: "login" },
];
