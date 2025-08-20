package org.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResponse {
    @JsonProperty("upload_url")
    String uploadUrl;
}
