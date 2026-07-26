package com.tcc.sspsp.repository;
 
import com.tcc.sspsp.model.Natureza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
 
@Repository
public interface NaturezaRepository extends JpaRepository<Natureza, Long> {

    Optional<Natureza> findByNatureza(String natureza);
    
    
}