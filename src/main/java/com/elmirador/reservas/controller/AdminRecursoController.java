package com.elmirador.reservas.controller;

import com.elmirador.reservas.model.Recurso;
import com.elmirador.reservas.service.RecursoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/recursos")
@RequiredArgsConstructor
public class AdminRecursoController {

    private final RecursoService recursoService;

    // 1. Listar todos
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recursos", recursoService.listarTodos());
        return "admin/recursos/lista";
    }

    // 2. Mostrar formulario para CREAR
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("recurso", new Recurso());
        model.addAttribute("tipos", recursoService.listarTipos()); // Para el <select>
        return "admin/recursos/formulario";
    }

    // 3. Mostrar formulario para EDITAR
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Recurso recurso = recursoService.obtenerPorId(id);
        model.addAttribute("recurso", recurso);
        model.addAttribute("tipos", recursoService.listarTipos());
        return "admin/recursos/formulario";
    }

    // 4. Guardar (Sirve para Crear y Editar)
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Recurso recurso) {
        recursoService.guardar(recurso);
        return "redirect:/admin/recursos";
    }

    // 5. Eliminar
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        recursoService.eliminar(id);
        return "redirect:/admin/recursos";
    }
}