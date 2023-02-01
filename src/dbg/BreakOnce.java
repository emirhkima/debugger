package dbg;

import com.sun.jdi.*;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;

import java.util.ArrayList;
import java.util.List;

public class BreakOnce extends Command {

    VirtualMachine vm;
    ArrayList<String> parameterList;

    public BreakOnce(VirtualMachine vm) {
        this.vm = vm;
        this.parameterList = new ArrayList<>();
    }

    @Override
    public void run() {
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(this.parameterList.get(0))) {
                try {
                    Location location = targetClass.locationsOfLine(Integer.parseInt(this.parameterList.get(1))).get(0);
                    BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                    bpReq.addCountFilter(1);
                    bpReq.enable();
                    System.out.println("BreakPoint created : "+ location.lineNumber());
                } catch (AbsentInformationException e) {
                    e.printStackTrace();
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
