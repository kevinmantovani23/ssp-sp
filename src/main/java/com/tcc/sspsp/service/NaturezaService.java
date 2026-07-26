package com.tcc.sspsp.service;

import com.tcc.sspsp.model.Natureza;
import com.tcc.sspsp.repository.NaturezaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NaturezaService {


    private final NaturezaRepository repository;

    public List<Natureza> listarNaturezas(){

        return repository.findAll();

    }
}
