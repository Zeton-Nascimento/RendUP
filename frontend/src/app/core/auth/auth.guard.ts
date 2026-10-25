import { inject } from "@angular/core";
import { CanActivateFn, Router, RouterStateSnapshot } from "@angular/router";
import { AuthService } from "./auth.service";

export const authGuard: CanActivateFn = (_route, state: RouterStateSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.getToken();
  if (!token) return router.createUrlTree(["/login"], { queryParams: { returnUrl: state.url } });
  if (auth.isTokenExpired(token)) {
    auth.logout();
    return router.createUrlTree(["/login"], { queryParams: { returnUrl: state.url } });
  }

  return true;
};
