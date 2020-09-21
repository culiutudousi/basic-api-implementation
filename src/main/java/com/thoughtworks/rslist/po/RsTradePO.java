package com.thoughtworks.rslist.po;

import com.thoughtworks.rslist.domain.RsEvent;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RsTrade")
@Data
@EqualsAndHashCode(callSuper=false)
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RsTradePO && this.id == ((RsTradePO) obj).getId();
    }
}
