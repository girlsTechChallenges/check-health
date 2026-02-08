package com.fiap.check.health.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Frequencia {
    
    private String periodicidade; // diária, semanal etc.
    
    private Integer vezesPorPeriodo;
}
