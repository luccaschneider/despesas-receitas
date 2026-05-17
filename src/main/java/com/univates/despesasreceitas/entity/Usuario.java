package com.univates.despesasreceitas.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Situacao situacao;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Perfil perfil = Perfil.USER;

    public enum Situacao { ATIVO, INATIVO }

    public enum Perfil { ADMIN, USER }

    public Usuario() {
    }

    public Usuario(Long id, String nome, String login, String email, String senha, Situacao situacao, Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.email = email;
        this.senha = senha;
        this.situacao = situacao;
        this.perfil = perfil != null ? perfil : Perfil.USER;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil != null ? perfil : Perfil.USER;
    }
}
