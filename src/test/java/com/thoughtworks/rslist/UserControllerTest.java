package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    @Order(1)
    public void should_get_user_list() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("czc")))
                .andExpect(jsonPath("$[0].gender", is("male")))
                .andExpect(jsonPath("$[0].age", is(24)))
                .andExpect(jsonPath("$[0].email", is("czc@xxx.com")))
                .andExpect(jsonPath("$[0].phone", is("12345678901")))
                .andExpect(jsonPath("$[0]", not(hasKey("votes"))))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void should_register_user() throws Exception {
        User user = new User("Alice", "female", 24, "Alice@xxx.com", "12222222222");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("index", "2"))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/users"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("czc")))
                .andExpect(jsonPath("$[1].name", is("Alice")))
                .andExpect(jsonPath("$[1].gender", is("female")))
                .andExpect(jsonPath("$[1].age", is(24)))
                .andExpect(jsonPath("$[1].email", is("Alice@xxx.com")))
                .andExpect(jsonPath("$[1].phone", is("12222222222")));
    }

    @Test
    @Order(3)
    public void should_validate_user_name_format() throws Exception {
        User user = new User("czc123456789", "male", 24, "czc@xxx.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void should_validate_user_age_format() throws Exception {
        User user = new User("czc", "male", 3, "czc@xxx.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    public void should_validate_user_email_format() throws Exception {
        User user = new User("czc", "male", 24, "czc.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(6)
    public void should_validate_user_phone_format() throws Exception {
        User user = new User("czc", "male", 24, "czc@xxx.com", "123456789012222222");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
