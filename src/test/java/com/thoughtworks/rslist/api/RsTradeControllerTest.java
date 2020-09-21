package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.RsTradePO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.RsTradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import com.thoughtworks.rslist.service.RsTradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RsTradeControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    RsTradeRepository rsTradeRepository;
    @Autowired
    RsTradeService rsTradeService;

    List<UserPO> existUserPOs;
    List<RsEventPO> existRsEventPOs;
    List<VotePO> existVotePOs;
    List<RsTradePO> existRsTradePOs;
    SimpleDateFormat formatter;

    @BeforeEach
    public void setUp() throws ParseException {
        rsTradeRepository.deleteAll();
        voteRepository.deleteAll();
        userRepository.deleteAll();
        rsEventRepository.deleteAll();

        existUserPOs = new ArrayList<>();
        existRsEventPOs = new ArrayList<>();
        existVotePOs = new ArrayList<>();

        UserPO firstUserPO = UserPO.builder().name("czc").age(24).gender("male").email("czc@xxx.com").phone("12345678901").leftVoteNumber(10).build();
        existUserPOs.add(firstUserPO);
        existUserPOs.forEach(userPO -> userRepository.save(userPO));

        existRsEventPOs.add(RsEventPO.builder().eventName("1st event").keyWord("no tag").userPO(existUserPOs.get(0)).build());
        existRsEventPOs.add(RsEventPO.builder().eventName("2ed event").keyWord("no tag").userPO(existUserPOs.get(0)).build());
        existRsEventPOs.add(RsEventPO.builder().eventName("3rd event").keyWord("no tag").userPO(existUserPOs.get(0)).build());
        existRsEventPOs.forEach(rsEventPO -> rsEventRepository.save(rsEventPO));

        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        existVotePOs.add(VotePO.builder().userPO(existUserPOs.get(0)).rsEventPO(existRsEventPOs.get(0)).voteNum(1).voteTime(formatter.parse("2020-09-18 00:11:11")).build());
        existVotePOs.add(VotePO.builder().userPO(existUserPOs.get(0)).rsEventPO(existRsEventPOs.get(1)).voteNum(2).voteTime(formatter.parse("2020-09-18 00:22:22")).build());
        existVotePOs.add(VotePO.builder().userPO(existUserPOs.get(0)).rsEventPO(existRsEventPOs.get(2)).voteNum(3).voteTime(formatter.parse("2020-09-18 00:33:33")).build());
        existVotePOs.forEach(votePO -> voteRepository.save(votePO));
    }

    @Test
    public void should_trade_given_rank_never_bought() throws Exception {
        mockMvc.perform(post("/rs/trade")
                .param("userId", String.valueOf(existUserPOs.get(0).getId()))
                .param("rsEventId", String.valueOf(existRsEventPOs.get(0).getId()))
                .param("amount", String.valueOf(2))
                .param("rank", String.valueOf(1)))
                .andExpect(status().isOk());
        List<RsTradePO> rsTradeResult = (List<RsTradePO>) rsTradeRepository.findAll();
        assertEquals(1, rsTradeResult.size());
        assertEquals(existRsEventPOs.get(0).getId(), rsTradeResult.get(0).getRsEventPO().getId());
        assertEquals(2, rsTradeResult.get(0).getAmount());
        assertEquals(1, rsTradeResult.get(0).getRank());
    }

    @Test
    public void should_delete_old_event_when_trade_given_larger_amount() throws Exception {
        int oldRsEventId = existRsEventPOs.get(0).getId();
        mockMvc.perform(post("/rs/trade")
                .param("userId", String.valueOf(existUserPOs.get(0).getId()))
                .param("rsEventId", String.valueOf(oldRsEventId))
                .param("amount", String.valueOf(2))
                .param("rank", String.valueOf(1)))
                .andExpect(status().isOk());
        List<RsTradePO> rsTradeResult = (List<RsTradePO>) rsTradeRepository.findAll();
        assertEquals(1, rsTradeResult.size());
        assertEquals(existRsEventPOs.get(0).getId(), rsTradeResult.get(0).getRsEventPO().getId());
        assertEquals(2, rsTradeResult.get(0).getAmount());
        assertEquals(1, rsTradeResult.get(0).getRank());
        mockMvc.perform(post("/rs/trade")
                .param("userId", String.valueOf(existUserPOs.get(0).getId()))
                .param("rsEventId", String.valueOf(existRsEventPOs.get(1).getId()))
                .param("amount", String.valueOf(3))
                .param("rank", String.valueOf(1)))
                .andExpect(status().isOk());
        rsTradeResult = (List<RsTradePO>) rsTradeRepository.findAll();
        assertEquals(1, rsTradeResult.size());
        assertEquals(existRsEventPOs.get(1).getId(), rsTradeResult.get(0).getRsEventPO().getId());
        assertEquals(3, rsTradeResult.get(0).getAmount());
        assertEquals(1, rsTradeResult.get(0).getRank());
        assertFalse(rsEventRepository.findById(oldRsEventId).isPresent());
    }

    @Test
    public void should_return_bad_request_when_trade_given_larger_amount() throws Exception {
        mockMvc.perform(post("/rs/trade")
                .param("userId", String.valueOf(existUserPOs.get(0).getId()))
                .param("rsEventId", String.valueOf(existRsEventPOs.get(0).getId()))
                .param("amount", String.valueOf(5))
                .param("rank", String.valueOf(1)))
                .andExpect(status().isOk());
        List<RsTradePO> rsTradeResult = (List<RsTradePO>) rsTradeRepository.findAll();
        assertEquals(1, rsTradeResult.size());
        assertEquals(existRsEventPOs.get(0).getId(), rsTradeResult.get(0).getRsEventPO().getId());
        assertEquals(5, rsTradeResult.get(0).getAmount());
        assertEquals(1, rsTradeResult.get(0).getRank());
        mockMvc.perform(post("/rs/trade")
                .param("userId", String.valueOf(existUserPOs.get(0).getId()))
                .param("rsEventId", String.valueOf(existRsEventPOs.get(1).getId()))
                .param("amount", String.valueOf(3))
                .param("rank", String.valueOf(1)))
                .andExpect(status().isBadRequest());
        rsTradeResult = (List<RsTradePO>) rsTradeRepository.findAll();
        assertEquals(1, rsTradeResult.size());
    }
}
