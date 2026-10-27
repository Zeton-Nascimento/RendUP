import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";

import { AuthService } from "../auth/auth.service";

function getObjectError(error: unknown): Record<string, unknown> {
  return typeof error === "object" && error ? (error as Record<string, unknown>) : {};
}

function buildFriendlyHttpError(
  err: HttpErrorResponse,
  message: string,
  errorOverride?: Record<string, unknown>,
): HttpErrorResponse {
  return new HttpErrorResponse({
    error: {
      ...getObjectError(err.error),
      ...errorOverride,
      message,
    },
    headers: err.headers,
    status: err.status,
    statusText: err.statusText,
    url: err.url ?? undefined,
  });
}

function extractServerMessage(err: HttpErrorResponse): string | null {
  if (typeof err.error === "string") {
    return err.error;
  }

  if (typeof err.error === "object" && err.error) {
    const errorObj = err.error as { message?: unknown; erro?: unknown };
    return (errorObj.message ?? errorObj.erro ?? null) as string | null;
  }

  return null;
}

function normalizeClientError(err: HttpErrorResponse, serverMessage: string): HttpErrorResponse {
  if (typeof err.error === "string") {
    return buildFriendlyHttpError(err, serverMessage);
  }

  if (typeof err.error === "object" && err.error && !(err.error as { message?: unknown }).message) {
    return buildFriendlyHttpError(err, serverMessage, getObjectError(err.error));
  }

  return err;
}

function getDefaultClientMessage(status: number): string {
  switch (status) {
    case 400:
      return "Requisição inválida. Revise os dados e tente novamente.";
    case 403:
      return "Acesso negado.";
    case 404:
      return "Recurso não encontrado.";
    case 422:
      return "Dados inválidos. Revise os campos e tente novamente.";
    default:
      return "Não foi possível concluir sua solicitação. Tente novamente.";
  }
}

function handleUnauthorizedError(
  err: HttpErrorResponse,
  auth: AuthService,
  router: Router,
): HttpErrorResponse {
  const serverMessage = extractServerMessage(err);
  const normalized401 =
    typeof serverMessage === "string" && serverMessage.trim().length > 0
      ? buildFriendlyHttpError(err, serverMessage, getObjectError(err.error))
      : err;

  auth.logout();
  const onAuthPage =
    router.url?.startsWith("/login") || router.url?.startsWith("/registro");
  if (!onAuthPage) {
    router.navigateByUrl("/login");
  }

  return normalized401;
}

function handleHttpError(
  err: HttpErrorResponse,
  auth: AuthService,
  router: Router,
): HttpErrorResponse {
  if (err.status === 0) {
    return buildFriendlyHttpError(
      err,
      "Falha de conexão. Verifique sua internet e tente novamente.",
    );
  }

  if (err.status === 401) {
    return handleUnauthorizedError(err, auth, router);
  }

  if (err.status >= 500) {
    return buildFriendlyHttpError(
      err,
      "Erro interno no servidor. Tente novamente em instantes.",
    );
  }

  if (err.status >= 400) {
    const serverMessage = extractServerMessage(err);
    if (typeof serverMessage === "string" && serverMessage.trim().length > 0) {
      return normalizeClientError(err, serverMessage);
    }

    return buildFriendlyHttpError(err, getDefaultClientMessage(err.status));
  }

  return err;
}

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err) => {
      if (!(err instanceof HttpErrorResponse)) {
        return throwError(() => err);
      }

      return throwError(() => handleHttpError(err, auth, router));
    }),
  );
};
