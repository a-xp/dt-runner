package ru.shoppinglive.deploynode;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.shoppinglive.deploynode.local.ActuatorService;
import ru.shoppinglive.deploynode.local.LinuxService;
import ru.shoppinglive.deploynode.model.ServiceInstance;

import java.util.List;

@RestController
public class InstanceController {

    @Autowired
    private LinuxService linuxService;
    @Autowired
    private ActuatorService actuatorService;

    @GetMapping("/instance")
    public List<ServiceInstance> all(){
        return linuxService.getInstances();
    }

    @GetMapping("/project/{code}/instance")
    public List<ServiceInstance> byProject(@PathVariable("code") String code){
        return linuxService.getInstances(code);
    }

    @PostMapping("/project/{code}/instance/{id}/stop")
    public ResponseEntity stop(@PathVariable("code") String projectCode, @PathVariable("id") int pid){
        return linuxService.stop(projectCode, pid)?ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

    @PostMapping("/project/{code}/instance/{id}/refresh")
    public ResponseEntity refresh(String projectCode, int pid){
        ServiceInstance instance = linuxService.getInstance(projectCode, pid);
        return instance!=null && actuatorService.refreshService(instance.getConnector().getPort())?
            ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

}
