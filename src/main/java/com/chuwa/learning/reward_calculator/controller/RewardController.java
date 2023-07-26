package com.chuwa.learning.reward_calculator.controller;

import com.chuwa.learning.reward_calculator.Exception.UserNotExistException;
import com.chuwa.learning.reward_calculator.entity.Transaction;
import com.chuwa.learning.reward_calculator.service.RewardService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@Log4j2
public class RewardController {
    private final RewardService rewardService;
    private final List<Transaction> transactions = new ArrayList<>();

    @Autowired
    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<String> addTransaction(@RequestBody Transaction transaction) {
        transactions.add(transaction);
        log.info("Transaction added: " + transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body("Transaction added successfully");
    }

    @GetMapping("/users/{userId}/reward")
    public ResponseEntity<Double> getRewardPoints(@PathVariable int userId) {
        try {
            double totalRewardPoints = 0.0;
            boolean userFound = false;

            for (Transaction transaction : transactions) {
                if (transaction.getId() == userId) {
                    totalRewardPoints += rewardService.calculateRewardPoints(transaction.getAmount());
                    userFound = true;
                }
            }

            if (!userFound) {
                throw new UserNotExistException("User not found with ID: " + userId);
            }

            log.info("Total reward points calculated for user {}: {}", userId, totalRewardPoints);
            return ResponseEntity.ok(totalRewardPoints);
        } catch (UserNotExistException e) {
            log.error("UserNotExistException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error occurred while calculating reward points for user {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
