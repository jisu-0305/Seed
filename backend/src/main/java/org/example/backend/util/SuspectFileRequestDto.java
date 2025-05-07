package org.example.backend.util;

import lombok.*;

@Getter
@Setter
public class SuspectFileRequestDto {
    private String diffRaw;
    private String tree;
    private String log;
}