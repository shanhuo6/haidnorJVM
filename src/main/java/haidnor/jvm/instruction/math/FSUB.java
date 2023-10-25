package haidnor.jvm.instruction.math;

import haidnor.jvm.instruction.Instruction;
import haidnor.jvm.runtime.Frame;
import haidnor.jvm.runtime.StackValue;
import haidnor.jvm.util.CodeStream;
import haidnor.jvm.bcel.Const;

public class FSUB extends Instruction {

    public FSUB(CodeStream codeStream) {
        super(codeStream);
    }

    @Override
    public void execute(Frame frame) {
        StackValue value2 = frame.pop();
        StackValue value1 = frame.pop();
        float result = (float) value1.getValue() - (float) value2.getValue();
        frame.push(new StackValue(Const.T_FLOAT, result));
    }

}
