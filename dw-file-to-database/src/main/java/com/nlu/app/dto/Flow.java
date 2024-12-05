package com.nlu.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Flow {
    private String fileName;
    private String[] flow;
    private String message;
}
