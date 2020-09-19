package com.thoughtworks.rslist.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.thoughtworks.rslist.api.PropertyFilter;
import com.thoughtworks.rslist.domain.RsEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RsEventDto extends RsEvent {
    @JsonView(PropertyFilter.ReEventShowFilter.class)
    private int voteNumber;

    public RsEventDto(String eventName, String keyWord, int userId, int voteNumber) {
        super(eventName, keyWord, userId);
        this.voteNumber = voteNumber;
    }

    public int getVoteNumber() {
        return voteNumber;
    }

    public void setVoteNumber(int voteNumber) {
        this.voteNumber = voteNumber;
    }
}
