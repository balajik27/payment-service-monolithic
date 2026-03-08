package com.balaji.payment.reward.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.balaji.payment.reward.service.RewardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    // REST endpoints for rewards can be added here
}
