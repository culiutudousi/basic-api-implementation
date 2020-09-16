package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    public void should_get_re_event_list() throws Exception {
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[0]", not(hasKey("user"))))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1]", not(hasKey("user"))))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].keyWord", is("no tag")))
                .andExpect(jsonPath("$[2]", not(hasKey("user"))))
                .andExpect(status().isOk());
    }

    @Test
    public void should_get_one_rs_event() throws Exception {
        mockMvc.perform(get("/rs/1"))
                .andExpect(jsonPath("$.eventName", is("1st event")))
                .andExpect(jsonPath("$.keyWord", is("no tag")))
                .andExpect(jsonPath("$", not(hasKey("user"))))
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
    @DirtiesContext
    public void should_add_re_event_given_exist_user() throws Exception {
        User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", user);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("index", "4"))
                .andExpect(status().isCreated());
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
        mockMvc.perform(get("/user"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("czc")));
    }

    @Test
    @DirtiesContext
    public void should_add_re_event_and_create_user_given_new_user() throws Exception {
        User user = new User("Alice", "female", 24, "alice@xxx.com", "1222222222");
        RsEvent rsEvent = new RsEvent("pork rise in price", "economic", user);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(post("/rs/event").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
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
        mockMvc.perform(get("/user"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("czc")))
                .andExpect(jsonPath("$[1].name", is("Alice")));
    }

    @Test
    @DirtiesContext
    public void should_update_re_event_given_only_event_name() throws Exception {
        User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
        RsEvent rsEvent = new RsEvent("1st event updated", null, user);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/1").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event updated")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].keyWord", is("no tag")))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    public void should_update_re_event_given_only_key_word() throws Exception {
        User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
        RsEvent rsEvent = new RsEvent(null, "education", user);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/2").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("education")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event")))
                .andExpect(jsonPath("$[2].keyWord", is("no tag")))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    public void should_update_re_event_given_both_event_name_and_key_word() throws Exception {
        User user = new User("czc", "male", 24, "czc@xxx.com", "12345678901");
        RsEvent rsEvent = new RsEvent("3rd event updated", "industry", user);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(rsEvent);
        mockMvc.perform(patch("/rs/event/3").content(jsonString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("2ed event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(jsonPath("$[2].eventName", is("3rd event updated")))
                .andExpect(jsonPath("$[2].keyWord", is("industry")))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext
    public void should_delete_re_event() throws Exception {
        mockMvc.perform(delete("/rs/event/2"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("1st event")))
                .andExpect(jsonPath("$[0].keyWord", is("no tag")))
                .andExpect(jsonPath("$[1].eventName", is("3rd event")))
                .andExpect(jsonPath("$[1].keyWord", is("no tag")))
                .andExpect(status().isOk());
    }
}
