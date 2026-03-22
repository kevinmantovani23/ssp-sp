package com.tcc.sspsp.repository;

import com.tcc.sspsp.model.Ocorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
        GROUP BY FUNCTION('YEAR', o.data), FUNCTION('MONTH', o.data)
        ORDER BY ano, mes
    """)
    List<Object[]> serieHistorica(
        @Param("naturezaId") Long naturezaId,
        @Param("anoInicio") int anoInicio,
        @Param("anoFim") int anoFim
    );

    // ranking de delegacias
    @Query("""
        SELECT d.delegacia              AS delegacia,
               d.regiao                AS regiao,
               SUM(o.quantidade)       AS total
        FROM Ocorrencia o
        JOIN o.delegacia d
        WHERE (:ano IS NULL OR FUNCTION('YEAR', o.data) = :ano)
        GROUP BY d.delegacia, d.regiao
        ORDER BY total DESC
    """)
    List<Object[]> rankingDelegacias(@Param("ano") Integer ano);
}