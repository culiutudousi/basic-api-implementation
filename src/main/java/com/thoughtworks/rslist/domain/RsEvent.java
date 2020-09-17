package com.thoughtworks.rslist.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.thoughtworks.rslist.api.PropertyFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.validation.Valid;

public class RsEvent {

    @JsonView(PropertyFilter.ReEventShowFilter.class)
    private String eventName;
    @JsonView(PropertyFilter.ReEventShowFilter.class)
    private String keyWord;
    private int userId;

    public RsEvent() {
    }

    public RsEvent(String eventName, String keyWord, int userId) {
        this.eventName = eventName;
        this.keyWord = keyWord;
        this.userId = userId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}