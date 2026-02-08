package com.fiap.check.health.exception;

public class MetaAlreadyCanceledException extends RuntimeException {
    
    public MetaAlreadyCanceledException(Long metaId) {
        super("Meta com ID " + metaId + " já está cancelada");
    }
    
    public MetaAlreadyCanceledException(String message) {
        super(message);
    }
}
