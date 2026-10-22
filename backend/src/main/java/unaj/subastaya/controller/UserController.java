package unaj.subastaya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import unaj.subastaya.model.User;
import unaj.subastaya.repository.UserRepository;
import unaj.subastaya.service.UserActivitiesService;
import unaj.subastaya.dto.UserActivitiesDto;
import unaj.subastaya.dto.AdminUserDto;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserActivitiesService userActivitiesService;

    public UserController(UserRepository userRepository, UserActivitiesService userActivitiesService) {
        this.userRepository = userRepository;
        this.userActivitiesService = userActivitiesService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        List<AdminUserDto> users = userRepository.findAll()
                .stream()
                .map(user -> new AdminUserDto(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getRegistrationDate()
                ))
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/me/activities")
    public ResponseEntity<UserActivitiesDto> getActivities(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(
                userActivitiesService.getActivities(authenticatedUser.getId())
        );
    }
}
