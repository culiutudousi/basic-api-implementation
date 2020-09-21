package com.thoughtworks.rslist.po;

import com.thoughtworks.rslist.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "RsEvent")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RsEventPO {
    @Id
    @GeneratedValue
    private int id;
    private String eventName;
    private String keyWord;
    @ManyToOne
    private UserPO userPO;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "rsEventPO")
    private List<VotePO> votePOs;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "rsEventPO")
    private List<RsTradePO> rsTradePOS;
}
