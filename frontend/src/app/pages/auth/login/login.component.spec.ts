import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { AuthService } from '../../../core/auth/auth.service';
import { AuthState } from '../../../core/state/auth.state';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let router: Router;

  const authService = {
    login: vi.fn(),
    getToken: vi.fn().mockReturnValue(null),
    isTokenExpired: vi.fn().mockReturnValue(false),
  };
  const loading = signal(false);
  const authStateMock = { loading: loading.asReadonly() };

  beforeEach(async () => {
    vi.clearAllMocks();
    loading.set(false);
    authService.login.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: AuthState, useValue: authStateMock },
        provideRouter([]),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve navegar para /perfil apos login bem-sucedido', () => {
    component.form.setValue({ email: 'user@email.com', senha: 'senha123' });
    component.entrar();
    expect(authService.login).toHaveBeenCalledWith({ email: 'user@email.com', senha: 'senha123' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/perfil');
  });

  it('deve exibir erro quando login falha', () => {
    authService.login.mockReturnValue(
      throwError(() => ({ error: { message: 'Credenciais inválidas' } })),
    );
    component.form.setValue({ email: 'user@email.com', senha: 'senha123' });
    component.entrar();
    expect(component.error()).toBe('Credenciais inválidas');
  });

  it('deve exibir mensagem padrao quando erro sem mensagem especifica', () => {
    authService.login.mockReturnValue(throwError(() => ({})));
    component.form.setValue({ email: 'user@email.com', senha: 'senha123' });
    component.entrar();
    expect(component.error()).toBe('Email ou senha inválidos.');
  });

  it('nao deve chamar login com formulario invalido', () => {
    component.form.setValue({ email: '', senha: '' });
    component.entrar();
    expect(component.form.invalid).toBe(true);
    expect(component.form.controls.email.touched).toBe(true);
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('nao deve chamar login com email invalido', () => {
    component.form.setValue({ email: 'nao-e-email', senha: 'senha123' });
    component.entrar();
    expect(component.form.controls.email.invalid).toBe(true);
    expect(component.form.controls.email.errors?.['email']).toBe(true);
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('deve fazer trim no email antes de validar', () => {
    component.form.setValue({ email: '  user@email.com  ', senha: 'senha123' });
    component.entrar();
    expect(authService.login).toHaveBeenCalledWith({ email: 'user@email.com', senha: 'senha123' });
  });

  it('nao deve chamar login quando loading=true', () => {
    loading.set(true);
    component.form.setValue({ email: 'user@email.com', senha: 'senha123' });
    component.entrar();
    expect(component.loading()).toBe(true);
    expect(router.navigateByUrl).not.toHaveBeenCalled();
    expect(authService.login).not.toHaveBeenCalled();
  });

  it('deve limpar erro ao alterar formulario', async () => {
    authService.login.mockReturnValue(throwError(() => ({})));
    component.form.setValue({ email: 'user@email.com', senha: 'senha123' });
    component.entrar();
    expect(component.error()).not.toBeNull();

    component.form.controls.email.setValue('outro@email.com');
    await fixture.whenStable();
    expect(component.error()).toBeNull();
  });
});
