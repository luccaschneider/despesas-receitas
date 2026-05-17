package com.univates.despesasreceitas.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "tipo_lancamento", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoLancamento tipoLancamento;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Situacao situacao;

    public enum TipoLancamento { RECEITA, DESPESA }

    public enum Situacao { PENDENTE, PAGO, CANCELADO }

    public Lancamento() {
    }

    public Lancamento(Long id, String descricao, LocalDate dataLancamento, BigDecimal valor,
                      TipoLancamento tipoLancamento, Situacao situacao) {
        this.id = id;
        this.descricao = descricao;
        this.dataLancamento = dataLancamento;
        this.valor = valor;
        this.tipoLancamento = tipoLancamento;
        this.situacao = situacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataLancamento() {
        return dataLancamento;
    }

    public void setDataLancamento(LocalDate dataLancamento) {
        this.dataLancamento = dataLancamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public TipoLancamento getTipoLancamento() {
        return tipoLancamento;
    }

    public void setTipoLancamento(TipoLancamento tipoLancamento) {
        this.tipoLancamento = tipoLancamento;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }
}
