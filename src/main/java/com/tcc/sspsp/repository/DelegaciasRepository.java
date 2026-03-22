package com.tcc.sspsp.repository;
 
import com.tcc.sspsp.model.Delegacias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface DelegaciasRepository extends JpaRepository<Delegacias, Long> {
    List<Delegacias> findByRegiao(String regiao);
}
 