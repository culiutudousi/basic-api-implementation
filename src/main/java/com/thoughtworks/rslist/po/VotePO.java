package com.thoughtworks.rslist.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "Vote")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VotePO implements Serializable {

    @Id
    @GeneratedValue
    int id;

    @ManyToOne
    @JoinColumn(name = "UserId")
    UserPO userPO;

    @ManyToOne
    @JoinColumn(name = "RsEventId")
    RsEventPO rsEventPO;

    int voteNum;

    Date voteTime;
}
