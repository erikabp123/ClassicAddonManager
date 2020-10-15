package com.CAM.HelperTools;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoopController {

    private AtomicBoolean loop = new AtomicBoolean(true);

    public boolean shouldLoop(){
        return loop.get();
    }

    public void setLooping(boolean loop){
        this.loop.set(loop);
    }


}
