package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySign {

    private Integer id;
    private Integer activityId;
    private String activityName;
    private Integer userId;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date signTime;

}
