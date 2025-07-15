package com.starline.faq.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FAQItem {

    private String question;
    private String answer;
    private String category;
    private int order;

}
