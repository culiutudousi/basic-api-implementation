package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsListApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void should_get_re_event_list() throws Exception {
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].keyWord", is("no tag")))
                .andExpect(status().isOk());
    }

    @Test
    public void should_get_one_rs_event() throws Exception {
        mockMvc.perform(get("/rs/1"))
                .andExpect(jsonPath("$.eventName", is("1st event")))
                .andExpect(jsonPath("$.keyWord", is("no tag")))
                .andExpect(status().isOk());
    }

    @Test
    public void should_get_rs_event_between() throws Exception {
        mockMvc.perform(get("/rs/list?start=1&end=2"))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list?start=2&end=3"))
                .andExpect(jsonPath("$[0].eventName", is("2ed event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("3rd event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list?start=1&end=3"))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].keyWord", is("no tag")))
                .andExpect(status().isOk());
    }

    @Test
    public void should_add_re_event() throws Exception {
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].keyWord", is("no tag")))
                .andExpect(jsonPath("$[3].eventName", is("pork rise in price")))
                .andExpect(jsonPath("$[3].keyWord", is("economic")))
                .andExpect(status().isOk());
    }
}
