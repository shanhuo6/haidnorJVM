package haidnor.jvm.core;


import haidnor.jvm.instruction.Instruction;
import haidnor.jvm.instruction.InstructionFactory;
import haidnor.jvm.instruction.control.*;
import haidnor.jvm.rtda.KlassMethod;
import haidnor.jvm.runtime.Frame;
import haidnor.jvm.runtime.JVMThread;
import haidnor.jvm.runtime.StackValue;
import haidnor.jvm.util.CodeStream;
import haidnor.jvm.util.JvmThreadHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;

import java.util.HashMap;
import java.util.Map;

/**
 * JVM 执行引擎
 *
 * @author wang xiang
 */
@Slf4j
public class JavaExecutionEngine {

    public static void callMainMethod(KlassMethod klassMethod) {
        callMethod(null, klassMethod);
    }

    public static void callMethod(Frame lastFrame, KlassMethod klassMethod) {
        JVMThread jvmThread = JvmThreadHolder.get();
        Frame newFrame = new Frame(jvmThread, klassMethod);

        // 如果有上一个栈帧, 代表需要传参
        if (lastFrame != null) {
            Method method = klassMethod.javaMethod;
            String signature = method.getSignature();
            String[] argumentTypes = Utility.methodSignatureArgumentTypes(signature);

            int argumentSlotSize = argumentTypes.length;
            if (!method.isStatic()) {
                argumentSlotSize++;
            }

            // 方法调用传参
            // 将上一个栈帧操作数栈中数据弹出,存入下一个栈帧的局部变量表中
            LocalVariableTable localVariableTable = method.getLocalVariableTable();
            if (localVariableTable != null) {
                for (int i = argumentSlotSize - 1; i >= 0; i--) {
                    LocalVariable[] localVariableArr = localVariableTable.getLocalVariableTable();
                    LocalVariable localVariable = localVariableArr[i];
                    int slotIndex = localVariable.getIndex();
                    StackValue stackValue = lastFrame.pop();
                    newFrame.slotSet(slotIndex, stackValue);
                }
            }
        }

        jvmThread.push(newFrame);
        executeFrame(newFrame);
    }

    @SneakyThrows
    public static void executeFrame(Frame frame) {
        int stackSize = frame.getJvmThread().stackSize();

        StringBuilder blank = new StringBuilder();
        blank.append("                    ".repeat(stackSize - 1));
        int index = 0;
        for (int i = 0; i < stackSize - 1; i++) {
            blank.replace(index, index + 1, "│");
            index += 20;
        }

        log.debug("{}┌──────────────────[{}] {} | {}", blank, stackSize, frame.klass.getClassName(), frame.getMethod());

        // 解析方法中的字节码指令
        Map<Integer, Instruction> instructionMap = new HashMap<>();
        CodeStream codeStream = frame.getCodeStream();
        while (codeStream.available() > 0) {
            Instruction instruction = InstructionFactory.creatInstruction(codeStream);
            log.debug("{}│> {}", blank, instruction);
            instructionMap.put(instruction.index(), instruction);
        }

        log.debug("{}├╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌", blank);

        // 执行方法中的字节码指令 tip:(int pc, 相当于程序计数器, 记录当前执行到的字节码指令的”行号“)
        for (int pc = 0; pc < frame.getCodeLength(); ) {
            Instruction instruction = instructionMap.get(pc);
            log.debug("{}│ {}", blank, instruction);
            try {
                instruction.execute(frame);
                if (instruction instanceof RETURN || instruction instanceof ARETURN || instruction instanceof DRETURN || instruction instanceof FRETURN || instruction instanceof IRETURN) {
                    break;
                }
                pc += instruction.offSet();
            }
            // catch instruction.execute() Exception
            catch (Exception exception) {
                Integer handlerPC = null;

                CodeException[] exceptionTable = frame.getMethod().getCode().getExceptionTable();
                for (CodeException codeException : exceptionTable) {
                    if (codeException.getStartPC() <= pc & pc <= codeException.getEndPC()) {
                        int catchType = codeException.getCatchType();
                        // 0, if the handler catches any exception, otherwise it points to the exception class which is to be caught.
                        if (catchType == 0) {
                            frame.push(new StackValue(Const.T_OBJECT, exception));
                            handlerPC = codeException.getHandlerPC();
                        } else {
                            String exceptionClassName = frame.getConstantPoolUtil().constantClass_ClassName(catchType);
                            exceptionClassName = Utility.compactClassName(exceptionClassName, false);
                            Class<?> exceptionClass = Class.forName(exceptionClassName);
                            if (exceptionClass.isAssignableFrom(exception.getClass())) {
                                frame.push(new StackValue(Const.T_OBJECT, exception));
                                handlerPC = codeException.getHandlerPC();
                            }
                        }
                    }
                }
                if (handlerPC != null) {
                    pc = handlerPC;
                } else {
                    log.debug("{}└──────────────────[{}] No Exception Handler Return!", blank, stackSize);
                    throw exception;
                }
            }

        }

        log.debug("{}└──────────────────[{}] {} | {}", blank, stackSize, frame.klass.getClassName(), frame.getMethod());
    }


}
