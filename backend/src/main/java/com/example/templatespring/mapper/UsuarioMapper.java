package com.example.templatespring.mapper;


import com.example.templatespring.dto.usuario.request.RegistroRequest;
import com.example.templatespring.dto.usuario.response.UsuarioResponse;
import com.example.templatespring.entity.security.Permissao;
import com.example.templatespring.entity.security.UsuarioConta;
import com.example.templatespring.entity.security.UsuarioDadosPessoais;
import com.example.templatespring.entity.security.Usuario;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UsuarioMapper {

    public static Usuario toEntity(RegistroRequest request) {
        Usuario entity = new Usuario();
        entity.setNome(request.getNome().trim());
        entity.setEmail(request.getEmail().trim());
        if (request.getPerfilInvestidor() != null) entity.setPerfilInvestidor(request.getPerfilInvestidor());

        UsuarioDadosPessoais dadosPessoais = new UsuarioDadosPessoais();
        dadosPessoais.setCpf(normalizarSomenteDigitos(request.getCpf()));
        dadosPessoais.setTelefone(normalizarSomenteDigitos(request.getTelefone()));
        dadosPessoais.setDataNascimento(request.getDataNascimento());
        entity.setDadosPessoais(dadosPessoais);

        UsuarioConta conta = new UsuarioConta();
        conta.setBanco(request.getBanco().trim());
        conta.setAgencia(normalizarSomenteDigitos(request.getAgencia()));
        conta.setNumeroConta(normalizarSomenteDigitos(request.getNumeroConta()));
        entity.setConta(conta);

        return entity;
    }

    public static UsuarioResponse toResponse(Usuario entity) {
        UsuarioDadosPessoais dadosPessoais = entity.getDadosPessoais();
        UsuarioConta conta = entity.getConta();

        return UsuarioResponse.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .perfilInvestidor(entity.getPerfilInvestidor() == null ? null : entity.getPerfilInvestidor().toString())
                .cpf(dadosPessoais == null ? null : dadosPessoais.getCpf())
                .telefone(dadosPessoais == null ? null : dadosPessoais.getTelefone())
                .dataNascimento(dadosPessoais == null ? null : dadosPessoais.getDataNascimento())
                .banco(conta == null ? null : conta.getBanco())
                .agencia(conta == null ? null : conta.getAgencia())
                .numeroConta(conta == null ? null : conta.getNumeroConta())
                .permissoes(entity.getPermissoes().stream().map(Permissao::getNome).toList())
                .build();
    }

    private static String normalizarSomenteDigitos(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}
