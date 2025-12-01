package com.elmirador.reservas.service;

import com.elmirador.reservas.model.Rol;
import com.elmirador.reservas.model.Usuario;
import com.elmirador.reservas.repository.RolRepository;
import com.elmirador.reservas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder; // Esto se configurará en la seguridad más adelante

    @Transactional
    public Usuario registrarCliente(Usuario usuario) {
        // 1. Validar que el email no exista
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado.");
        }

        // 2. Asignar rol de CLIENTE por defecto
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Error: Rol CLIENTE no encontrado en BD."));
        usuario.setRol(rolCliente);

        // 3. Encriptar contraseña (CRÍTICO PARA PRODUCCIÓN)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // 4. Activar usuario
        usuario.setEstado(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}