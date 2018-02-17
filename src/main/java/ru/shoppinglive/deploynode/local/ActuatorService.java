package ru.shoppinglive.deploynode.local;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ActuatorService {

    private RestTemplate restTemplate = new RestTemplate();

    public boolean refreshService(int port){
        try {
            restTemplate.postForLocation("http://127.0.0.1:" + port + "/refresh", null);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean refreshLocalConfiguration(){
        try {
            restTemplate.postForLocation("http://127.0.0.1:8888/refresh",null);
            return true;
        }catch (Exception e){
            return false;
        }
    }



}
