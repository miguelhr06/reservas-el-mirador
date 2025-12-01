package com.elmirador.reservas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index"; // Buscará index.html en templates
    }
    @GetMapping("/info/gastronomia")
    public String gastronomia(){
        return "info/gastronomia";
    }
     @GetMapping("/info/piscinas")
    public String piscinas(){
        return "info/piscinas";
    }
     @GetMapping("/info/naturaleza")
    public String naturaleza(){
        return "info/naturaleza";
    }
}