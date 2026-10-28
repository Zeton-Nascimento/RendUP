import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { AuthService } from '../../../core/auth/auth.service';
import { AuthState } from '../../../core/state/auth.state';
import { RegistroComponent } from './registro.component';

describe('RegistroComponent', () => {
  let fixture: ComponentFixture<RegistroComponent>;
  let component: RegistroComponent;
  let router: Router;
  let navigateByUrlSpy: any;

  const authService = {
    registro: vi.fn(),
  };
  const loading = signal(false);

  const authStateMock = {
    loading: loading.asReadonly(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    authService.registro.mockReset();
    authService.registro.mockReturnValue(
      of({
        id: 'novo-usuario',
        nome: 'Gabriel Ortiz',
        email: 'gabriel@email.com',
        perfilInvestidor: 'MODERADO',
        cpf: '12345678901',
        telefone: '11988887777',
        dataNascimento: '1990-01-10',
        banco: 'Banco do Brasil',
        agencia: '1234',
        numeroConta: '1234567',
        permissoes: ['ROLE_INVESTIDOR'],
      }),
    );

    await TestBed.configureTestingModule({
      imports: [RegistroComponent],
      providers: [
        { provide: AuthService, useValue: authService as unknown as AuthService },
        { provide: AuthState, useValue: authStateMock },
        provideRouter([]),
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    navigateByUrlSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);

    fixture = TestBed.createComponent(RegistroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve enviar payload completo no cadastro com campos normalizados', () => {
    component.form.setValue({
      nome: ' Gabriel Ortiz ',
      email: ' gabriel@email.com ',
      senha: 'senha1234',
      cpf: '123.456.789-01',
      telefone: '(11) 98888-7777',
      dataNascimento: '1990-01-10',
      banco: ' Banco do Brasil ',
      agencia: '12-34',
      numeroConta: '00.123-45',
      perfilInvestidor: 'MODERADO',
    });

    component.criarConta();

    expect(authService.registro).toHaveBeenCalledWith({
      nome: 'Gabriel Ortiz',
      email: 'gabriel@email.com',
      senha: 'senha1234',
      cpf: '12345678901',
      telefone: '11988887777',
      dataNascimento: '1990-01-10',
      banco: 'Banco do Brasil',
      agencia: '1234',
      numeroConta: '0012345',
      perfilInvestidor: 'MODERADO',
    });
    expect(navigateByUrlSpy).not.toHaveBeenCalled();
    expect(component.cadastroConcluido()).toBe(true);
    expect(component.sucesso()).toContain('Conta criada');

    component.irParaLogin();
    expect(navigateByUrlSpy).toHaveBeenCalledWith('/login');
  });

  it('deve bloquear envio quando cpf for inválido', () => {
    component.form.setValue({
      nome: 'Gabriel Ortiz',
      email: 'gabriel@email.com',
      senha: 'senha1234',
      cpf: '123',
      telefone: '11988887777',
      dataNascimento: '1990-01-10',
      banco: 'Banco do Brasil',
      agencia: '1234',
      numeroConta: '1234567',
      perfilInvestidor: 'MODERADO',
    });

    component.criarConta();
    fixture.detectChanges();

    expect(authService.registro).not.toHaveBeenCalled();
    const mensagemCpf = component.mensagemErro('cpf') ?? '';
    const mensagemSemAcento = mensagemCpf.normalize('NFD').replaceAll(/[\u0300-\u036f]/g, '');
    expect(mensagemSemAcento).toContain('CPF deve ter 11 digitos numericos.');
  });

  it('deve exibir mensagem de validacao ao tentar criar conta com dados invalidos', () => {
    component.form.setValue({
      nome: '',
      email: 'email-invalido',
      senha: 'abcdefgh',
      cpf: '123',
      telefone: '1',
      dataNascimento: '',
      banco: '',
      agencia: '12',
      numeroConta: '1',
      perfilInvestidor: 'MODERADO',
    });
    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector(
      '#registro-submit',
    ) as HTMLButtonElement;
    expect(submitButton.disabled).toBe(false);

    submitButton.click();
    fixture.detectChanges();

    const mensagens = Array.from(
      fixture.nativeElement.querySelectorAll('.auth-field-error'),
      (element: Element) => element.textContent?.trim(),
    );

    expect(authService.registro).not.toHaveBeenCalled();
    expect(mensagens).toContain('Campo obrigatório.');
    expect(mensagens).toContain('Informe um e-mail válido.');
    expect(mensagens).toContain('Senha precisa ter 8+ caracteres, com letra e número.');
  });

  it('deve exibir instrução de senha antes do erro de validacao', () => {
    const fixtureSenha = TestBed.createComponent(RegistroComponent);
    const componentSenha = fixtureSenha.componentInstance;
    componentSenha.form.controls.senha.setValue('');
    fixtureSenha.detectChanges();

    const ajudaSenha = fixtureSenha.nativeElement.querySelector(
      '.auth-field-hint',
    ) as HTMLElement | null;

    expect(ajudaSenha?.textContent?.trim()).toBe(
      'Use pelo menos 8 caracteres, com ao menos uma letra e um número.',
    );
  });
});
