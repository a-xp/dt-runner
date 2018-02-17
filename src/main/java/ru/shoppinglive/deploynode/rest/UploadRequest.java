package ru.shoppinglive.deploynode.rest;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class UploadRequest {
    @Length(min = 10)
    @NotNull
    private String artifactUrl;

}
