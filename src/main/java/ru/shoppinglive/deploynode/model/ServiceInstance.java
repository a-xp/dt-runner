package ru.shoppinglive.deploynode.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class ServiceInstance {

    protected String code;
    protected String version;
    protected String modifier;
    protected Connector connector;
    protected int pid;
    protected ProcessStats stats;

}
