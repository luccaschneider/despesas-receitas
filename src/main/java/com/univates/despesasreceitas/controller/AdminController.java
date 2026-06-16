package com.univates.despesasreceitas.controller;

import com.univates.despesasreceitas.entity.ConfiguracaoEmail;
import com.univates.despesasreceitas.service.ConfiguracaoEmailService;
import com.univates.despesasreceitas.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ConfiguracaoEmailService configuracaoEmailService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/configuracoes")
    public String configuracoes(Model model) {
        ConfiguracaoEmail c = configuracaoEmailService.carregar();
        c.setSmtpPassword("");
        model.addAttribute("configuracao", c);
        return "admin/configuracoes";
    }

    @PostMapping("/configuracoes")
    public String salvar(@ModelAttribute ConfiguracaoEmail configuracao, RedirectAttributes ra) {
        configuracaoEmailService.salvar(configuracao);
        ra.addFlashAttribute("sucesso", "Configurações salvas com sucesso.");
        return "redirect:/admin/configuracoes";
    }

    @PostMapping("/configuracoes/testar")
    public String testar(@ModelAttribute ConfiguracaoEmail configuracao, RedirectAttributes ra) {
        configuracaoEmailService.salvar(configuracao);
        try {
            emailService.enviarEmailTesteSincrono();
            ra.addFlashAttribute("sucesso", "E-mail de teste enviado. Verifique a caixa de entrada (e spam).");
        } catch (IllegalStateException e) {
            log.warn("Teste de e-mail falhou: {}", e.getMessage());
            ra.addFlashAttribute("erro", e.getMessage());
        } catch (Exception e) {
            log.warn("Teste de e-mail falhou", e);
            ra.addFlashAttribute("erro", "Não foi possível enviar o teste: " + e.getMessage());
        }
        return "redirect:/admin/configuracoes";
    }
}
