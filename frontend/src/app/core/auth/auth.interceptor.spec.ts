import { HttpRequest, HttpResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  const authService = {
    getToken: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }],
    });
  });

  it('anexa o header Authorization quando existe token', () => {
    authService.getToken.mockReturnValue('abc123');
    const next = vi.fn((request: HttpRequest<unknown>) => of(new HttpResponse({ body: request.url })));
    const req = new HttpRequest('GET', '/api/protegida');

    TestBed.runInInjectionContext(() => authInterceptor(req, next)).subscribe();

    const forwarded = next.mock.calls[0][0] as HttpRequest<unknown>;
    expect(forwarded.headers.get('Authorization')).toBe('Bearer abc123');
  });

  it('mantem a requisicao original quando nao existe token', () => {
    authService.getToken.mockReturnValue(null);
    const next = vi.fn((request: HttpRequest<unknown>) => of(new HttpResponse({ body: request.url })));
    const req = new HttpRequest('GET', '/api/publica');

    TestBed.runInInjectionContext(() => authInterceptor(req, next)).subscribe();

    const forwarded = next.mock.calls[0][0] as HttpRequest<unknown>;
    expect(forwarded).toBe(req);
    expect(forwarded.headers.has('Authorization')).toBe(false);
  });
});
