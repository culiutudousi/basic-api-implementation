package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.dto.UserDTO;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;

    List<UserPO> existUserPOs = new ArrayList<>();
    List<RsEventPO> existRsEventPOs = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        rsEventRepository.deleteAll();
        userRepository.deleteAll();

        existUserPOs.add(UserPO.builder().name("czc").age(24).gender("male").email("czc@xxx.com").phone("12345678901").leftVoteNumber(10).build());
        existUserPOs.forEach(userPO -> userRepository.save(userPO));

        existRsEventPOs.add(RsEventPO.builder().eventName("1st event").keyWord("no tag").userPO(existUserPOs.get(0)).build());
        existRsEventPOs.add(RsEventPO.builder().eventName("2ed event").keyWord("no tag").userPO(existUserPOs.get(0)).build());
        existRsEventPOs.add(RsEventPO.builder().eventName("3rd event").keyWord("no tag").userPO(existUserPOs.get(0)).build());
        existRsEventPOs.forEach(rsEventPO -> rsEventRepository.save(rsEventPO));
    }

    @Test
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
    public void should_register_user() throws Exception {
        UserDTO userDTO = new UserDTO("Alice", "female", 24, "Alice@xxx.com", "12222222222");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(userDTO);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<UserPO> userPOs = (List<UserPO>) userRepository.findAll();
        assertEquals(2, userPOs.size());
        assertEquals("Alice", userPOs.get(1).getName());
        assertEquals(24, userPOs.get(1).getAge());
    }

    @Test
    public void should_validate_user_name_format() throws Exception {
        UserDTO userDTO = new UserDTO("czc123456789", "male", 24, "czc@xxx.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(userDTO);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    public void should_validate_user_age_format() throws Exception {
        UserDTO userDTO = new UserDTO("czc", "male", 3, "czc@xxx.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(userDTO);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    public void should_validate_user_email_format() throws Exception {
        UserDTO userDTO = new UserDTO("czc", "male", 24, "czc.com", "12345678901");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(userDTO);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    public void should_validate_user_phone_format() throws Exception {
        UserDTO userDTO = new UserDTO("czc", "male", 24, "czc@xxx.com", "123456789012222222");
        ObjectMapper objectMapper = new ObjectMapper();
        String userString = objectMapper.writeValueAsString(userDTO);
        mockMvc.perform(post("/user").content(userString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid user")));
    }

    @Test
    public void should_get_user_information_given_exist_user_id() throws Exception {
        mockMvc.perform(get("/user/" + existUserPOs.get(0).getId()))
                .andExpect(jsonPath("$.name", is("czc")))
                .andExpect(jsonPath("$.age", is(24)))
                .andExpect(status().isOk());
    }

    @Test
    public void should_return_bad_request_when_get_user_information_given_not_exist_user_id() throws Exception {
        mockMvc.perform(get("/user/99999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void should_delete_user_with_related_rs_event_given_exist_user_id() throws Exception {
        int userId = existUserPOs.get(0).getId();
        mockMvc.perform(delete("/user/" + userId))
                .andExpect(status().isOk());
        assertFalse(userRepository.findById(userId).isPresent());
        assertEquals(0, ((List<RsEventPO>) rsEventRepository.findAll()).size());
    }

    @Test
    public void should_return_bad_request_when_delete_user_given_not_exist_user_id() throws Exception {
        mockMvc.perform(delete("/user/99999"))
                .andExpect(status().isBadRequest());
    }
}
