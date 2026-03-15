package com.goti.repository;

import com.goti.domain.entity.stadium.HomeStadiumEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HomeStadiumRepository extends JpaRepository<HomeStadiumEntity, UUID> {
}
