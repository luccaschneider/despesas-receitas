package com.univates.despesasreceitas.repository;

import com.univates.despesasreceitas.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByLoginAndSenha(String login, String senha);

    @Query("SELECT u FROM Usuario u WHERE u.login = :id OR LOWER(u.email) = LOWER(:id)")
    Optional<Usuario> findByLoginOrEmail(@Param("id") String loginOuEmail);
}
