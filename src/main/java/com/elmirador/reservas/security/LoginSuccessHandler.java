package com.elmirador.reservas.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        // Obtener la colección de roles (Authorities) del usuario autenticado
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        String redirectUrl = "/"; // Ruta por defecto

        // Iterar sobre los roles para determinar la redirección
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_RECEPCIONISTA")) {
                // Si es ADMIN o RECEPCIONISTA, va al panel administrativo
                redirectUrl = "/recepcion/dashboard";
                break; // El rol administrativo tiene prioridad
            } else if (role.equals("ROLE_CLIENTE")) {
                // Si es CLIENTE, va a su dashboard/lista de reservas
                redirectUrl = "/reservas/mis-reservas";
                // No rompemos el ciclo por si acaso tiene otro rol, pero el cliente es el
                // principal
            }
        }

        // Redirigir al usuario
        response.sendRedirect(redirectUrl);
    }
}