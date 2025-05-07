package com.telegram.menfess.repository;

import com.telegram.menfess.entity.MenfessData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenfessDataRepository extends JpaRepository<MenfessData, String> {
}
