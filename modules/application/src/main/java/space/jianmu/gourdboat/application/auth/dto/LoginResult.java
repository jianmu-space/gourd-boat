package space.jianmu.gourdboat.application.auth.dto;

public record LoginResult(
    String token,
    String identifier,
    String role
) {} 