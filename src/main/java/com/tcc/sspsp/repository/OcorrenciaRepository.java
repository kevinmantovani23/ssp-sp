package com.tcc.sspsp.repository;

import com.tcc.sspsp.model.Ocorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    // ocorrências filtradas por ano, natureza e delegacia
    @Query("""
        SELECT o FROM Ocorrencia o
        JOIN FETCH o.natureza n
        JOIN FETCH o.delegacia d
        WHERE (:ano IS NULL OR FUNCTION('YEAR', o.data) = :ano)
          AND (:naturezaId IS NULL OR n.id = :naturezaId)
          AND (:delegaciaId IS NULL OR d.id = :delegaciaId)
        ORDER BY o.data DESC
    """)
    Page<Ocorrencia> findWithFilters(
            @Param("ano") Integer ano,
            @Param("naturezaId") Long naturezaId,
            @Param("delegaciaId") Long delegaciaId,
            Pageable pageable
    );

    // totais por natureza e ano — grafico de barras
    @Query("""
        SELECT n.natureza                    AS natureza,
               FUNCTION('YEAR', o.data)     AS ano,
               SUM(o.quantidade)            AS total
        FROM Ocorrencia o
        JOIN o.natureza n
        WHERE (:ano IS NULL OR FUNCTION('YEAR', o.data) = :ano)
        GROUP BY n.natureza, FUNCTION('YEAR', o.data)
        ORDER BY total DESC
    """)
    List<Object[]> totalPorNaturezaEAno(@Param("ano") Integer ano);

    // serie historico mensal — grafico de linha
    @Query("""
        SELECT FUNCTION('YEAR',  o.data)  AS ano,
               FUNCTION('MONTH', o.data)  AS mes,
               SUM(o.quantidade)          AS total
        FROM Ocorrencia o
        JOIN o.natureza n
        WHERE n.id = :naturezaId
          AND FUNCTION('YEAR', o.data) BETWEEN :anoInicio AND :anoFim 
          AND (:delegaciaId IS NULL OR o.delegacia.id = :delegaciaId)
          AND (:regiao IS NULL OR o.delegacia.regiao LIKE :regiao)
        GROUP BY FUNCTION('YEAR', o.data), FUNCTION('MONTH', o.data)
        ORDER BY ano, mes
    """)
    List<Object[]> serieHistorica(
            @Param("naturezaId") Long naturezaId,
            @Param("anoInicio") int anoInicio,
            @Param("anoFim") int anoFim,
            @Param("delegaciaId") Long delegaciaId,
            @Param("regiao") String regiao
    );

    @Query("""
    SELECT FUNCTION('YEAR', o.data) AS ano,
           SUM(o.quantidade) AS total
    FROM Ocorrencia o
    WHERE o.natureza.id = :naturezaId
      AND (:delegaciaId IS NULL OR o.delegacia.id = :delegaciaId)
      AND (:regiao IS NULL OR o.delegacia.regiao LIKE :regiao)
      AND FUNCTION('YEAR', o.data) BETWEEN :anoInicio AND :anoFim
    GROUP BY FUNCTION('YEAR', o.data)
    ORDER BY FUNCTION('YEAR', o.data)
""")
    List<Object[]> serieAnual(@Param("naturezaId") Long naturezaId, @Param("delegaciaId") Long delegaciaId,
                              @Param("regiao") String regiao, @Param("anoInicio") int anoInicio, @Param("anoFim") int anoFim);



    // ranking de delegacias
    @Query("""
        SELECT d.delegacia              AS delegacia,
               d.regiao                AS regiao,
               SUM(o.quantidade)       AS total
        FROM Ocorrencia o
        JOIN o.delegacia d
        WHERE (:ano IS NULL OR FUNCTION('YEAR', o.data) = :ano)
          AND (:naturezaId IS NULL OR o.natureza.id = :naturezaId)
        GROUP BY d.delegacia, d.regiao
        ORDER BY total DESC
    """)
    List<Object[]> rankingDelegacias(@Param("ano") Integer ano, @Param("naturezaId") Long naturezaId);

    // total por natureza agrupado por região — visão lado a lado das regiões
    @Query("""
        SELECT n.natureza AS natureza,
               d.regiao   AS regiao,
               SUM(o.quantidade) AS total
        FROM Ocorrencia o
        JOIN o.natureza n
        JOIN o.delegacia d
        WHERE (:ano IS NULL OR FUNCTION('YEAR', o.data) = :ano)
          AND (:naturezaId IS NULL OR n.id = :naturezaId)
        GROUP BY n.natureza, d.regiao
        ORDER BY d.regiao, total DESC
    """)
    List<Object[]> totalPorRegiao(@Param("ano") Integer ano, @Param("naturezaId") Long naturezaId);

    //Buscar a ultima data da ocorrencia importada de uma delegacia
    @Query("SELECT MAX(o.data) FROM Ocorrencia o WHERE o.delegacia.id = :delegaciaId")
    LocalDate buscarUltimaDataImportadaPorDelegacia(
            @Param("delegaciaId") Long idDelegacia
    );

    // soma total de ocorrências registradas — usado no resumo de estatísticas
    @Query("SELECT COALESCE(SUM(o.quantidade), 0) FROM Ocorrencia o")
    Long somaTotalQuantidade();

    // menor e maior data com ocorrência registrada em toda a base — usado pelo endpoint de cobertura
    @Query("SELECT MIN(o.data), MAX(o.data) FROM Ocorrencia o")
    List<Object[]> buscarPeriodoGeral();

    @Query("""
	    SELECT AVG(totalMensal) FROM (
	    SELECT SUM(o.quantidade) as totalMensal    
	    FROM Ocorrencia o 
	    WHERE o.natureza.id = :tipo AND YEAR(o.data) = :ano AND 
	    (:delegaciaId IS NULL OR o.delegacia.id = :delegaciaId) AND 
	    (:regiao IS NULL OR o.delegacia.regiao LIKE :regiao)
	    GROUP BY MONTH(o.data)
	    )    
	        """)
    Double calcularMediaMensal(
            @Param("tipo") Long natureza,
            @Param("ano") int ano,
            @Param("delegaciaId") Long delegaciaId,
            @Param("regiao") String regiao);

    // menor e maior data com ocorrência registrada para uma natureza — usado para descobrir automaticamente o período disponível
    @Query("""
        SELECT MIN(o.data), MAX(o.data) FROM Ocorrencia o 
        WHERE o.natureza.id = :naturezaId AND 
        (:delegaciaId IS NULL or o.delegacia.id = :delegaciaId) AND 
        (:regiao IS NULL OR o.delegacia.regiao LIKE :regiao)
                        """)
    List<Object[]> buscarPeriodoOcorrencia(
            @Param("naturezaId") Long naturezaId,
            @Param("delegaciaId") Long delegaciaId,
            @Param("regiao") String regiao);

}