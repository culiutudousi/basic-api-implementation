package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.po.RsEventPO;
import com.thoughtworks.rslist.po.UserPO;
import com.thoughtworks.rslist.po.VotePO;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class VoteServiceTest {
    VoteService voteService;
    @Mock
    RsEventRepository rsEventRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VoteRepository voteRepository;

    @BeforeEach
    void setUp() {
        initMocks(this);
        voteService = new VoteService(rsEventRepository, userRepository, voteRepository);
    }

    @Test
    public void should_vote_successfully() {
        UserPO userPO = UserPO.builder()
                .id(1)
                .name("czc")
                .age(24)
                .gender("male")
                .email("czc@xxx.com")
                .phone("18888888888")
                .leftVoteNumber(10)
                .build();
        RsEventPO rsEventPO = RsEventPO.builder()
                .id(1)
                .eventName("1st event")
                .keyWord("1st keyword")
                .build();
        VotePO votePO = VotePO.builder()
                .id(0)
                .userPO(userPO)
                .rsEventPO(rsEventPO)
                .voteNum(3)
                .voteTime(new Date(2020, Calendar.SEPTEMBER, 20, 9, 0, 0))
                .build();
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userPO));
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventPO));
        voteService.vote(rsEventPO.getId(), Vote.builder()
                .userId(userPO.getId())
                .voteNum(votePO.getVoteNum())
                .voteTime(new Date(2020, Calendar.SEPTEMBER, 20, 9, 0, 0))
                .build()
        );
        verify(userRepository).save(userPO);
        verify(voteRepository).save(votePO);
    }
}
