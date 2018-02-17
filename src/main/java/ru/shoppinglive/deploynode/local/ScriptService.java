package ru.shoppinglive.deploynode.local;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.shoppinglive.deploynode.DeployNodeApplication;
import ru.shoppinglive.deploynode.exceptions.RunnerError;
import ru.shoppinglive.deploynode.model.ScriptMeta;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "deploy.scripts")
public class ScriptService {

    private final Pattern paramPattern = Pattern.compile("^([A-Z_]+)=\"(.*?)\"");
    private Configuration freemarker;

    @Setter
    private String cronPath;
    @Setter
    private String servicePath;

    @Value("${deploy.artifacts.root}")
    private String root;

    private boolean createScript(String tpl, Path file, ScriptMeta meta){
        try {
            if(!Files.exists(file.getParent()))Files.createDirectories(file.getParent());
            Template template = freemarker.getTemplate(tpl);
            try(FileWriter fw = new FileWriter(file.toFile())){
                template.process(new HashMap<String, Object>() {{
                    put("meta", meta);
                    put("root", root);
                }}, fw);
            }
            try {
                Files.setPosixFilePermissions(file,
                new HashSet<PosixFilePermission>(){{
                    add(PosixFilePermission.OWNER_READ);
                    add(PosixFilePermission.OWNER_WRITE);
                    add(PosixFilePermission.OWNER_EXECUTE);
                    add(PosixFilePermission.GROUP_READ);
                    add(PosixFilePermission.OTHERS_READ);
                }});
            }catch (UnsupportedOperationException e){
                return false;
            }
            return true;
        }catch (IOException | TemplateException e){
            return false;
        }
    }

    private ScriptMeta parseParams(Path file){
        if(Files.exists(file)){
            ScriptMeta result = new ScriptMeta();
            try {
                Files.lines(file).forEachOrdered(l->{
                    Matcher matcher = paramPattern.matcher(l);
                    if(matcher.find()){
                        switch (matcher.group(1)){
                            case "MEM": result.setMemory(matcher.group(2)); break;
                            case "ENV": result.setEnv(matcher.group(2)); break;
                            case "SERVICE_NAME": result.setCode(matcher.group(2)); break;
                            case "VERSION": result.setVersion(matcher.group(2)); break;
                            case "ARGS": result.setArgs(matcher.group(2)); break;
                        }
                    }
                });
                return result;
            }catch (IOException e){
                throw new RunnerError(e, "Can not parse script");
            }
        }else{
            return null;
        }
    }

    public ScriptMeta parseCronScript(String code){
        return parseParams(Paths.get(cronPath, code));
    }

    public ScriptMeta parseServiceScript(String code){
        return parseParams(Paths.get(servicePath, code));
    }

    public boolean createCronScript(ScriptMeta meta){
        return createScript("cron.tpl", Paths.get(cronPath, meta.getCode()), meta);
    }

    public boolean createServiceScript(ScriptMeta meta){
        return createScript("service.tpl", Paths.get(servicePath, meta.getCode()), meta);
    }

    @PostConstruct
    private void init(){
        freemarker = new freemarker.template.Configuration(Configuration.VERSION_2_3_23);
        freemarker.setDefaultEncoding("UTF-8");
        freemarker.setClassForTemplateLoading(DeployNodeApplication.class, "/templates");
        freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarker.setLogTemplateExceptions(false);
    }

}
