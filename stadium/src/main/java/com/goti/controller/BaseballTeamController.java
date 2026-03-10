package com.goti.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "BaseballTeam", description = "야구구단(팀) 관련 API")
@RestController
@RequestMapping("/api/v1/baseball-teams")
@RequiredArgsConstructor
public class BaseballTeamController {
}
