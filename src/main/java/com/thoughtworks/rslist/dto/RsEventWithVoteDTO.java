package com.thoughtworks.rslist.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RsEventWithVoteDTO extends RsEventDTO {
    private int voteNumber;

    public RsEventWithVoteDTO(String eventName, String keyWord, int voteNumber) {
        super(eventName, keyWord);
        this.voteNumber = voteNumber;
    }
}
