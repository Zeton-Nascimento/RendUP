import { TestBed } from '@angular/core/testing';

import { AuthState } from './auth.state';

describe('AuthState', () => {
  let state: AuthState;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    state = TestBed.inject(AuthState);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('deve iniciar sem token quando localStorage esta vazio', () => {
    expect(state.accessToken()).toBeNull();
    expect(state.autenticado()).toBe(false);
  });

  it('deve definir token e persistir no localStorage', () => {
    state.setAccessToken('meu-token-123');
    expect(state.accessToken()).toBe('meu-token-123');
    expect(localStorage.getItem('access_token')).toBe('meu-token-123');
    expect(state.autenticado()).toBe(true);
  });

  it('deve remover token ao receber null', () => {
    state.setAccessToken('token');
    state.setAccessToken(null);
    expect(state.accessToken()).toBeNull();
    expect(localStorage.getItem('access_token')).toBeNull();
    expect(state.autenticado()).toBe(false);
  });

  it('deve definir e ler usuario', () => {
    const usuario = { id: '1', nome: 'Luiz', email: 'luiz@email.com', permissoes: ['ROLE_INVESTIDOR'] };
    state.setUsuario(usuario);
    expect(state.usuario()).toEqual(usuario);
  });

  it('deve definir usuario como null', () => {
    state.setUsuario({ id: '1', nome: 'X', email: 'x@x.com', permissoes: [] });
    state.setUsuario(null);
    expect(state.usuario()).toBeNull();
  });

  it('deve controlar loading', () => {
    expect(state.loading()).toBe(false);
    state.setLoading(true);
    expect(state.loading()).toBe(true);
    state.setLoading(false);
    expect(state.loading()).toBe(false);
  });

  it('deve fazer logout limpando token e usuario', () => {
    state.setAccessToken('tok');
    state.setUsuario({ id: '1', nome: 'A', email: 'a@a.com', permissoes: [] });
    state.logout();
    expect(state.accessToken()).toBeNull();
    expect(state.usuario()).toBeNull();
    expect(state.autenticado()).toBe(false);
  });

  it('isAuthenticated deve ser alias de autenticado', () => {
    state.setAccessToken('tok');
    expect(state.isAuthenticated()).toBe(state.autenticado());
  });
});
