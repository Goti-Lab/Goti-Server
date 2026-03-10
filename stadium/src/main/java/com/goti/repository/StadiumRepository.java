package com.goti.repository;

import com.goti.domain.entity.stadium.StadiumEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StadiumRepository extends JpaRepository<StadiumEntity, UUID> {
}
