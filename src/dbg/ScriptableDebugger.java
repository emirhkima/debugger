package dbg;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.StepRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

public class ScriptableDebugger {

    private Class debugClass;
    private VirtualMachine vm;
    Map<String, Command> commands = new HashMap<>();
    private boolean isStepEnabled = false;

    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debugClass.getName());
        VirtualMachine vm = launchingConnector.launch(arguments);

        return vm;
    }

    public void attachTo(Class debuggeeClass) {

        this.debugClass = debuggeeClass;
        try {
            vm = connectAndLaunchVM();
            enableClassPrepareRequest(vm);
            startCommand();
            startDebugger();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } catch (VMStartException e) {
            e.printStackTrace();
            System.out.println(e.toString());
        } catch (VMDisconnectedException e) {
            System.out.println("Virtual Machine is disconnected: " + e.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    public void setBreakPoint(String className, int lineNumber) throws AbsentInformationException, IOException, InterruptedException {
        for (ReferenceType targetClass : vm.allClasses()) {
            if (targetClass.name().equals(className)) {
                Location location = targetClass.locationsOfLine(lineNumber).get(0);
                BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
                bpReq.enable();
            }
        }
    }

    public void enableStepRequest(LocatableEvent event){
        StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(),
                StepRequest.STEP_LINE,
                StepRequest.STEP_OVER);
        stepRequest.disable();
        stepRequest.enable();
    }

    public void startCommand(){
        commands.put("step",  new Step(vm));
        commands.put("step-over", new StepOver(vm));
        commands.put("frame", new Frame(vm));
        commands.put("stack", new Stack(vm));
        commands.put("method", new Method(vm));
        commands.put("argument", new Argument(vm));
        commands.put("print-var", new PrintVar(vm));
        commands.put("continue", new Continue(vm));
        commands.put("temporaries", new Temporaries(vm));
        commands.put("break", new Break(vm));
        commands.put("breakpoints", new Breakpoints(vm));
        commands.put("break-once", new BreakOnce(vm));
        commands.put("break-on-count", new BreakOnCount(vm));
        commands.put("break-before-method-call", new BreakBeforeMethodCall(vm));
        commands.put("receiver", new Receiver(vm));
    }

    public void read(Event event, ArrayList<String> parameterList){
        parameterList = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        System.out.print("> ");

        String str = sc.nextLine();
        String arr[] = str.split(" ", 4);
        String commandStr = arr[0];

        Command command = commands.get(commandStr);
        command.setEvent(event);

        if(commandStr.equals("step") || commandStr.equals("step-over")){
            command.setStepEnabled(this.isStepEnabled);
            if(this.isStepEnabled == false){
                this.isStepEnabled = true;
            }
        }

        try {
            parameterList.add(0, arr[1]);
            parameterList.add(1, arr[2]);
            parameterList.add(2, arr[3]);
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        command.setParameterList(parameterList);

        if (command != null){
            command.run();
            if (command.isLocked()){
                read(event, parameterList);
            } else {
                return;
            }
        }
    }

    public void startDebugger() throws Exception {
        EventSet eventSet = null;
        while ((eventSet = vm.eventQueue().remove()) != null) {
            for (Event event : eventSet) {
                // Disconnection even interception
                if (event instanceof VMDisconnectEvent) {

                    // Get debuggee VM's input stream
                    System.out.println("Debuggee output ===");
                    InputStreamReader reader = new InputStreamReader(vm.process().getInputStream());
                    OutputStreamWriter writer = new OutputStreamWriter (System.out);

                    // Transfer debuggee VM's data to console
                    char[] buf = new char[vm.process().getInputStream().available()];
                    reader.read(buf);
                    writer.write(buf);
                    writer.flush();

                    System.out.println(" === End of program.");
                    return;
                }

                switch (event) {
                    case ClassPrepareEvent cpe -> {
                        setBreakPoint(debugClass.getName(),6);
                        setBreakPoint(debugClass.getName(),9);
                        setBreakPoint(debugClass.getName(),10);
                    }
                    case BreakpointEvent bpe -> {
                        System.out.println(bpe.location());
                        read(bpe, null);
                    }
                    case StepEvent se -> {
                        event.request().disable();
                        System.out.println(se.location());
                        read(se, null);
                    }
                    case LocatableEvent sr -> {
                        System.out.println(sr.toString());
                        read(sr, null);
                    }
                    default -> {

                    }
                }

                vm.resume();

            }
        }
    }

}
