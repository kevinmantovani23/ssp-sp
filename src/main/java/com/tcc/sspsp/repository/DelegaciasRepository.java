package com.tcc.sspsp.repository;
 
import com.tcc.sspsp.model.Delegacias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface DelegaciasRepository extends JpaRepository<Delegacias, Long> {
    List<Delegacias> findByRegiao(String regiao);
    
    Optional<Delegacias> findByIdSSP(int id);
    
    // delegacias filtradas por região, nome (parcial) e idSSP
    @Query("""
        SELECT d FROM Delegacias d
        WHERE (:regiao IS NULL OR d.regiao = :regiao)
          AND (:delegacia IS NULL OR UPPER(d.delegacia) LIKE UPPER(CONCAT('%', :delegacia, '%')))
          AND (:idSSP IS NULL OR d.idSSP = :idSSP)
        ORDER BY d.delegacia
    """)
    List<Delegacias> findWithFilters(
        @Param("regiao") String regiao,
        @Param("delegacia") String delegacia,
        @Param("idSSP") Integer idSSP
    );
}
 