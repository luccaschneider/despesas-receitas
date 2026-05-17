package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.entity.Usuario;
import com.univates.despesasreceitas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Usuario> autenticar(String login, String senha) {
        return usuarioRepository.findByLoginAndSenha(login, senha);
    }

    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    /**
     * Registra novo usuário. Lança {@link IllegalStateException} se login ou e-mail já existirem.
     */
    public Usuario registrar(String nome, String login, String email, String senha) {
        if (nome == null || nome.isBlank()
            || login == null || login.isBlank()
            || email == null || email.isBlank()
            || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("campos_obrigatorios");
        }
        if (usuarioRepository.findByLogin(login).isPresent()) {
            throw new IllegalStateException("login_duplicado");
        }
        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalStateException("email_duplicado");
        }

        Usuario u = new Usuario();
        u.setNome(nome);
        u.setLogin(login);
        u.setEmail(email);
        u.setSenha(senha);
        u.setSituacao(Usuario.Situacao.ATIVO);
        u.setPerfil(Usuario.Perfil.USER);
        return usuarioRepository.save(u);
    }
}
