package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
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
    UserPO existUserPO;
    List<RsEventPO> existRsEventPOs = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        existUserPO = UserPO.builder().name("czc").age(24).gender("male")
                .email("czc@xxx.com").phone("12345678901").votes(10).build();
        userRepository.save(existUserPO);
        existRsEventPOs.add(RsEventPO.builder().eventName("1st event").keyWord("no tag").userPO(existUserPO).build());
        existRsEventPOs.add(RsEventPO.builder().eventName("2ed event").keyWord("no tag").userPO(existUserPO).build());
        existRsEventPOs.add(RsEventPO.builder().eventName("3rd event").keyWord("no tag").userPO(existUserPO).build());
        existRsEventPOs.forEach(rsEventPO -> rsEventRepository.save(rsEventPO));
    }

    @Test
    public void should_get_re_event_list() throws Exception {
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[0]", not(hasKey("userId"))))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(status().isOk());
    }

    @Test
    public void should_get_one_rs_event() throws Exception {
        mockMvc.perform(get("/rs/" + existRsEventPOs.get(1).getId()))
                .andExpect(jsonPath("$.eventName", is("2ed event")))
                .andExpect(jsonPath("$.keyWord", is("no tag")))
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
        mockMvc.perform(get("/rs/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid index")));
    }

    @Test
    public void should_add_re_event_given_exist_user() throws Exception {
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", existUserPO.getId());
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[3].eventName", is("pork rise in price")))
                .andExpect(jsonPath("$[3].keyWord", is("economic")))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    public void should_add_re_event_and_create_user_given_new_user() throws Exception {
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", 999);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
