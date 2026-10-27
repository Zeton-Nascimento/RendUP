import { signal } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { Router, UrlTree } from "@angular/router";
import { vi } from "vitest";

import { AuthService } from "./auth.service";
import { adminGuard } from "./admin.guard";
import { AuthState } from "../state/auth.state";

describe("adminGuard", () => {
  it("permite acesso para usuário administrador", () => {
    const usuario = signal({ permissoes: ["ROLE_ADMIN"] });
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { getToken: vi.fn(), hasRole: vi.fn().mockReturnValue(true) } },
        { provide: AuthState, useValue: { usuario: usuario.asReadonly() } },
        { provide: Router, useValue: { createUrlTree: vi.fn() } },
      ],
    });

    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).toBe(true);
  });

  it("redireciona usuário sem permissão administrativa", () => {
    const usuario = signal({ permissoes: ["ROLE_INVESTIDOR"] });
    const urlTree = {} as UrlTree;
    const createUrlTree = vi.fn().mockReturnValue(urlTree);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { getToken: vi.fn(), hasRole: vi.fn().mockReturnValue(false) } },
        { provide: AuthState, useValue: { usuario: usuario.asReadonly() } },
        { provide: Router, useValue: { createUrlTree } },
      ],
    });

    const resultado = TestBed.runInInjectionContext(() => adminGuard({} as never, {} as never));

    expect(resultado).toBe(urlTree);
    expect(createUrlTree).toHaveBeenCalledWith(["/perfil"]);
  });
});
