package ru.shoppinglive.deploynode.local;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.shoppinglive.deploynode.exceptions.RunnerError;
import ru.shoppinglive.deploynode.model.Build;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@ConfigurationProperties(prefix = "deploy.artifacts")
public class FileSystemService {

    @Setter
    private Path root;
    @Setter
    @Getter
    private List<String> srcServers;
    @Setter
    private long maxJarSize;

    private final Pattern jarNamePattern = Pattern.compile("^([a-z\\-0-9]+?)-([0-9.]+)(-([a-zA-Z0-9]+))?\\.jar$");

    public List<Build> getProjectBuild(String projectCode){
        Path jarPath = root.resolve(Paths.get("jar", projectCode));
        if(!Files.exists(jarPath))return Collections.emptyList();
        if(!Files.isReadable(jarPath))throw new RunnerError("Can not read "+jarPath+" directory");
        try{
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(Files.newDirectoryStream(jarPath).iterator(), Spliterator.ORDERED), false)
                .map(path -> {
                    Matcher matcher = jarNamePattern.matcher(path.getFileName().toString());
                    return matcher.find() && matcher.group(1).equals(projectCode)?new Build(matcher.group(1), matcher.group(2), matcher.group(4)):null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
        }catch (IOException e){
            return Collections.emptyList();
        }
    }

    public boolean upload(String artifactUrl){
        try {
            URL url = new URL(artifactUrl);
            if(!srcServers.contains(url.getHost()))throw new RunnerError(url.getHost()+ " is not in valid repository servers list");
            String[] fragments = url.getPath().split("/");
            String filename = fragments[fragments.length-1];
            Matcher matcher = jarNamePattern.matcher(filename);
            if(matcher.find()){
                String projectCode = matcher.group(1);
                Path jarPath = root.resolve(Paths.get("jar",projectCode, filename));
                if(!Files.exists(jarPath.getParent())){
                    Files.createDirectories(jarPath.getParent());
                }
                try(InputStream is = url.openStream();
                    ReadableByteChannel rbc = Channels.newChannel(is);
                    FileOutputStream fos = new FileOutputStream(jarPath.toFile())){
                    fos.getChannel().transferFrom(rbc, 0, maxJarSize);
                }
                return true;
            }else{
                throw new RunnerError(filename + " is not valid service jar name");
            }
        }catch (IOException e){
            throw new RunnerError(e, "Can not download artifact");
        }
    }

    public boolean remove(Build build){
        Path jarPath = root.resolve(Paths.get("jar", build.getCode(), build.getJarName()));
        try {
            Files.deleteIfExists(jarPath);
            return true;
        }catch (IOException e){
            return false;
        }
    }

    public boolean copyLogTail(Build build, OutputStream os, long size){
        Path log = root.resolve(Paths.get("logs", build.getCode(), build.getCode()+"-"+build.getServiceId()+".log"));
        if(Files.exists(log)){
            try(SeekableByteChannel sbc = Files.newByteChannel(log);
                WritableByteChannel wbc = Channels.newChannel(os)){
                ChannelTools.fastCopy(sbc.position(Math.max(0, sbc.size() - size)), wbc);
            }catch (IOException e){
                return false;
            }
        }
        return false;
    }
}
