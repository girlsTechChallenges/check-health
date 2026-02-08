package com.fiap.check.health.exception;

public class MetaAlreadyInProgressException extends RuntimeException {
    
    public MetaAlreadyInProgressException(Long metaId) {
        super("Meta com ID " + metaId + " já está em progresso");
    }
    
    public MetaAlreadyInProgressException(String message) {
        super(message);
    }
}
