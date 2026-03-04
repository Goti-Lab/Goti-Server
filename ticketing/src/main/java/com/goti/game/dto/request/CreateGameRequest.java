package com.goti.game.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.constants.LeagueType;
import com.goti.game.service.command.CreateGameCommand;

import jakarta.validation.constraints.NotNull;

public record CreateGameRequest(
	@NotNull(message = "홈팀 ID는 필수입니다.")
	UUID homeTeamId,

	@NotNull(message = "원정팀 ID는 필수입니다.")
	UUID awayTeamId,

	@NotNull(message = "구장 ID는 필수입니다.")
	UUID stadiumId,

	@NotNull(message = "경기 날짜는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate playDate,

	@NotNull(message = "경기 시작 시간은 필수입니다.")
	@JsonFormat(pattern = "HH:mm:ss")
	LocalTime startAt,

	@NotNull(message = "리그 종류는 필수입니다.")
	LeagueType leagueType,

	@NotNull(message = "예매 오픈 일시는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime reservationOpenedAt,

	@NotNull(message = "예매 종료 일시는 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	LocalDateTime reservationClosedAt

) {
	public CreateGameCommand toCommand() {
		return new CreateGameCommand(
			homeTeamId,
			awayTeamId,
			stadiumId,
			playDate,
			startAt,
			leagueType,
			reservationOpenedAt,
			reservationClosedAt
		);
	}
}
