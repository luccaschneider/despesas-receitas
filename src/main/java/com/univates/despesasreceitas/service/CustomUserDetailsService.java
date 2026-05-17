package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.entity.Usuario;
import com.univates.despesasreceitas.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String loginOuEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLoginOrEmail(loginOuEmail.trim())
            .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + loginOuEmail));

        if (usuario.getSituacao() != Usuario.Situacao.ATIVO) {
            throw new UsernameNotFoundException("Usuário inativo: " + loginOuEmail);
        }

        String role = (usuario.getPerfil() == Usuario.Perfil.ADMIN) ? "ADMIN" : "USER";

        return User.builder()
            .username(usuario.getLogin())
            .password("{noop}" + usuario.getSenha())
            .roles(role)
            .build();
    }
}
