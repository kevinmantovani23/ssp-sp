package com.tcc.sspsp.repository;
 
import com.tcc.sspsp.model.Delegacias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface DelegaciasRepository extends JpaRepository<Delegacias, Long> {
    List<Delegacias> findByRegiao(String regiao);
    
    Optional<Delegacias> findByIdSSP(int id);
}
 