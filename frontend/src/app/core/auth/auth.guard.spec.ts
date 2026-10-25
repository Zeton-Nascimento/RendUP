import { UrlTree } from '@angular/router';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import { AuthService } from './auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  const authService = {
    getToken: vi.fn(),
    isTokenExpired: vi.fn(),
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

  it('redireciona para login quando nao existe token', () => {
    authService.getToken.mockReturnValue(null);
    const router = TestBed.inject(Router);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBeInstanceOf(UrlTree);
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });

  it('faz logout e redireciona quando o token expirou', () => {
    authService.getToken.mockReturnValue('token-expirado');
    authService.isTokenExpired.mockReturnValue(true);
    const router = TestBed.inject(Router);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(authService.logout).toHaveBeenCalled();
    expect(router.serializeUrl(result as UrlTree)).toBe('/login');
  });

  it('libera a navegacao quando o token eh valido', () => {
    authService.getToken.mockReturnValue('token-valido');
    authService.isTokenExpired.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));

    expect(result).toBe(true);
  });
});
