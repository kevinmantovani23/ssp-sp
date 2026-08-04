package com.tcc.sspsp.repository;
 
import com.tcc.sspsp.dto.NaturezaDTO;
import com.tcc.sspsp.model.Natureza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
 
@Repository
public interface NaturezaRepository extends JpaRepository<Natureza, Long> {

    Optional<Natureza> findByNatureza(String natureza);

    @Query("""
        SELECT new com.tcc.sspsp.dto.NaturezaDTO (
            n.id,
            n.natureza
        ) FROM Natureza n
    """)
    List<NaturezaDTO> findDTOAll();
}