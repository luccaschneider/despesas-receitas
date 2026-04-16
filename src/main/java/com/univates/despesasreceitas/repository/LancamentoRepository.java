package com.univates.despesasreceitas.repository;

import com.univates.despesasreceitas.entity.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByTipoLancamento(Lancamento.TipoLancamento tipo);

    @Query("SELECT l FROM Lancamento l WHERE " +
           "(:situacao IS NULL OR l.situacao = :situacao) AND " +
           "(:tipo IS NULL OR l.tipoLancamento = :tipo) AND " +
           "(:dataInicio IS NULL OR l.dataLancamento >= :dataInicio) AND " +
           "(:dataFim IS NULL OR l.dataLancamento <= :dataFim) " +
           "ORDER BY l.dataLancamento DESC")
    List<Lancamento> findWithFilters(
        @Param("situacao") Lancamento.Situacao situacao,
        @Param("tipo") Lancamento.TipoLancamento tipo,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT l FROM Lancamento l ORDER BY l.dataLancamento DESC")
    List<Lancamento> findAllOrderByDataDesc();
}
