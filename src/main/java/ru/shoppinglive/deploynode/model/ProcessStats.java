package ru.shoppinglive.deploynode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessStats {
    private long uptime;
    private float cpu;
    private long mem;
}
