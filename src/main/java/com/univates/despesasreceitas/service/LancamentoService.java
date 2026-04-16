package com.univates.despesasreceitas.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.univates.despesasreceitas.entity.Lancamento;
import com.univates.despesasreceitas.repository.LancamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LancamentoService {

    @Autowired
    private LancamentoRepository lancamentoRepository;

    public List<Lancamento> listarTodos() {
        return lancamentoRepository.findAllOrderByDataDesc();
    }

    public List<Lancamento> filtrar(Lancamento.Situacao situacao,
                                    Lancamento.TipoLancamento tipo,
                                    LocalDate dataInicio, LocalDate dataFim) {
        return lancamentoRepository.findWithFilters(situacao, tipo, dataInicio, dataFim);
    }

    public Lancamento buscarPorId(Long id) {
        return lancamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lançamento não encontrado: " + id));
    }

    public Lancamento salvar(Lancamento lancamento) {
        return lancamentoRepository.save(lancamento);
    }

    public void excluir(Long id) {
        lancamentoRepository.deleteById(id);
    }

    public BigDecimal totalReceitas() {
        return lancamentoRepository.findByTipoLancamento(Lancamento.TipoLancamento.RECEITA)
            .stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalDespesas() {
        return lancamentoRepository.findByTipoLancamento(Lancamento.TipoLancamento.DESPESA)
            .stream().map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal saldo() {
        return totalReceitas().subtract(totalDespesas());
    }

    public void exportarPdf(OutputStream os) throws Exception {
        Document doc = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(doc, os);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(26, 35, 126));
        Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font hFont     = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Font cellFont  = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph title = new Paragraph("Relatório de Lançamentos", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph sub = new Paragraph("Gerado em: " +
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), subFont);
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 3f, 1.5f, 1.5f, 1.5f, 1.5f});

        Color headerColor = new Color(26, 35, 126);
        for (String h : new String[]{"#", "Descrição", "Data", "Valor", "Tipo", "Situação"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(headerColor);
            cell.setPadding(7);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean alt = false;
        for (Lancamento l : listarTodos()) {
            Color bg = alt ? new Color(245, 245, 245) : Color.WHITE;
            Color valorColor = l.getTipoLancamento() == Lancamento.TipoLancamento.RECEITA
                ? new Color(46, 125, 50) : new Color(198, 40, 40);

            addCell(table, l.getId().toString(), cellFont, bg, Element.ALIGN_CENTER);
            addCell(table, l.getDescricao(), cellFont, bg, Element.ALIGN_LEFT);
            addCell(table, l.getDataLancamento().format(fmt), cellFont, bg, Element.ALIGN_CENTER);

            Font vf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, valorColor);
            PdfPCell vc = new PdfPCell(new Phrase(String.format("R$ %.2f", l.getValor()), vf));
            vc.setBackgroundColor(bg); vc.setPadding(6);
            vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(vc);

            addCell(table, l.getTipoLancamento().name(), cellFont, bg, Element.ALIGN_CENTER);
            addCell(table, l.getSituacao().name(), cellFont, bg, Element.ALIGN_CENTER);
            alt = !alt;
        }

        doc.add(table);

        doc.add(Chunk.NEWLINE);
        Font sumFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        doc.add(new Paragraph(String.format("Total Receitas: R$ %.2f  |  Total Despesas: R$ %.2f  |  Saldo: R$ %.2f",
            totalReceitas(), totalDespesas(), saldo()), sumFont));

        doc.close();
    }

    private void addCell(PdfPTable t, String text, Font f, Color bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setPadding(6);
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }
}
