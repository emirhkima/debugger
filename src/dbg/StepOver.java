package dbg;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.StepRequest;

public class StepOver extends Command{

    private VirtualMachine vm;

    public StepOver(VirtualMachine vm) {
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
                StepRequest.STEP_OVER);
        if(stepRequest.isEnabled()){
            stepRequest.disable();
        }
        stepRequest.enable();
    }
}
