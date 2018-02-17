package ru.shoppinglive.deploynode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Build {

    private final static Pattern idPattern = Pattern.compile("^([a-z\\-0-9]+?)-([0-9.]+)(-([a-zA-Z0-9]+))?$");

    private String code;
    private String version;
    private String modifier;

    public String getJarName(){
        return code+"-"+getServiceId()+".jar";
    }

    public String getServiceId(){
        return modifier==null || modifier.isEmpty()?version:(version+"-"+modifier);
    }

    public static Build fromId(String id){
        Matcher matcher = idPattern.matcher(id);
        return matcher.find()?new Build(matcher.group(1), matcher.group(2), matcher.group(4)):null;
    }

}
