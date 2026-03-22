package com.tcc.sspsp.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "natureza")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Natureza {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, unique = true, length = 100)
    private String natureza;
 
    @Column(length = 50)
    private String caracteristica;
}