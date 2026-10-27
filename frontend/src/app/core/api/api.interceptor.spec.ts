import { HttpErrorResponse, HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthService } from '../auth/auth.service';
import { apiInterceptor } from './api.interceptor';

describe('apiInterceptor', () => {
  const authService = {
    logout: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
      ],
    });
  });

  function interceptError(error: unknown): unknown {
    const req = new HttpRequest('GET', '/api/teste');
    const next = vi.fn(() => throwError(() => error));
    let captured: unknown;

    TestBed.runInInjectionContext(() => apiInterceptor(req, next)).subscribe({
      error: (intercepted) => {
        captured = intercepted;
      },
    });

    return captured;
  }

  it('traduz falha de conexao em mensagem amigavel', () => {
    const error = new HttpErrorResponse({ status: 0, statusText: 'Unknown Error' });

    const result = interceptError(error) as HttpErrorResponse;

    expect(result.status).toBe(0);
    expect(result.error.message).toBe('Falha de conexão. Verifique sua internet e tente novamente.');
  });

  it('faz logout e redireciona para login em erro 401 fora das telas de autenticacao', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    Object.defineProperty(router, 'url', { configurable: true, value: '/dashboard' });

    const result = interceptError(
      new HttpErrorResponse({
        status: 401,
        error: { message: 'Sessao expirada.' },
      }),
    ) as HttpErrorResponse;

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
    expect(result.error.message).toBe('Sessao expirada.');
  });

  it('nao navega novamente quando o erro 401 acontece na propria tela de login', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    Object.defineProperty(router, 'url', { configurable: true, value: '/login' });

    interceptError(
      new HttpErrorResponse({
        status: 401,
        error: { message: 'Credenciais invalidas.' },
      }),
    );

    expect(authService.logout).toHaveBeenCalled();
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('normaliza erros 4xx com objeto de erro sem message preservando os demais campos', () => {
    const result = interceptError(
      new HttpErrorResponse({
        status: 422,
        error: {
          erro: 'CPF invalido.',
          campo: 'cpf',
        },
      }),
    ) as HttpErrorResponse;

    expect(result.status).toBe(422);
    expect(result.error.message).toBe('CPF invalido.');
    expect(result.error.campo).toBe('cpf');
  });

  it('usa uma mensagem padrao para erros 4xx sem mensagem do servidor', () => {
    const result = interceptError(
      new HttpErrorResponse({
        status: 404,
        error: {},
      }),
    ) as HttpErrorResponse;

    expect(result.error.message).toBe('Recurso não encontrado.');
  });

  it('normaliza erros 5xx em uma mensagem generica', () => {
    const result = interceptError(
      new HttpErrorResponse({
        status: 500,
        error: { traceId: 'abc' },
      }),
    ) as HttpErrorResponse;

    expect(result.error.message).toBe('Erro interno no servidor. Tente novamente em instantes.');
  });

  it('propaga erros nao HTTP sem alteracao', () => {
    const rawError = new Error('falha inesperada');

    const result = interceptError(rawError);

    expect(result).toBe(rawError);
  });
});
