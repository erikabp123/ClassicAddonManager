package com.CAM.HelperTools;

import java.util.ArrayList;

public class ArgumentPasser {
    private Object[] arguments;
    private Object[] returnArguments;

    public ArgumentPasser(Object[] arguments){
        this.arguments = arguments;
    }

    public ArgumentPasser(){
        this.arguments = new Object[0];
    }

    public Object[] getArguments(){
        return this.arguments;
    }

    public Object[] getReturnArguments(){
        return this.returnArguments;
    }

    public void setReturnArguments(Object[] returnArguments){
        this.returnArguments = returnArguments;
    }

}
