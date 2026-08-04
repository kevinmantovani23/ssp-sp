package com.tcc.sspsp.repository;
 
import com.tcc.sspsp.dto.DelegaciasDTO;
import com.tcc.sspsp.model.Delegacias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface DelegaciasRepository extends JpaRepository<Delegacias, Long> {

    Optional<Delegacias> findByIdSSP(int id);

    @Query("""
    SELECT new com.tcc.sspsp.dto.DelegaciasDTO(
                d.id,
                d.delegacia,
                d.regiao
            ) FROM Delegacias d
    WHERE d.id = :id
    """)
    Optional<DelegaciasDTO> findDTOById(@Param("id") Long id);

    @Query("""
        SELECT new com.tcc.sspsp.dto.DelegaciasDTO(
                d.id,
                d.delegacia,
                d.regiao
            ) FROM Delegacias d
        WHERE (:regiao IS NULL OR d.regiao LIKE :regiao)
          AND (:delegacia IS NULL OR UPPER(d.delegacia) LIKE UPPER(CONCAT('%', :delegacia, '%')))
        ORDER BY d.delegacia
    """)
    List<DelegaciasDTO> findWithFilters(
        @Param("regiao") String regiao,
        @Param("delegacia") String delegacia
    );

    @Query("""
        SELECT DISTINCT d.regiao FROM Delegacias d
    """)
    List<String> listarRegioes();
}
 