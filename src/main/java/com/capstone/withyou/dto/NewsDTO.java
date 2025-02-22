package com.capstone.withyou.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NewsDTO {
    private String title;
    private String link;
    private String summary;
    private String press;
    private String date;
    private String imageUrl;
    private String predictedResult;
}
