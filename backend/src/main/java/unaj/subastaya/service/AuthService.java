package unaj.subastaya.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import unaj.subastaya.dto.AuthResponseDto;
import unaj.subastaya.dto.LoginRequestDto;
import unaj.subastaya.dto.RegisterRequestDto;
import unaj.subastaya.model.User;
import unaj.subastaya.model.UserRole;
import unaj.subastaya.repository.UserRepository;
import unaj.subastaya.service.JwtService;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            WalletService walletService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletService = walletService;
    }

    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        if (request == null
                || request.name() == null
                || request.name().isBlank()
                || request.email() == null
                || request.email().isBlank()
                || request.password() == null
                || request.password().length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completá los datos y usá una contraseña de al menos 8 caracteres"
            );
        }

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese email"
            );
        }

        User user = new User();
        user.setRole(UserRole.USER);
        user.setName(request.name().trim());
        user.setEmail(email);
        //Generating a password hash using the password encoder and setting it to the user
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRegistrationDate(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        walletService.createWalletForUser(savedUser);
        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {

        if (request == null
                || request.email() == null
                || request.email().isBlank()
                || request.password() == null
                || request.password().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debés ingresar email y contraseña"
            );
        }

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Credenciales incorrectas"
                        )
                );

        // Verifying the password using the password encoder
        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas"
            );
        }

        return toResponse(user);
    }

    private AuthResponseDto toResponse(User user) {
        return new AuthResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                jwtService.generateToken(user.getEmail())
        );
    }
}