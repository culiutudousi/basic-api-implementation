package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.RsListApplication;
import com.thoughtworks.rslist.component.Error;
import com.thoughtworks.rslist.domain.RsTrade;
import com.thoughtworks.rslist.exception.RsTradeNotValidException;
import com.thoughtworks.rslist.exception.VoteNotValidException;
import com.thoughtworks.rslist.service.RsTradeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;

@Controller
public class RsTradeController {
    Logger logger = RsListApplication.logger;

    @Autowired
    RsTradeService rsTradeService;

    @PostMapping("/rs/trade")
    public void addTrade(@RequestParam int userId, @RequestParam int rsEventId,
                         @RequestParam int amount, @RequestParam int rank) {
        rsTradeService.addTrade(RsTrade.builder()
                .userId(userId)
                .rsEventId(rsEventId)
                .amount(amount)
                .rank(rank)
                .build());
    }

    @ExceptionHandler({RsTradeNotValidException.class, ParseException.class})
    public ResponseEntity voteExceptionHandler(Exception exception) {
        String errorMessage = exception.getMessage();
        logger.error(errorMessage);
        return ResponseEntity.badRequest().body(new Error(errorMessage));
    }
}
