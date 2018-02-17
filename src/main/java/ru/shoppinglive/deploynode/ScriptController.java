package ru.shoppinglive.deploynode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shoppinglive.deploynode.local.ScriptService;
import ru.shoppinglive.deploynode.model.ScriptMeta;

@RestController
public class ScriptController {

    @Autowired
    private ScriptService scriptService;

    @GetMapping("/project/{code}/service-script")
    public ScriptMeta getByCode(@PathVariable("code") String projectCode){
        return scriptService.parseServiceScript(projectCode);
    }

    @GetMapping("/project/{code}/cron-script")
    public ScriptMeta getCronByCode(@PathVariable("code") String projectCode){
        return scriptService.parseCronScript(projectCode);
    }

    @PostMapping("/project/{code}/service-script")
    public ResponseEntity updateScript(@PathVariable("code") String projectCode, @RequestBody ScriptMeta scriptMeta){
        scriptMeta.setCode(projectCode);
        return scriptService.createServiceScript(scriptMeta)?
                ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

    @PostMapping("/project/{code}/cron-script")
    public ResponseEntity updateCron(@PathVariable("code") String projectCode, @RequestBody ScriptMeta scriptMeta){
        scriptMeta.setCode(projectCode);
        return scriptService.createCronScript(scriptMeta)?
                ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

}
