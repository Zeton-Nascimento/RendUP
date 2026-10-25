package com.example.templatespring.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.templatespring.dto.login.request.LoginRequest;
import com.example.templatespring.dto.usuario.request.RegistroRequest;
import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.repository.CargoRepository;
import com.example.templatespring.repository.UsuarioRepository;
import com.example.templatespring.validator.ValidarEmailUnicoValidator;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ValidarEmailUnicoValidator validarEmailUnicoValidator;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrarDeveCriarUsuarioAtivoComCargoInvestidor() {
        RegistroRequest request = new RegistroRequest();
        request.setNome("Usuário Novo");
        request.setEmail(" novo@email.com ");
        request.setSenha("senha123");
        request.setCpf("12345678901");
        request.setTelefone("11999998888");
        request.setDataNascimento(LocalDate.of(1995, 5, 17));
        request.setBanco("Banco Teste");
        request.setAgencia("1234");
        request.setNumeroConta("987654");
        request.setPerfilInvestidor(PerfilInvestidor.MODERADO);

        Cargo cargoInvestidor = Cargo.builder().nome("INVESTIDOR").build();
        when(cargoRepository.findByNome("INVESTIDOR")).thenReturn(Optional.of(cargoInvestidor));
        when(passwordEncoder.encode("senha123")).thenReturn("senha-hash");

        UsuarioResponse response = authService.registrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario usuario = captor.getValue();
        assertEquals("novo@email.com", usuario.getEmail());
        assertEquals("senha-hash", usuario.getSenha());
        assertEquals(true, usuario.isAtivo());
        assertEquals(PerfilInvestidor.MODERADO, usuario.getPerfilInvestidor());
        assertEquals(cargoInvestidor, usuario.getCargos().iterator().next());
        assertEquals("novo@email.com", response.getEmail());
    }

    @Test
    void loginDeveRecusarUsuarioInativo() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inativo@email.com");
        request.setSenha("senha123");

        Usuario usuario = new Usuario();
        usuario.setAtivo(false);
        when(usuarioRepository.findByEmailComCargosEPermissoes(request.getEmail()))
                .thenReturn(Optional.of(usuario));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
