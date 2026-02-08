package com.fiap.check.health.exception;

public class MetaAlreadyCompletedException extends RuntimeException {
    
    public MetaAlreadyCompletedException(Long metaId) {
        super("Meta com ID " + metaId + " já está completa");
    }
    
    public MetaAlreadyCompletedException(String message) {
        super(message);
    }
}
