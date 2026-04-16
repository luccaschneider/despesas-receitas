package com.univates.despesasreceitas.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
