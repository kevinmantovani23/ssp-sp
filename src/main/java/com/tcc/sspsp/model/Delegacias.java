package com.tcc.sspsp.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "delegacias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Delegacias {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer idSSP;
 
    @Column(nullable = false, length = 100)
    private String delegacia;
 
    
    @Column(length = 100)
    private String regiao;
}