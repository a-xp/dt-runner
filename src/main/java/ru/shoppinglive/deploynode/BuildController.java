package ru.shoppinglive.deploynode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shoppinglive.deploynode.local.FileSystemService;
import ru.shoppinglive.deploynode.local.LinuxService;
import ru.shoppinglive.deploynode.model.Build;
import ru.shoppinglive.deploynode.rest.UploadRequest;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.StreamSupport;

@RestController
public class BuildController {

    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    private LinuxService linuxService;

    @GetMapping("/project/{code}/build")
    public List<Build> getByCode(@PathVariable("code") String projectCode){
        return fileSystemService.getProjectBuild(projectCode);
    }

    @PostMapping("/build/upload")
    public ResponseEntity upload(@Valid @RequestBody UploadRequest request){
        return fileSystemService.upload(request.getArtifactUrl())?
                ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

    @PostMapping("/build/{id}/start")
    public ResponseEntity start(@PathVariable("id") String id){
        Build build = Build.fromId(id);
        return build!=null && linuxService.start(build)?
                ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

    @PostMapping("/build/{id}/remove")
    public ResponseEntity remove(@PathVariable("id") String id){
        Build build = Build.fromId(id);
        return build!=null && fileSystemService.remove(build)?
                ResponseEntity.accepted().build():ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/build/{id}/log", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity logTail(@RequestParam(value = "size", defaultValue = "10000") long size, @PathVariable("id") String id,
                                  HttpServletResponse response) throws IOException{
        Build build = Build.fromId(id);
        if(build!=null){
            response.setHeader("Content-Type", "text/plain; charset=UTF-8");
            fileSystemService.copyLogTail(build, response.getOutputStream(), size);
            return null;
        }else{
            return ResponseEntity.badRequest().build();
        }
    }

}
