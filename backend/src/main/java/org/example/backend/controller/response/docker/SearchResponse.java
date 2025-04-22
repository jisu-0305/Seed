package org.example.backend.controller.response.docker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.example.backend.domain.docker.dto.RepositoryDto;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {
    private int count;
    private String next;
    private String previous;
    private List<RepositoryDto> results;
}
