package com.example.vkr.config;

import com.example.vkr.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Публичные страницы
                        .requestMatchers("/login", "/register", "/static/css/**", "/js/**", "/h2-console/**").permitAll()

                        // Профиль и смена пароля доступны для всех авторизованных
                        .requestMatchers("/users/profile", "/users/change-password").authenticated()

                        // Управление пользователями (список, создание, редактирование, удаление) — только ADMIN
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Админка
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Диспетчерские функции
                        .requestMatchers("/dispatcher/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers("/equipment/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers("/materials/**").hasAnyRole("ADMIN", "DISPATCHER")

                        // Заявки
                        .requestMatchers("/requests/create").hasAnyRole("ADMIN", "DISPATCHER", "REQUESTER", "WORKER")
                        .requestMatchers("/requests/**").authenticated()

                        // Остальное — только для авторизованных
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}