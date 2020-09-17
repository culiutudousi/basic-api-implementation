package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;


    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void should_get_user_list() throws Exception {
        userRepository.save(UserPO.builder().name("czc").age(24).gender("male")
                .email("czc@xxx.com").phone("12345678901").votes(10).build());
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
    public void should_register_user() throws Exception {
        User user = new User("Alice", "female", 24, "Alice@xxx.com", "12222222222");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<UserPO> userPOs = (List<UserPO>) userRepository.findAll();
        assertEquals(1, userPOs.size());
        assertEquals("Alice", userPOs.get(0).getName());
        assertEquals(24, userPOs.get(0).getAge());
    }

    @Test
    @Order(3)
    public void should_validate_user_name_format() throws Exception {
        User user = new User("czc123456789", "male", 24, "czc@xxx.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    @Order(4)
    public void should_validate_user_age_format() throws Exception {
        User user = new User("czc", "male", 3, "czc@xxx.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    @Order(5)
    public void should_validate_user_email_format() throws Exception {
        User user = new User("czc", "male", 24, "czc.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    @Order(6)
    public void should_validate_user_phone_format() throws Exception {
        User user = new User("czc", "male", 24, "czc@xxx.com", "123456789012222222");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(user);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }
}
