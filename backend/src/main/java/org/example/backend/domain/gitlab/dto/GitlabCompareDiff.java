package org.example.backend.domain.gitlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GitlabCompareDiff {
    @JsonProperty("old_path")
    private String oldPath;

    @JsonProperty("new_path")
    private String newPath;

    @JsonProperty("a_mode")
    private String aMode;

    @JsonProperty("b_mode")
    private String bMode;

    private String diff;

    @JsonProperty("new_file")
    private boolean newFile;

    @JsonProperty("renamed_file")
    private boolean renamedFile;

    @JsonProperty("deleted_file")
    private boolean deletedFile;

    @JsonProperty("generated_file")
    private final Object generatedFile = null;
}
