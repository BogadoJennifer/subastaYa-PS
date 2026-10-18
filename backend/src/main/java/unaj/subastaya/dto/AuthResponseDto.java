package unaj.subastaya.dto;

public record AuthResponseDto(
        Long id,
        String name,
        String email,
        String token
) {
}