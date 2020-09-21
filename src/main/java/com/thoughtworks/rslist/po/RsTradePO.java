package com.thoughtworks.rslist.po;

import com.thoughtworks.rslist.domain.RsEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RsTrade")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RsTradePO {
    @Id
    @GeneratedValue
    private int id;
    private int amount;
    @Column(name = "listRank")
    private int rank;
    @ManyToOne
    private RsEventPO rsEventPO;
}
