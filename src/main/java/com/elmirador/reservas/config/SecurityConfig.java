package com.elmirador.reservas.config;

import com.elmirador.reservas.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationSuccessHandler loginSuccessHandler;

    // 1. Bean para encriptar contraseñas (BCrypt es el estándar actual)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Definir el proveedor de autenticación (BD + PasswordEncoder)
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 3. Cadena de filtros de seguridad (Las reglas del juego)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // RUTAS PÚBLICAS (Todos pueden entrar)
                        .requestMatchers("/", "/index", "/registro", "/login", "/css/**", "/js/**", "/images/**",
                                "/reservas/buscar", "/reservas/resultados", "/info/**")
                        .permitAll()
                        // RUTAS SOLO PARA ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")

                        // RUTAS SOLO PARA RECEPCIÓN O ADMIN
                        .requestMatchers("/recepcion/**").hasAnyRole("RECEPCION", "ADMINISTRADOR")

                        // RUTAS PROTEGIDAS (Cualquier usuario logueado: Cliente, Admin, etc.)
                        .requestMatchers("/reservas/**", "/perfil/**").authenticated()

                        // CUALQUIER OTRA RUTA: Requiere login
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login") // URL de tu vista de login personalizada
                        .loginProcessingUrl("/perform_login") // URL donde el formulario hace POST
                        .successHandler(loginSuccessHandler) // A dónde va si login es exitoso
                        .failureUrl("/login?error=true") // A dónde va si falla
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}