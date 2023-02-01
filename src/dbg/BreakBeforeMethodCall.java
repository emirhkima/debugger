package dbg;

import com.sun.jdi.*;
import com.sun.jdi.Method;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;

import java.util.ArrayList;
import java.util.List;

public class BreakBeforeMethodCall extends Command {

    VirtualMachine vm;
    ArrayList<String> parameterList;

    public BreakBeforeMethodCall(VirtualMachine vm) {
        this.vm = vm;
        this.parameterList = new ArrayList<>();
    }

    @Override
    public void run() {
        for (ReferenceType targetClass : vm.allClasses()) {
            for(Method method : targetClass.methods()){
                if(method.name().equals((this.parameterList.get(0)))){
                    Location location = method.location();
                    BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                    bpReq.enable();
                    System.out.println("BreakPoint created : "+ location.lineNumber());
                }
            }
        }
    }

    @Override
    public void setParameterList(ArrayList<String> parameterList){
        this.parameterList = parameterList;
    }

    @Override
    public boolean isLocked() {
        return true;
    }
}
