package ru.shoppinglive.deploynode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptMeta {
    private String version;
    private String modifier;
    private String code;
    private String env;
    private String memory;
    private String args;
}
