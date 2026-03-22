package com.tcc.sspsp.service;

import com.tcc.sspsp.dto.*;
import com.tcc.sspsp.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcorrenciaService {

    private final OcorrenciaRepository repo;

    public Page<OcorrenciaResponseDTO> listarComFiltros(OcorrenciaFiltroDTO filtro) {
        var pageable = PageRequest.of(filtro.page(), filtro.size());
        return repo.findWithFilters(filtro.ano(), filtro.naturezaId(), filtro.delegaciaId(), pageable)
            .map(o -> new OcorrenciaResponseDTO(
                o.getId(),
                o.getNatureza().getNatureza(),
                o.getNatureza().getCaracteristica(),
                o.getDelegacia().getDelegacia(),
                o.getDelegacia().getRegiao(),
                o.getQuantidade(),
                o.getData()
            ));
    }

    public List<TotalNaturezaDTO> totalPorNatureza(Integer ano) {
        return repo.totalPorNaturezaEAno(ano).stream()
            .map(row -> new TotalNaturezaDTO(
                (String)  row[0],
                ((Number) row[1]).intValue(),
                ((Number) row[2]).longValue()
            ))
            .toList();
    }

    public List<SerieHistoricaDTO> serieHistorica(Long naturezaId, int anoInicio, int anoFim) {
        return repo.serieHistorica(naturezaId, anoInicio, anoFim).stream()
            .map(row -> new SerieHistoricaDTO(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).intValue(),
                ((Number) row[2]).longValue()
            ))
            .toList();
    }

    public List<RankingDelegaciaDTO> rankingDelegacias(Integer ano) {
        return repo.rankingDelegacias(ano).stream()
            .map(row -> new RankingDelegaciaDTO(
                (String)  row[0],
                (String)  row[1],
                ((Number) row[2]).longValue()
            ))
            .toList();
    }
}