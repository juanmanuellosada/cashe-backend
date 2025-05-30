package com.cashe.backend.config;

public class JwtConstants {
    // ¡¡¡IMPORTANTE: ESTA CLAVE ES SOLO PARA DESARROLLO!!!
    // ¡¡¡EN PRODUCCIÓN, USA UNA CLAVE SEGURA Y CARGADA DESDE CONFIGURACIÓN
    // EXTERNA!!!
    public static final String JWT_SECRET_KEY = "C@sh3S3cr3tK3yF0rD3v3l0pm3ntPurposezOnly!2024";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 días en milisegundos
}