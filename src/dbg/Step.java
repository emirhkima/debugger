package dbg;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.StepRequest;

import java.util.Scanner;

public class Step extends Command {

    VirtualMachine vm;

    public Step(VirtualMachine vm) {
        this.vm = vm;
    }

    @Override
    public void run() {
        if(this.getStepEnabled()) {
            if (!vm.eventRequestManager().stepRequests().isEmpty()) {
                vm.eventRequestManager().stepRequests().get(0).disable();
                vm.eventRequestManager().deleteEventRequests(vm.eventRequestManager().stepRequests());
            }
        }
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(((LocatableEvent) getEvent()).thread(),
                StepRequest.STEP_LINE,
                StepRequest.STEP_INTO);
        if(stepRequest.isEnabled()){
            stepRequest.disable();
        }
        stepRequest.enable();
    }
}
