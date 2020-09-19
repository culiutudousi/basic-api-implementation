package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Vote;
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
import java.util.*;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RsEventRepository rsEventRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    VoteRepository voteRepository;

    List<UserPO> existUserPOs = new ArrayList<>();
    List<RsEventPO> existRsEventPOs = new ArrayList<>();
    List<VotePO> existVotePOs = new ArrayList<>();

    SimpleDateFormat formatter;

    @BeforeEach
    public void setUp() throws ParseException {
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        voteRepository.deleteAll();

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
    public void should_get_re_event_list() throws Exception {
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[0].voteNumber", is(1)))
                .andExpect(jsonPath("$[0]", not(hasKey("userId"))))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].voteNumber", is(2)))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].voteNumber", is(3)))
                .andExpect(status().isOk());
    }

    @Test
    public void should_get_one_rs_event() throws Exception {
        mockMvc.perform(get("/rs/" + existRsEventPOs.get(1).getId()))
                .andExpect(jsonPath("$.eventName", is("2ed event")))
                .andExpect(jsonPath("$.keyWord", is("no tag")))
                .andExpect(jsonPath("$.voteNumber", is(2)))
                .andExpect(jsonPath("$", not(hasKey("userId"))))
                .andExpect(status().isOk());
    }

    @Test
    public void should_return_error_message_when_get_rs_event_between_given_start_out_of_range() throws Exception {
        mockMvc.perform(get("/rs/list?start=-3&end=2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid request param")));
    }

    @Test
    public void should_return_error_message_when_get_rs_event_between_given_start_is_larger_than_end() throws Exception {
        mockMvc.perform(get("/rs/list?start=2&end=1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid request param")));
    }

    @Test
    public void should_return_error_message_when_get_rs_event_given_index_out_of_range() throws Exception {
        mockMvc.perform(get("/rs/99999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid index")));
    }

    @Test
    public void should_add_rs_event_given_exist_user() throws Exception {
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", existUserPOs.get(0).getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<RsEventPO> rsEventPOResults = (List<RsEventPO>) rsEventRepository.findAll();
        assertEquals(4, rsEventPOResults.size());
        assertEquals("1st event", rsEventPOResults.get(0).getEventName());
        assertEquals("2ed event", rsEventPOResults.get(1).getEventName());
        assertEquals("3rd event", rsEventPOResults.get(2).getEventName());
        assertEquals("pork rise in price", rsEventPOResults.get(3).getEventName());
        assertEquals("economic", rsEventPOResults.get(3).getKeyWord());
    }

    @Test
    public void should_return_bad_request_when_add_re_event_given_user_not_exist() throws Exception {
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", 99999);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_update_rs_event_given_related_user() throws Exception {
        int rsEventId = existRsEventPOs.get(1).getId();
        int userId = existRsEventPOs.get(1).getUserPO().getId();
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", userId);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        RsEventPO rsEventPO = rsEventRepository.findById(rsEventId).get();
        assertEquals("pork rise in price", rsEventPO.getEventName());
        assertEquals("economic", rsEventPO.getKeyWord());
    }

    @Test
    public void should_return_bad_request_when_update_rs_event_given_not_related_user() throws Exception {
        UserPO newUserPO = UserPO.builder().name("czc").age(24).gender("male").email("czc@xxx.com").phone("12345678901").leftVoteNumber(10).build();
        userRepository.save(newUserPO);
        int existRsEventId = existRsEventPOs.get(1).getId();
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", newUserPO.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/" + existRsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_only_update_event_name_rs_event_when_update_rs_event_given_keyword_of_null() throws Exception {
        int rsEventId = existRsEventPOs.get(1).getId();
        int userId = existRsEventPOs.get(1).getUserPO().getId();
        RsEvent rsEvent = new RsEvent("pork rise in price", null, userId);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        RsEventPO rsEventPO = rsEventRepository.findById(rsEventId).get();
        assertEquals("pork rise in price", rsEventPO.getEventName());
        assertEquals("no tag", rsEventPO.getKeyWord());
    }

    @Test
    public void should_only_update_keyword_rs_event_when_update_rs_event_given_event_name_of_null() throws Exception {
        int rsEventId = existRsEventPOs.get(1).getId();
        int userId = existRsEventPOs.get(1).getUserPO().getId();
        RsEvent rsEvent = new RsEvent(null, "economic", userId);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        RsEventPO rsEventPO = rsEventRepository.findById(rsEventId).get();
        assertEquals("2ed event", rsEventPO.getEventName());
        assertEquals("economic", rsEventPO.getKeyWord());
    }

    @Test
    public void should_vote_given_vote_number_less_than_user_has() throws Exception {
        RsEventPO rsEventPO = existRsEventPOs.get(1);
        int rsEventId = rsEventPO.getId();
        int userId = rsEventPO.getUserPO().getId();
        VoteDTO voteDTO = new VoteDTO(2, userId, "2020-09-18 00:18:27");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(voteDTO);
        mockMvc.perform(post("/rs/vote/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
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
        mockMvc.perform(post("/rs/vote/" + rsEventId).content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        List<VotePO> votesResult = (List<VotePO>) voteRepository.findAll();
        assertEquals(3, votesResult.size());
    }

    @Test
    public void should_delete_rs_event() throws Exception {
        int rsEventId = existRsEventPOs.get(1).getId();
        mockMvc.perform(delete("/rs/event/" + rsEventId))
                .andExpect(status().isOk());
        List<RsEventPO> rsEventPOResults = (List<RsEventPO>) rsEventRepository.findAll();
        assertEquals(2, rsEventPOResults.size());
        assertEquals("1st event", rsEventPOResults.get(0).getEventName());
        assertEquals("3rd event", rsEventPOResults.get(1).getEventName());
    }
}
