package unaj.subastaya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unaj.subastaya.model.User;
import unaj.subastaya.repository.UserRepository;
import unaj.subastaya.service.UserActivitiesService;
import unaj.subastaya.dto.UserActivitiesDto;

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
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{userId}/activities")
    public ResponseEntity<UserActivitiesDto> getActivities(@PathVariable Long userId) {
        return ResponseEntity.ok(userActivitiesService.getActivities(userId));
    }
}
