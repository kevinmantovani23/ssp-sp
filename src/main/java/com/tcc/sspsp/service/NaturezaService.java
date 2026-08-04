package com.tcc.sspsp.service;

import com.tcc.sspsp.dto.NaturezaDTO;
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

    public List<NaturezaDTO> listarNaturezas(){

        return repository.findDTOAll();

    }
}
