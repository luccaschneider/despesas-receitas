package com.univates.despesasreceitas.controller;

import com.univates.despesasreceitas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registro")
    public String registroForm() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(
        @RequestParam String nome,
        @RequestParam String login,
        @RequestParam String email,
        @RequestParam String senha
    ) {
        try {
            usuarioService.registrar(
                nome != null ? nome.trim() : "",
                login != null ? login.trim() : "",
                email != null ? email.trim() : "",
                senha != null ? senha : ""
            );
            return "redirect:/login?cadastro";
        } catch (IllegalStateException | IllegalArgumentException e) {
            return "redirect:/registro?erro";
        }
    }
}
