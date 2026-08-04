package com.tcc.sspsp.service;

import com.tcc.sspsp.dto.DelegaciasDTO;
import com.tcc.sspsp.repository.DelegaciasRepository;
import com.tcc.sspsp.utils.NormalizaCampos;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DelegaciasService {

    private final DelegaciasRepository repo;

    public List<DelegaciasDTO> listarComFiltros(String regiao, String delegacia) {
        regiao = NormalizaCampos.normalizaRegiao(regiao);
        return repo.findWithFilters(regiao, delegacia);
    }

    public DelegaciasDTO buscarPorId(Long id) {
        return repo.findDTOById(id)
            .orElseThrow(() -> new EntityNotFoundException("Delegacia não encontrada com id: " + id));
    }
}
