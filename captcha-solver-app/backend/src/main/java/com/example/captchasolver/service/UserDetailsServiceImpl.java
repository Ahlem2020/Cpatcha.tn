package com.example.captchasolver.service;

import com.example.captchasolver.model.User;
import com.example.captchasolver.repository.UserRepository;
import com.example.captchasolver.security.services.UserDetailsImpl; // Import custom UserDetailsImpl
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import java.util.Set;
// import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional // Removed readOnly = true as it might be problematic with some JPA providers if entities are lazy-loaded by UserDetailsImpl.build()
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user); // Use the static build method from our UserDetailsImpl
    }
}
