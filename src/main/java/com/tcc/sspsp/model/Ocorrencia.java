package com.tcc.sspsp.model;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
 
@Entity
@Table(name = "ocorrencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ocorrencia {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "natureza_id", nullable = false)
    private Natureza natureza;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegacia_id", nullable = false)
    private Delegacias delegacia;
 
    @Column(nullable = false)
    private Integer quantidade;
 
    @Column(nullable = false)
    private LocalDate data;
}