package com.teago.teago.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.teago.teago.models.LandOwner;
import com.teago.teago.Repositories.LandOwnerRepository;

@Service
public class LandOwnerService {

    private final LandOwnerRepository landOwnerRepository;

    public LandOwnerService(LandOwnerRepository landOwnerRepository) {
        this.landOwnerRepository = landOwnerRepository;
    }

    public List<LandOwner> getAllLandOwners() {
        return landOwnerRepository.findAll();
    }

    public LandOwner getLandOwnerById(Integer id) {
        return landOwnerRepository.findById(id).orElse(null);
    }

    public LandOwner saveLandOwner(LandOwner landOwner) {
        return landOwnerRepository.save(landOwner);
    }

    public void deleteLandOwner(Integer id) {
        landOwnerRepository.deleteById(id);
    }
}