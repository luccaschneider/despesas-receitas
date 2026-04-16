package com.univates.despesasreceitas.service;

import com.univates.despesasreceitas.entity.Lancamento;
import com.univates.despesasreceitas.repository.LancamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LancamentoServiceTest {

    @Mock private LancamentoRepository repository;
    @InjectMocks private LancamentoService service;

    private Lancamento receita;
    private Lancamento despesa;

    @BeforeEach
    void setUp() {
        receita = new Lancamento(1L, "Salário", LocalDate.now(), new BigDecimal("5000.00"),
            Lancamento.TipoLancamento.RECEITA, Lancamento.Situacao.PAGO);
        despesa = new Lancamento(2L, "Aluguel", LocalDate.now(), new BigDecimal("1200.00"),
            Lancamento.TipoLancamento.DESPESA, Lancamento.Situacao.PAGO);
    }

    @Test void test01_listarTodos_retornaLista() {
        when(repository.findAllOrderByDataDesc()).thenReturn(Arrays.asList(receita, despesa));
        List<Lancamento> result = service.listarTodos();
        assertEquals(2, result.size());
        verify(repository).findAllOrderByDataDesc();
    }

    @Test void test02_buscarPorId_encontrado() {
        when(repository.findById(1L)).thenReturn(Optional.of(receita));
        Lancamento result = service.buscarPorId(1L);
        assertEquals("Salário", result.getDescricao());
    }

    @Test void test03_buscarPorId_naoEncontrado_lancaException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.buscarPorId(99L));
    }

    @Test void test04_salvar_chamaRepository() {
        when(repository.save(receita)).thenReturn(receita);
        Lancamento result = service.salvar(receita);
        assertNotNull(result);
        verify(repository).save(receita);
    }

    @Test void test05_excluir_chamaDeleteById() {
        doNothing().when(repository).deleteById(1L);
        service.excluir(1L);
        verify(repository).deleteById(1L);
    }

    @Test void test06_totalReceitas_somaCorreto() {
        Lancamento r2 = new Lancamento(3L, "Freelance", LocalDate.now(), new BigDecimal("800.00"),
            Lancamento.TipoLancamento.RECEITA, Lancamento.Situacao.PAGO);
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.RECEITA))
            .thenReturn(Arrays.asList(receita, r2));
        BigDecimal total = service.totalReceitas();
        assertEquals(new BigDecimal("5800.00"), total);
    }

    @Test void test07_totalDespesas_somaCorreto() {
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.DESPESA))
            .thenReturn(List.of(despesa));
        BigDecimal total = service.totalDespesas();
        assertEquals(new BigDecimal("1200.00"), total);
    }

    @Test void test08_saldo_positivo() {
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.RECEITA))
            .thenReturn(List.of(receita));
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.DESPESA))
            .thenReturn(List.of(despesa));
        BigDecimal saldo = service.saldo();
        assertEquals(new BigDecimal("3800.00"), saldo);
    }

    @Test void test09_saldo_negativo() {
        Lancamento grandeDespesa = new Lancamento(3L, "Dívida", LocalDate.now(),
            new BigDecimal("9000.00"), Lancamento.TipoLancamento.DESPESA, Lancamento.Situacao.PENDENTE);
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.RECEITA))
            .thenReturn(List.of(receita));
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.DESPESA))
            .thenReturn(List.of(grandeDespesa));
        assertTrue(service.saldo().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test void test10_totalReceitas_semReceitas_retornaZero() {
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.RECEITA))
            .thenReturn(List.of());
        assertEquals(BigDecimal.ZERO, service.totalReceitas());
    }

    @Test void test11_totalDespesas_semDespesas_retornaZero() {
        when(repository.findByTipoLancamento(Lancamento.TipoLancamento.DESPESA))
            .thenReturn(List.of());
        assertEquals(BigDecimal.ZERO, service.totalDespesas());
    }

    @Test void test12_filtrar_chamaRepositoryComParametros() {
        when(repository.findWithFilters(any(), any(), any(), any()))
            .thenReturn(List.of(receita));
        List<Lancamento> result = service.filtrar(
            Lancamento.Situacao.PAGO, Lancamento.TipoLancamento.RECEITA,
            LocalDate.now().minusDays(30), LocalDate.now());
        assertEquals(1, result.size());
        verify(repository).findWithFilters(any(), any(), any(), any());
    }

    @Test void test13_filtrar_semFiltros_retornaTodos() {
        when(repository.findWithFilters(null, null, null, null))
            .thenReturn(Arrays.asList(receita, despesa));
        List<Lancamento> result = service.filtrar(null, null, null, null);
        assertEquals(2, result.size());
    }

    @Test void test14_salvar_novoLancamento_retornaComId() {
        Lancamento novo = new Lancamento(null, "Novo", LocalDate.now(),
            new BigDecimal("100.00"), Lancamento.TipoLancamento.RECEITA, Lancamento.Situacao.PENDENTE);
        Lancamento salvo = new Lancamento(10L, "Novo", LocalDate.now(),
            new BigDecimal("100.00"), Lancamento.TipoLancamento.RECEITA, Lancamento.Situacao.PENDENTE);
        when(repository.save(novo)).thenReturn(salvo);
        Lancamento result = service.salvar(novo);
        assertNotNull(result.getId());
        assertEquals(10L, result.getId());
    }

    @Test void test15_listarTodos_listaVazia() {
        when(repository.findAllOrderByDataDesc()).thenReturn(List.of());
        assertTrue(service.listarTodos().isEmpty());
    }
}
