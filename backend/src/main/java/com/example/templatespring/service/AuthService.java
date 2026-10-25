package com.example.templatespring.service;

import com.example.templatespring.dto.login.request.LoginRequest;
import com.example.templatespring.dto.login.response.TokenResponse;
import com.example.templatespring.dto.usuario.request.RegistroRequest;
import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.enums.PerfilInvestidor;
import com.example.templatespring.entity.security.Cargo;
import com.example.templatespring.entity.security.Usuario;
import com.example.templatespring.mapper.UsuarioMapper;
import com.example.templatespring.repository.CargoRepository;
import com.example.templatespring.repository.UsuarioRepository;
import com.example.templatespring.validator.ValidarEmailUnicoValidator;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class AuthService {

    private static final String CARGO_INVESTIDOR = "INVESTIDOR";
    private static final String TIPO_TOKEN_BEARER = "Bearer";
    private static final BigDecimal SALDO_INICIAL = BigDecimal.ZERO;

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidarEmailUnicoValidator validarEmailUnicoValidator;
    private final TokenService tokenService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            CargoRepository cargoRepository,
            PasswordEncoder passwordEncoder,
            ValidarEmailUnicoValidator validarEmailUnicoValidator,
            TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.passwordEncoder = passwordEncoder;
        this.validarEmailUnicoValidator = validarEmailUnicoValidator;
        this.tokenService = tokenService;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        request.setEmail(request.getEmail().trim());
        validarEmailUnicoValidator.validar(request.getEmail());

        Usuario usuario = UsuarioMapper.toEntity(request);
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        usuario.setAtivo(true);
        usuario.setSaldoDisponivel(SALDO_INICIAL);
        usuario.setPerfilInvestidor(
                request.getPerfilInvestidor() == null ? PerfilInvestidor.CONSERVADOR : request.getPerfilInvestidor());

        Cargo cargoInvestidor = cargoRepository.findByNome(CARGO_INVESTIDOR)
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Cargo INVESTIDOR não encontrado."));
        usuario.adicionarCargo(cargoInvestidor);

        usuarioRepository.save(usuario);
        return UsuarioMapper.toResponse(usuario);
    }

    public TokenResponse login(LoginRequest request) {
        Optional<Usuario> optUser = usuarioRepository.findByEmailComCargosEPermissoes(request.getEmail());

        if (optUser.isEmpty()
                || !optUser.get().isAtivo()
                || !passwordEncoder.matches(request.getSenha(), optUser.get().getSenha())) {
            throw new BadCredentialsException("Usuário ou senha incorretos!");
        }

        Usuario usuario = optUser.get();
        String token = tokenService.gerarToken(usuario);
        return new TokenResponse(
                token,
                TIPO_TOKEN_BEARER,
                UsuarioMapper.toResponse(usuario),
                tokenService.getExpiresInSeconds());
    }
}
