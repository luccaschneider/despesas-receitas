package com.univates.despesasreceitas.repository;

import com.univates.despesasreceitas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLogin(String login);
    Optional<Usuario> findByLoginAndSenha(String login, String senha);
}
