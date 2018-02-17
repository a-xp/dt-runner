package ru.shoppinglive.deploynode.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Connector {
    String ip;
    int port;
}
