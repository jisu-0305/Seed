package org.example.backend.util.fastai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilepathRequest {
    private String diffRaw;
    private String tree;
    private String log;
}