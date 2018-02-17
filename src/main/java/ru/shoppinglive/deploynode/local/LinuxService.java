package ru.shoppinglive.deploynode.local;

import com.sun.tools.attach.VirtualMachine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.shoppinglive.deploynode.model.Build;
import ru.shoppinglive.deploynode.model.Connector;
import ru.shoppinglive.deploynode.model.ProcessStats;
import ru.shoppinglive.deploynode.model.ServiceInstance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LinuxService {

    private final Pattern jarNamePattern = Pattern.compile("^([a-z\\-0-9]+?)-([0-9.]+)(-([a-zA-Z0-9]+))?\\.jar$");
    private final Pattern lsofOutputPattern = Pattern.compile("((\\d+\\.\\d+\\.\\d+\\.\\d+)|\\*):(\\d+)");

    public List<ServiceInstance> getInstances(){
        return findLocalInstances().peek(i->{
            i.setConnector(getPortByPid(i.getPid()));
            i.setStats(getProcessStats(i.getPid()));
        }).collect(Collectors.toList());
    }

    private Stream<ServiceInstance> findLocalInstances(){
        return VirtualMachine.list().stream().map(vm->{
            String name = vm.displayName();
            int id = Integer.parseInt(vm.id());
            Matcher matcher = jarNamePattern.matcher(name);
            if(matcher.find()){
                return new ServiceInstance(matcher.group(1), matcher.group(2), matcher.group(4), null, id, null);
            }else{
                return null;
            }
        }).filter(Objects::nonNull);
    }

    public ServiceInstance getInstance(String projectCode, int pid){
        return findLocalInstances().filter(i->i.getCode().equals(projectCode) && i.getPid()==pid).findFirst().orElse(null);
    }


    public List<ServiceInstance> getInstances(String projectCode){
        return findLocalInstances().filter(i->i.getCode().equals(projectCode)).peek(i->{
            i.setConnector(getPortByPid(i.getPid()));
            i.setStats(getProcessStats(i.getPid()));
        }).collect(Collectors.toList());
    }

    private Connector getPortByPid(int pid){
        return ShellExecutor.from(line->{
            if(!line.contains("(LISTEN)"))return null;
            Matcher m = lsofOutputPattern.matcher(line);
            return m.find()?new Connector(m.group(1), Integer.parseInt(m.group(3))):null;
        }, "lsof -Pan -p "+pid+" -i").execute();
    }

    private ProcessStats getProcessStats(int pid){
        return ShellExecutor.from(line->{
            String[] parts = line.trim().split("\\s+");
            return new ProcessStats(Long.parseLong(parts[1]), Float.parseFloat(parts[2]), Long.parseLong(parts[3])*1024);
        }, "ps -f -p "+pid+" -eo pid,etimes,pcpu,rss --no-headers").execute();
    }

    public boolean stop(String projectCode, int pid){
        Optional<ServiceInstance> instance = findLocalInstances().filter(i->i.getCode().equals(projectCode) && i.getPid()==pid).findFirst();
        return instance.isPresent() && ShellExecutor.runAndWait("kill "+instance.get().getPid());
    }

    public boolean start(Build build){
        return ShellExecutor.runAndWait("service "+build.getCode()+" start "+build.getServiceId());
    }



}
