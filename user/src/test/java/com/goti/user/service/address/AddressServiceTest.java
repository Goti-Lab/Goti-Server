package com.goti.user.service.address;

import com.goti.constants.Gender;
import com.goti.user.GotiUserApplication;

import com.goti.user.domain.entity.user.AddressEntity;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AddressCreateResponse;
import com.goti.user.repository.AddressRepository;
import com.goti.user.repository.MemberRepository;
import com.goti.user.service.domain.address.AddressService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest(classes = GotiUserApplication.class)
@ActiveProfiles("test")
public class AddressServiceTest {

	@Autowired AddressService addressService;
	@Autowired MemberRepository memberRepository;
	@Autowired AddressRepository addressRepository;

	MemberEntity member;

	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.MALE,
			LocalDate.of(2026, 2, 4)
		);
		memberRepository.save(member);
	}

	@Test
	void 주소_생성_성공() {
		AddressCreateResponse response = addressService.register(
			"06111",
			"서울특별시 강남구 학동로 343",
			"(논현동, 포바강남타워) 4층, 15층",
			member
		);
		assertNotNull(response);
		assertNotNull(response.addressId());
		AddressEntity address = addressService.findAddress(member).orElse(null);
		assertNotNull(address);
		assertEquals(response.addressId(), address.getId());
		log.info("response addressId : {}", response.addressId());
		log.info("address getId : {}", address.getId());
	}

	@Test
	void 주소_생성_성공_기존_주소_존재() {
		AddressEntity address = AddressEntity.create(
			"06111",
			"서울특별시 강남구 학동로 343",
			"(논현동, 포바강남타워) 4층, 15층",
			member
		);
		addressRepository.save(address);

		AddressCreateResponse response = addressService.register(
			"123123",
			"서울특별시 관악구 낙성대역 8길 50-4",
			"104호",
			member
		);
		assertNotNull(response);
		assertEquals(address.getId(), response.addressId());
		log.info("address getId :: {}", address.getId());
		log.info("response addressId :: {}", response.addressId());
		assertEquals("123123", address.getZipCode());
		assertEquals("서울특별시 관악구 낙성대역 8길 50-4", address.getBaseAddress());
		assertEquals("104호", address.getDetailAddress());
		AddressEntity savedAddress = addressService.findAddress(member).orElse(null);
		assertNotNull(savedAddress);
	}
}
