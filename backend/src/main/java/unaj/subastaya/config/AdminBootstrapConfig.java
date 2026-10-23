package unaj.subastaya.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import unaj.subastaya.model.User;
import unaj.subastaya.model.UserRole;
import unaj.subastaya.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Locale;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    CommandLineRunner bootstrapAdministrators(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.admin1.email:}") String admin1Email,
            @Value("${app.bootstrap.admin1.password:}") String admin1Password,
            @Value("${app.bootstrap.admin2.email:}") String admin2Email,
            @Value("${app.bootstrap.admin2.password:}") String admin2Password
    ) {
        return args -> {
            boolean allEmpty =
                    admin1Email.isBlank()
                            && admin1Password.isBlank()
                            && admin2Email.isBlank()
                            && admin2Password.isBlank();

            if (allEmpty) {
                return;
            }

            requireCredentials(admin1Email, admin1Password, "primer");
            requireCredentials(admin2Email, admin2Password, "segundo");

            String normalizedEmail1 =
                    admin1Email.trim().toLowerCase(Locale.ROOT);
            String normalizedEmail2 =
                    admin2Email.trim().toLowerCase(Locale.ROOT);

            if (normalizedEmail1.equals(normalizedEmail2)) {
                throw new IllegalStateException(
                        "Los dos administradores deben tener emails distintos"
                );
            }

            createOrPromoteAdmin(
                    userRepository,
                    passwordEncoder,
                    normalizedEmail1,
                    admin1Password,
                    "Administrador 1"
            );

            createOrPromoteAdmin(
                    userRepository,
                    passwordEncoder,
                    normalizedEmail2,
                    admin2Password,
                    "Administrador 2"
            );
        };
    }

    private void requireCredentials(
            String email,
            String password,
            String label
    ) {
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Configurá email y contraseña del " + label
                            + " administrador"
            );
        }
    }

    private void createOrPromoteAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String password,
            String name
    ) {
        var existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getRole() != UserRole.ADMIN) {
                user.setRole(UserRole.ADMIN);
                userRepository.save(user);
            }

            return;
        }

        User admin = new User();
        admin.setName(name);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRegistrationDate(LocalDateTime.now());
        admin.setRole(UserRole.ADMIN);

        userRepository.save(admin);
    }
}