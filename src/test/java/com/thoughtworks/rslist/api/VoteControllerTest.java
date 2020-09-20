package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.dto.DateRangeDTO;
import com.thoughtworks.rslist.dto.VoteDTO;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VoteControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    VoteRepository voteRepository;

    List<UserPO> existUserPOs;
    List<RsEventPO> existRsEventPOs;
    List<VotePO> existVotePOs;
    SimpleDateFormat formatter;

    @BeforeEach
    public void setUp() throws ParseException {
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        voteRepository.deleteAll();
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
        firstUserPO.setLeftVoteNumber(firstUserPO.getLeftVoteNumber() - 6);
        userRepository.save(firstUserPO);
    }

    @Test
    public void should_vote_given_vote_number_less_than_user_has() throws Exception {
        RsEventPO rsEventPO = existRsEventPOs.get(1);
        int rsEventId = rsEventPO.getId();
        int userId = rsEventPO.getUserPO().getId();
        VoteDTO voteDTO = new VoteDTO(2, userId, "2020-09-18 00:18:27");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(voteDTO);
        mockMvc.perform(post("/vote/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        List<VotePO> votesResult = (List<VotePO>) voteRepository.findAll();
        assertEquals(4, votesResult.size());
        assertEquals(2, votesResult.get(3).getVoteNum());
        assertEquals("2020-09-18 00:18:27", formatter.format(votesResult.get(3).getVoteTime()));
        assertEquals(userId, votesResult.get(3).getUserPO().getId());
        assertEquals(rsEventId, votesResult.get(3).getRsEventPO().getId());
        assertEquals(2, userRepository.findById(userId).get().getLeftVoteNumber());
    }

    @Test
    public void should_return_bad_request_when_vote_given_vote_number_larger_than_user_has() throws Exception {
        int rsEventId = existRsEventPOs.get(1).getId();
        int userId = existRsEventPOs.get(1).getUserPO().getId();
        VoteDTO voteDTO = new VoteDTO(12, userId, "2020-09-18 00:18:27");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(voteDTO);
        mockMvc.perform(post("/vote/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        List<VotePO> votesResult = (List<VotePO>) voteRepository.findAll();
        assertEquals(3, votesResult.size());
    }

    @Test
    public void should_return_vote_record_by_page() throws Exception {
        voteRepository.deleteAll();
        existVotePOs.clear();
        UserPO userPO = existUserPOs.get(0);
        RsEventPO rsEventPO = existRsEventPOs.get(0);
        for (int i = 0; i < 8; i++) {
            VotePO votePO = VotePO.builder().userPO(userPO).rsEventPO(rsEventPO).voteNum(i + 1).voteTime(formatter.parse("2020-09-18 20:20:20")).build();
            existVotePOs.add(votePO);
            voteRepository.save(votePO);
        }
        mockMvc.perform(get("/vote")
                .param("userId", String.valueOf(userPO.getId()))
                .param("rsEventId", String.valueOf(rsEventPO.getId()))
                .param("pageIndex", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].userId", is(userPO.getId())))
                .andExpect(jsonPath("$[0].voteNum", is(1)))
                .andExpect(jsonPath("$[0].voteTime", is("2020-09-18 20:20:20")))
                .andExpect(jsonPath("$[1].userId", is(userPO.getId())))
                .andExpect(jsonPath("$[1].voteNum", is(2)))
                .andExpect(jsonPath("$[0].voteTime", is("2020-09-18 20:20:20")))
                .andExpect(jsonPath("$[2].voteNum", is(3)))
                .andExpect(jsonPath("$[3].voteNum", is(4)))
                .andExpect(jsonPath("$[4].voteNum", is(5)));
    }

    @Test
    public void should_return_vote_record_in_time_range() throws Exception {
        voteRepository.deleteAll();
        existVotePOs.clear();
        UserPO userPO = existUserPOs.get(0);
        RsEventPO rsEventPO = existRsEventPOs.get(0);
        for (int i = 0; i < 8; i++) {
            VotePO votePO = VotePO.builder().userPO(userPO).rsEventPO(rsEventPO).voteNum(i + 1)
                    .voteTime(formatter.parse(String.format("2020-09-%02d 20:20:20", i + 1))).build();
            existVotePOs.add(votePO);
            voteRepository.save(votePO);
        }
        DateRangeDTO dateRangeDTO = new DateRangeDTO("2020-09-02 08:00:00", "2020-09-04 23:00:00");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(dateRangeDTO);
        mockMvc.perform(get("/vote/between").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].userId", is(userPO.getId())))
                .andExpect(jsonPath("$[0].voteNum", is(2)))
                .andExpect(jsonPath("$[0].voteTime", is("2020-09-02 20:20:20")))
                .andExpect(jsonPath("$[1].userId", is(userPO.getId())))
                .andExpect(jsonPath("$[1].voteNum", is(3)))
                .andExpect(jsonPath("$[1].voteTime", is("2020-09-03 20:20:20")))
                .andExpect(jsonPath("$[2].voteNum", is(4)));
    }
}
