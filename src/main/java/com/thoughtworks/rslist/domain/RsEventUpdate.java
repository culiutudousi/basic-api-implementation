package com.thoughtworks.rslist.domain;

public class RsEventUpdate extends RsEvent{
    private Integer index;

    public RsEventUpdate(Integer index, String eventName, String keyWord) {
        super(eventName, keyWord);
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
}
