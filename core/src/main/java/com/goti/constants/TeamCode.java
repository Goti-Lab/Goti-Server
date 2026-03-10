package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TeamCode {
	LG("LG Twins"),
	DO("Doosan Bears"),
	KIW("Kiwoom Heroes"),
	HH("Hanwha Eagles"),
	KT("KT Wiz"),
	SSG("SSG Landers"),
	KIA("KIA Tigers"),
	LOT("Lotte Giants"),
	NC("NC Dinos"),
	SS("Samsung Lions")
	;

	private final String description;
}