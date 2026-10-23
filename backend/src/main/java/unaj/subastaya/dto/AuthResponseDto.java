package unaj.subastaya.dto;

public record AuthResponseDto(
        Long id,
        String name,
        String email,
        String role,
        String token
) {
}