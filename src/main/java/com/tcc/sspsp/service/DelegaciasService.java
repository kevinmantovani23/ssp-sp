package com.tcc.sspsp.service;

import com.tcc.sspsp.model.Delegacias;
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

    public List<Delegacias> listarComFiltros(String regiao, String delegacia, Integer idSSP) {
        regiao = NormalizaCampos.normalizaRegiao(regiao);
        return repo.findWithFilters(regiao, delegacia, idSSP);
    }

    public Delegacias buscarPorId(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Delegacia não encontrada com id: " + id));
    }
}
