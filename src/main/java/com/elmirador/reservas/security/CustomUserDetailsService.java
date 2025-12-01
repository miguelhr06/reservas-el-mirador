package com.elmirador.reservas.security;

import com.elmirador.reservas.model.Usuario;
import com.elmirador.reservas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Spring Security necesita que los roles empiecen con "ROLE_"
        // Aquí convertimos tu rol "ADMINISTRADOR" a "ROLE_ADMINISTRADOR"
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre());

        return new User(
                usuario.getEmail(),
                usuario.getPassword(), // Aquí pasamos el hash (password_hash)
                usuario.getEstado(), // enabled (activo)
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                Collections.singletonList(authority));
    }
}