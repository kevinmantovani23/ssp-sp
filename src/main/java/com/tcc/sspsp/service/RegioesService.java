package com.tcc.sspsp.service;


import com.tcc.sspsp.repository.DelegaciasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegioesService {

    private final DelegaciasRepository repository;

    public List<String> listarRegioes(){
       return repository.listarRegioes();
    }
}
