package com.thoughtworks.rslist.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Data
@Builder
@AllArgsConstructor
public class Vote {
    private int voteNum;
    private int userId;
    private String voteTime;

    public void setVoteTime(Date time) {
        SimpleDateFormat formatter = getDateFormatter();
        voteTime = formatter.format(time);
    }

    public Date getVoteTimeDateObject() throws ParseException {
        return getDateFormatter().parse(voteTime);
    }

    private SimpleDateFormat getDateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return formatter;
    }
}
