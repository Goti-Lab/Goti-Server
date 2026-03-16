package com.goti.domain.base;

import java.time.Instant;

import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
public abstract class ModificationTimestampEntity extends CreationTimestampEntity {

	@LastModifiedDate
	@Column(nullable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private @Getter Instant updatedAt;

}