import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { UsuarioService } from './usuario.service';
import { environment } from '../../environments/environment';

describe('UsuarioService', () => {
  let service: UsuarioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obterMe deve fazer GET /v1/usuarios/me', () => {
    service.obterMe().subscribe((u) => expect(u.nome).toBe('Gabriel'));
    http
      .expectOne(`${environment.apiUrl}/v1/usuarios/me`)
      .flush({ id: 'u1', nome: 'Gabriel', email: 'g@email.com', permissoes: ['ROLE_INVESTIDOR'] });
  });

  it('atualizarMe deve fazer PUT /v1/usuarios/me', () => {
    const request = {
      nome: 'Gabriel Atualizado',
      telefone: '11999998888',
      dataNascimento: '1996-09-12',
      perfilInvestidor: 'MODERADO' as const,
    };

    service.atualizarMe(request).subscribe((u) => expect(u.nome).toBe('Gabriel Atualizado'));

    const req = http.expectOne(`${environment.apiUrl}/v1/usuarios/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush({
      id: 'u1',
      ...request,
      email: 'g@email.com',
      cpf: '12345678901',
      permissoes: ['ROLE_INVESTIDOR'],
    });
  });
});
