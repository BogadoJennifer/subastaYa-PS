package unaj.subastaya.dto;

public record RegisterRequestDto(
        String name,
        String email,
        String password
) {
}