//package tech.csm.service;
//
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final PasswordEncoder passwordEncoder;
//
//    public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username)
//            throws UsernameNotFoundException {
//
//        // TEMP: in-memory user (later replace with DB)
//        if (!"abhi".equals(username)) {
//            throw new UsernameNotFoundException("User not found");
//        }
//
//        return User.builder()
//                .username("abhi")
//                .password(passwordEncoder.encode("1234"))
//                .roles("USER")
//                .build();
//    }
//}


package tech.csm.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // BCrypt hash for "1234"
    private static final String ENCODED_PASSWORD =
        "$2a$10$MSi5vIMCoF5wI7FUjRTz1u7KOTSoctudgjLXeil50U2ewHhogH7cG";

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        if (!"abhi".equals(username)) {
            throw new UsernameNotFoundException("User not found");
        }

        return User.builder()
                .username("abhi")
                .password(ENCODED_PASSWORD)
                .roles("USER")
                .build();
    }
}
