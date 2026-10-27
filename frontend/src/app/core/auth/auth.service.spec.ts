import { signal } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import type { AuthUsuario, RegistroRequest } from '../../models/auth.models';
import { AuthService } from './auth.service';
import { AuthState } from '../state/auth.state';
import { vi } from 'vitest';

function makeJwt(payload: Record<string, unknown>): string {
  const json = JSON.stringify(payload);
  const base64 = btoa(json).replaceAll('+', '-').replaceAll('/', '_').replaceAll('=', '');
  return `header.${base64}.signature`;
}

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let accessTokenSignal: ReturnType<typeof signal<string | null>>;
  let usuarioSignal: ReturnType<typeof signal<AuthUsuario | null>>;
  let loadingSignal: ReturnType<typeof signal<boolean>>;
  let authStateMock: {
    accessToken: ReturnType<typeof accessTokenSignal.asReadonly>;
    usuario: ReturnType<typeof usuarioSignal.asReadonly>;
    loading: ReturnType<typeof loadingSignal.asReadonly>;
    setAccessToken: ReturnType<typeof vi.fn>;
    setUsuario: ReturnType<typeof vi.fn>;
    setLoading: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    accessTokenSignal = signal<string | null>(null);
    usuarioSignal = signal<AuthUsuario | null>(null);
    loadingSignal = signal(false);

    authStateMock = {
      accessToken: accessTokenSignal.asReadonly(),
      usuario: usuarioSignal.asReadonly(),
      loading: loadingSignal.asReadonly(),
      setAccessToken: vi.fn((token: string | null) => accessTokenSignal.set(token)),
      setUsuario: vi.fn((usuario: AuthUsuario | null) => usuarioSignal.set(usuario)),
      setLoading: vi.fn((loading: boolean) => loadingSignal.set(loading)),
      logout: vi.fn(() => {
        accessTokenSignal.set(null);
        usuarioSignal.set(null);
      }),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthState, useValue: authStateMock },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('hidrata o usuario a partir do token ao iniciar o servico', () => {
    accessTokenSignal.set(
      makeJwt({
        name: 'Maria Teste',
        sub: 'maria@email.com',
        perfil: 'ARROJADO',
        roles: 'ROLE_ADMIN',
      }),
    );

    service = TestBed.inject(AuthService);

    expect(service.getToken()).not.toBeNull();
    expect(usuarioSignal()).toEqual({
      nome: 'Maria Teste',
      email: 'maria@email.com',
      perfilInvestidor: 'ARROJADO',
      permissoes: ['ROLE_ADMIN'],
    });
  });

  it('identifica expiracao do token a partir do claim exp', () => {
    service = TestBed.inject(AuthService);
    const expiredToken = makeJwt({ exp: Math.floor(Date.now() / 1000) - 60 });
    const validToken = makeJwt({ exp: Math.floor(Date.now() / 1000) + 60 });
    const tokenWithoutExp = makeJwt({ sub: 'sem-exp@email.com' });

    expect(service.isTokenExpired(expiredToken)).toBe(true);
    expect(service.isTokenExpired(validToken)).toBe(false);
    expect(service.isTokenExpired(tokenWithoutExp)).toBe(false);
  });

  it('faz login, salva o token e hidrata o usuario a partir do JWT quando o backend nao envia usuario', () => {
    service = TestBed.inject(AuthService);
    const token = makeJwt({
      nome: 'Carlos Login',
      email: 'carlos@email.com',
      perfilInvestidor: 'MODERADO',
      permissions: ['ROLE_INVESTIDOR'],
    });

    let resposta: unknown;
    service.login({ email: 'carlos@email.com', senha: '123456' }).subscribe((value) => {
      resposta = value;
    });

    expect(loadingSignal()).toBe(true);

    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'carlos@email.com', senha: '123456' });

    req.flush({ token });

    expect(resposta).toEqual({ token });
    expect(accessTokenSignal()).toBe(token);
    expect(usuarioSignal()).toEqual({
      nome: 'Carlos Login',
      email: 'carlos@email.com',
      perfilInvestidor: 'MODERADO',
      permissoes: ['ROLE_INVESTIDOR'],
    });
    expect(loadingSignal()).toBe(false);
  });

  it('encerra o loading e propaga erro quando a resposta de login nao traz token', () => {
    service = TestBed.inject(AuthService);
    let capturedError: unknown;

    service.login({ email: 'sem-token@email.com', senha: '123456' }).subscribe({
      error: (error) => {
        capturedError = error;
      },
    });

    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/login');
    req.flush({ usuario: null });

    expect(capturedError).toBeInstanceOf(Error);
    expect((capturedError as Error).message).toBe('Resposta de login sem token.');
    expect(loadingSignal()).toBe(false);
  });

  it('registra o usuario e atualiza o estado', () => {
    service = TestBed.inject(AuthService);
    const request: RegistroRequest = {
      nome: 'Novo Usuario',
      email: 'novo@email.com',
      senha: 'senha123',
      cpf: '12345678901',
      telefone: '11999999999',
      dataNascimento: '1990-01-01',
      banco: 'Banco',
      agencia: '0001',
      numeroConta: '12345',
      perfilInvestidor: 'CONSERVADOR',
    };

    service.registro(request).subscribe();

    expect(loadingSignal()).toBe(true);

    const req = httpMock.expectOne('http://localhost:8080/api/v1/auth/registrar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);

    req.flush({
      id: 'user-1',
      ...request,
      permissoes: ['ROLE_INVESTIDOR'],
    });

    expect(usuarioSignal()?.email).toBe('novo@email.com');
    expect(loadingSignal()).toBe(false);
  });

  it('delegates logout para o estado de autenticacao', () => {
    service = TestBed.inject(AuthService);

    service.logout();

    expect(authStateMock.logout).toHaveBeenCalled();
  });
});
