package com.univates.despesasreceitas.controller;

import com.univates.despesasreceitas.entity.Lancamento;
import com.univates.despesasreceitas.service.EmailService;
import com.univates.despesasreceitas.service.LancamentoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/")
public class LancamentoController {

    @Autowired private LancamentoService lancamentoService;
    @Autowired private EmailService emailService;

    @GetMapping
    public String index(
        @RequestParam(required = false) String situacao,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        Model model) {

        Lancamento.Situacao sit = (situacao != null && !situacao.isEmpty())
            ? Lancamento.Situacao.valueOf(situacao) : null;
        Lancamento.TipoLancamento tp = (tipo != null && !tipo.isEmpty())
            ? Lancamento.TipoLancamento.valueOf(tipo) : null;

        model.addAttribute("lancamentos", lancamentoService.filtrar(sit, tp, dataInicio, dataFim));
        model.addAttribute("totalReceitas", lancamentoService.totalReceitas());
        model.addAttribute("totalDespesas", lancamentoService.totalDespesas());
        model.addAttribute("saldo", lancamentoService.saldo());
        model.addAttribute("situacaoSelecionada", situacao);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        return "lancamentos";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("lancamento", new Lancamento());
        model.addAttribute("tipos", Lancamento.TipoLancamento.values());
        model.addAttribute("situacoes", Lancamento.Situacao.values());
        return "form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Lancamento lancamento, RedirectAttributes ra) {
        Lancamento salvo = lancamentoService.salvar(lancamento);
        emailService.enviarEmailCriacao(salvo);
        ra.addFlashAttribute("sucesso", "Lançamento criado com sucesso!");
        return "redirect:/";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("lancamento", lancamentoService.buscarPorId(id));
        model.addAttribute("tipos", Lancamento.TipoLancamento.values());
        model.addAttribute("situacoes", Lancamento.Situacao.values());
        return "form";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizar(@PathVariable Long id, @ModelAttribute Lancamento lancamento, RedirectAttributes ra) {
        lancamento.setId(id);
        Lancamento salvo = lancamentoService.salvar(lancamento);
        emailService.enviarEmailAtualizacao(salvo);
        ra.addFlashAttribute("sucesso", "Lançamento atualizado com sucesso!");
        return "redirect:/";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes ra) {
        lancamentoService.excluir(id);
        ra.addFlashAttribute("sucesso", "Lançamento excluído com sucesso!");
        return "redirect:/";
    }

    @GetMapping("/exportar-pdf")
    public void exportarPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=lancamentos.pdf");
        lancamentoService.exportarPdf(response.getOutputStream());
    }
}
