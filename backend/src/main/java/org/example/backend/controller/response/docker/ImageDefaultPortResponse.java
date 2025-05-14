package org.example.backend.controller.response.docker;

import java.util.List;

public record ImageDefaultPortResponse(
        String imageNameAndTag,
        List<String> defaultPorts
) {}
