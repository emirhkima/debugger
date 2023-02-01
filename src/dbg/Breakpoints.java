
package dbg;

        import com.sun.jdi.*;
        import com.sun.jdi.request.BreakpointRequest;

public class Breakpoints extends Command {

    VirtualMachine vm;

    public Breakpoints(VirtualMachine vm) {
        this.vm = vm;
    }

    @Override
    public void run() {
        for (BreakpointRequest breakpointRequest:
                vm.eventRequestManager().breakpointRequests()
             ) {
            System.out.println(breakpointRequest);
        }
    }

    @Override
    public boolean isLocked() {
        return true;
    }

}
