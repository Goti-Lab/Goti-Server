package com.goti.user.service.domain.address;

import com.goti.user.domain.entity.user.AddressEntity;
import com.goti.user.domain.entity.user.MemberEntity;

import com.goti.user.dto.response.AddressCreateResponse;
import com.goti.user.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

	private final AddressRepository addressRepository;

	@Override
	@Transactional
	public AddressCreateResponse register(
		String zipCode, String baseAddress, String detailAddress, MemberEntity member
	) {
		AddressEntity address = findAddress(member).map(
			entity -> {
				entity.updateDetails(
					zipCode, baseAddress, detailAddress
				);
				return entity;
			}
		).orElseGet(
			() -> AddressEntity.create(
				zipCode, baseAddress, detailAddress, member
			)
		);
		addressRepository.save(address);
		return AddressCreateResponse.from(address);
	}

	@Override
	public Optional<AddressEntity> findAddress(MemberEntity member) {
		return addressRepository.findByMember(member);
	}
}
