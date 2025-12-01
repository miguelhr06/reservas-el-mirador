package com.elmirador.reservas.controller;

import com.elmirador.reservas.model.Usuario;
import com.elmirador.reservas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    // Mostrar formulario de Login
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("mensajeError", "Usuario o contraseña incorrectos.");
        }
        if (logout != null) {
            model.addAttribute("mensajeExito", "Has cerrado sesión correctamente.");
        }
        return "auth/login"; // Buscará en templates/auth/login.html
    }

    // Mostrar formulario de Registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    // Procesar el Registro
    @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute Usuario usuario, Model model) {
        try {
            usuarioService.registrarCliente(usuario);
            return "redirect:/login?registroExitoso";
        } catch (Exception e) {
            model.addAttribute("mensajeError", "Error al registrar: " + e.getMessage());
            model.addAttribute("usuario", usuario); // Devolver datos para no borrarlos
            return "auth/registro";
        }
    }
}