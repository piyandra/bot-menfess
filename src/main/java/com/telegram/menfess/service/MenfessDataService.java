package com.telegram.menfess.service;

import com.telegram.menfess.entity.MenfessData;
import com.telegram.menfess.repository.MenfessDataRepository;
import org.springframework.stereotype.Service;

@Service
public class MenfessDataService {
    private final MenfessDataRepository menfessDataRepository;

    public MenfessDataService(MenfessDataRepository menfessDataRepository) {
        this.menfessDataRepository = menfessDataRepository;
    }

    public String saveDataMenfess(MenfessData data) {
        MenfessData save = menfessDataRepository.save(data);
        return save.getId();
    }
    public void deleteDataById(String id) {
        menfessDataRepository.deleteById(id);
    }
    public MenfessData findDataById(String id) {
        return menfessDataRepository.findById(id).orElse(null);
    }
}
