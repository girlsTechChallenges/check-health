package com.fiap.check.health.exception;

public class MetaNotFoundException extends RuntimeException {
    
    public MetaNotFoundException(Long metaId) {
        super("Meta não encontrada com ID: " + metaId);
    }
    
    public MetaNotFoundException(String message) {
        super(message);
    }
}
