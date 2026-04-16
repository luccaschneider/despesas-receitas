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
}
