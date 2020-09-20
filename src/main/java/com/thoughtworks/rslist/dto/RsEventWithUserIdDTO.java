package com.thoughtworks.rslist.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper=false)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RsEventWithUserIdDTO extends RsEventDTO {
    private int userId;

    public RsEventWithUserIdDTO(String eventName, String keyWord, int userId) {
        super(eventName, keyWord);
        this.userId = userId;
    }
}
