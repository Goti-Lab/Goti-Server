package com.goti.domain.base;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
public abstract class BaseUuidEntity implements Persistable<UUID> {
	@Id
	@UuidGenerator
	@Column(name = "id", nullable = false, updatable = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private @Getter UUID id;

	@Override
	public boolean isNew() {
		return id == null;
	}

}