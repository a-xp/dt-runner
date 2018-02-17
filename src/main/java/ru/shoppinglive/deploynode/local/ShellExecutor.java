package ru.shoppinglive.deploynode.local;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class ShellExecutor<T> {

    private Function<String, T> callable;
    private String statement;

    private ShellExecutor(Function<String, T> callable, String statement) {
        this.callable = callable;
        this.statement = statement;
    }

    public T execute(){
        try {
            Process process = Runtime.getRuntime().exec(statement);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            T result = null;
            while ((line = bufReader.readLine()) != null && result == null) {
                  result = callable.apply(line);
            }
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static <T> ShellExecutor<T> from (Function<String, T> callable, String command){
        return new ShellExecutor<>(callable, command);
    }

    public static boolean runAndWait(String command){
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
