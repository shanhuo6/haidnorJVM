package haidnor.jvm.instruction.math;

import haidnor.jvm.instruction.Instruction;
import haidnor.jvm.runtime.Frame;
import haidnor.jvm.runtime.StackValue;
import haidnor.jvm.util.CodeStream;
import haidnor.jvm.bcel.Const;

public class LDIV extends Instruction {

    public LDIV(CodeStream codeStream) {
        super(codeStream);
    }

    @Override
    public void execute(Frame frame) {
        StackValue value2 = frame.pop();
        StackValue value1 = frame.pop();
        long result = (long) value1.getValue() / (long) value2.getValue();
        frame.push(new StackValue(Const.T_LONG, result));
    }

}
