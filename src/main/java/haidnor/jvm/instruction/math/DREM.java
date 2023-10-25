package haidnor.jvm.instruction.math;

import haidnor.jvm.instruction.Instruction;
import haidnor.jvm.runtime.Frame;
import haidnor.jvm.runtime.StackValue;
import haidnor.jvm.util.CodeStream;
import haidnor.jvm.bcel.Const;

public class DREM extends Instruction {

    public DREM(CodeStream codeStream) {
        super(codeStream);
    }

    @Override
    public void execute(Frame frame) {
        StackValue value2 = frame.pop();
        StackValue value1 = frame.pop();
        double result = (double) value1.getValue() % (double) value2.getValue();
        frame.push(new StackValue(Const.T_DOUBLE, result));
    }

}
