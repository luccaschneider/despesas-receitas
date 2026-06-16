package com.univates.despesasreceitas.controller;

import com.univates.despesasreceitas.service.AnsibleCommandResult;
import com.univates.despesasreceitas.service.AnsibleDeploymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/ambientes")
public class EnvironmentAdminController {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentAdminController.class);

    private final AnsibleDeploymentService deploymentService;

    public EnvironmentAdminController(AnsibleDeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping
    public String ambientes(Model model) {
        model.addAttribute("ansibleEnabled", deploymentService.isEnabled());
        model.addAttribute("ambientes", deploymentService.listEnvironments());
        return "admin/ambientes";
    }

    @PostMapping("/{ambiente}/rodar")
    public String rodar(@PathVariable String ambiente, RedirectAttributes ra) {
        return executarAcao(ambiente, "subir", () -> deploymentService.deploy(ambiente), ra);
    }

    @PostMapping("/{ambiente}/stop")
    public String stop(@PathVariable String ambiente, RedirectAttributes ra) {
        return executarAcao(ambiente, "parar", () -> deploymentService.stop(ambiente), ra);
    }

    private String executarAcao(
        String ambiente,
        String acao,
        AnsibleAction action,
        RedirectAttributes ra
    ) {
        try {
            AnsibleCommandResult result = action.execute();
            if (result.success()) {
                ra.addFlashAttribute("sucesso", "Acao enviada com sucesso para o ambiente " + ambiente + ".");
            } else {
                log.warn("Ansible retornou erro ao {} ambiente {}: {}", acao, ambiente, result.output());
                ra.addFlashAttribute(
                    "erro",
                    "Nao foi possivel " + acao + " o ambiente " + ambiente + ". Codigo: " + result.exitCode()
                );
            }
        } catch (RuntimeException e) {
            log.warn("Erro ao {} ambiente {}", acao, ambiente, e);
            ra.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/admin/ambientes";
    }

    @FunctionalInterface
    private interface AnsibleAction {
        AnsibleCommandResult execute();
    }
}
