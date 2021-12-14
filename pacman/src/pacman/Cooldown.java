package pacman;

import java.util.Date;

public class Cooldown {
    private float cooldownTime;
    private long currentTime;
    private long timeStarted;
    Cooldown(float cd){cooldownTime=cd;}

    long getMachineTimeInSeconds(){
        Date d = new Date();
        return d.getTime()/1000%1000000;
    }
    void start(){
        timeStarted = getMachineTimeInSeconds();
        currentTime = 0;
    }
    boolean isReady(){
        return currentTime > cooldownTime;
    }
    void updateTimer(){
        if(!isReady()) currentTime = getMachineTimeInSeconds() - timeStarted;
    }
}
