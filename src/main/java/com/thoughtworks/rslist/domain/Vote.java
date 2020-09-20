package com.thoughtworks.rslist.domain;

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
@NoArgsConstructor
public class Vote {
    private int voteNum;
    private int userId;
    private Date voteTime;
}
