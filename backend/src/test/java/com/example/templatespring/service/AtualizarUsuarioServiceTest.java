package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.templatespring.dto.usuario.request.AtualizarUsuarioRequest;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.entity.security.UsuarioConta;
import com.example.templatespring.entity.security.UsuarioDadosPessoais;
import com.example.templatespring.exception.RecursoNaoEncontradoException;
import com.example.templatespring.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AtualizarUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private AtualizarUsuarioService atualizarUsuarioService;

    @Test
    void deveAtualizarSomenteOsDadosPermitidosDoProprioUsuario() {
        Usuario usuario = criarUsuario();
        AtualizarUsuarioRequest request = criarRequest();
        when(usuarioAutenticadoService.get()).thenReturn(usuario);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        atualizarUsuarioService.atualizarProprioPerfil(request);

        assertEquals("Nome Atualizado", usuario.getNome());
        assertEquals(PerfilInvestidor.MODERADO, usuario.getPerfilInvestidor());
        assertEquals("11999998888", usuario.getDadosPessoais().getTelefone());
        assertEquals(LocalDate.of(1998, 2, 3), usuario.getDadosPessoais().getDataNascimento());

        assertEquals("usuario@email.com", usuario.getEmail());
        assertEquals("hash-original", usuario.getSenha());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(usuario.getSaldoDisponivel()));
        assertEquals("Banco original", usuario.getConta().getBanco());
        assertEquals("1234", usuario.getConta().getAgencia());
        assertEquals("987654", usuario.getConta().getNumeroConta());
        assertEquals("12345678901", usuario.getDadosPessoais().getCpf());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void administradorDeveAtualizarUsuarioExistente() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = criarUsuario();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        atualizarUsuarioService.atualizarPorAdministrador(usuarioId, criarRequest());

        assertEquals("Nome Atualizado", usuario.getNome());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void administradorNaoDeveAtualizarUsuarioInexistente() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> atualizarUsuarioService.atualizarPorAdministrador(usuarioId, criarRequest()));
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Nome Original");
        usuario.setEmail("usuario@email.com");
        usuario.setSenha("hash-original");
        usuario.setSaldoDisponivel(BigDecimal.valueOf(500));
        usuario.setPerfilInvestidor(PerfilInvestidor.CONSERVADOR);

        UsuarioDadosPessoais dados = new UsuarioDadosPessoais();
        dados.setCpf("12345678901");
        dados.setTelefone("11988887777");
        dados.setDataNascimento(LocalDate.of(1990, 1, 1));
        usuario.setDadosPessoais(dados);

        UsuarioConta conta = new UsuarioConta();
        conta.setBanco("Banco original");
        conta.setAgencia("1234");
        conta.setNumeroConta("987654");
        usuario.setConta(conta);
        return usuario;
    }

    private AtualizarUsuarioRequest criarRequest() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest();
        request.setNome(" Nome Atualizado ");
        request.setTelefone("(11) 99999-8888");
        request.setDataNascimento(LocalDate.of(1998, 2, 3));
        request.setPerfilInvestidor(PerfilInvestidor.MODERADO);
        return request;
    }
}
