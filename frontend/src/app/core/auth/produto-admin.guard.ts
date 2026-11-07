import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";

import { AuthState } from "../state/auth.state";

export const produtoAdminGuard: CanActivateFn = () => {
  const authState = inject(AuthState);
  const router = inject(Router);
  const permissoes = authState.usuario()?.permissoes ?? [];

  return permissoes.includes("PRODUTO_GERIR")
    ? true
    : router.createUrlTree(["/perfil"]);
};
