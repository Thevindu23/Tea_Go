package com.teago.teago.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teago.teago.models.FactoryOwner;
import com.teago.teago.Repositories.FactoryOwnerRepository;

@Service
public class FactoryOwnerService {

    private final FactoryOwnerRepository factoryOwnerRepository;

    public FactoryOwnerService(FactoryOwnerRepository factoryOwnerRepository) {
        this.factoryOwnerRepository = factoryOwnerRepository;
    }

    public List<FactoryOwner> getAllFactoryOwners() {
        return factoryOwnerRepository.findAll();
    }

    public FactoryOwner getFactoryOwnerById(Integer id) {
        return factoryOwnerRepository.findById(id).orElse(null);
    }

    public FactoryOwner saveFactoryOwner(FactoryOwner factoryOwner) {
        return factoryOwnerRepository.save(factoryOwner);
    }

    public void deleteFactoryOwner(Integer id) {
        factoryOwnerRepository.deleteById(id);
    }
}