import { signal } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { of, throwError } from "rxjs";
import { vi } from "vitest";

import { AuthState } from "../../core/state/auth.state";
import type { AuthUsuario } from "../../models/auth.models";
import { UsuarioService } from "../../services/usuario.service";
import { PerfilComponent } from "./perfil.component";

describe("PerfilComponent", () => {
  let fixture: ComponentFixture<PerfilComponent>;
  let component: PerfilComponent;

  const usuarioSignal = signal<AuthUsuario | null>(null);
  const usuarioService = {
    obterMe: vi.fn(),
    atualizarMe: vi.fn(),
    alterarSenha: vi.fn(),
  };
  const authStateMock = {
    usuario: usuarioSignal.asReadonly(),
    setUsuario: vi.fn((usuario: AuthUsuario | null) => usuarioSignal.set(usuario)),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    usuarioSignal.set(null);
    usuarioService.obterMe.mockReturnValue(of(criarUsuario()));
    usuarioService.atualizarMe.mockReset();
    usuarioService.alterarSenha.mockReset();

    await TestBed.configureTestingModule({
      imports: [PerfilComponent],
      providers: [
        { provide: UsuarioService, useValue: usuarioService },
        { provide: AuthState, useValue: authStateMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PerfilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("deve consultar e exibir o próprio perfil", () => {
    const texto = (fixture.nativeElement as HTMLElement).textContent ?? "";

    expect(usuarioService.obterMe).toHaveBeenCalled();
    expect(texto).toContain("Gabriel Ortiz");
    expect(texto).toContain("Dados pessoais");
    expect(texto).toContain("***.***.789-01");
  });

  it("deve enviar somente os dados permitidos na atualização", () => {
    usuarioService.atualizarMe.mockReturnValue(of({ ...criarUsuario(), nome: "Gabriel Atualizado" }));

    component.abrirEdicao();
    component.form.patchValue({
      nome: " Gabriel Atualizado ",
      telefone: "(11) 99999-8888",
      dataNascimento: "1997-01-02",
      perfilInvestidor: "ARROJADO",
    });
    component.salvar();

    expect(usuarioService.atualizarMe).toHaveBeenCalledWith({
      nome: "Gabriel Atualizado",
      telefone: "11999998888",
      dataNascimento: "1997-01-02",
      perfilInvestidor: "ARROJADO",
    });
    expect(component.editando()).toBe(false);
    expect(component.sucessoAtualizacao()).toBe("Dados atualizados com sucesso.");
  });

  it("deve exibir erro de consulta e permitir tentar novamente", () => {
    usuarioService.obterMe.mockReturnValueOnce(
      throwError(() => new Error("falha de conexão")),
    );

    component.recarregar();
    fixture.detectChanges();

    expect(component.carregando()).toBe(false);
    expect(component.erro()).toBe("Não foi possível carregar seu perfil.");
    expect(fixture.nativeElement.textContent).toContain("Tentar novamente");

    usuarioService.obterMe.mockReturnValueOnce(of(criarUsuario()));
    component.recarregar();
    fixture.detectChanges();

    expect(component.erro()).toBeNull();
    expect(component.usuarioPerfil()?.email).toBe("gabriel@email.com");
  });

  it("deve exibir estado vazio quando a consulta não retorna perfil", () => {
    usuarioService.obterMe.mockReturnValueOnce(of(null));

    component.recarregar();
    fixture.detectChanges();

    expect(component.usuarioPerfil()).toBeNull();
    expect(fixture.nativeElement.textContent).toContain("Nenhum dado de perfil encontrado.");
  });

  function criarUsuario(): AuthUsuario {
    return {
      id: "user-1",
      nome: "Gabriel Ortiz",
      email: "gabriel@email.com",
      perfilInvestidor: "MODERADO",
      cpf: "12345678901",
      telefone: "11988887777",
      dataNascimento: "1996-09-12",
      permissoes: ["ROLE_INVESTIDOR"],
    };
  }
});
