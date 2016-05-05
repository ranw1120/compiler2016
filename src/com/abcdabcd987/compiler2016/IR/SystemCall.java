package com.abcdabcd987.compiler2016.IR;

import java.util.Map;
import java.util.function.*;
import java.util.function.Function;

/**
 * Created by abcdabcd987 on 2016-05-03.
 */
public class SystemCall extends IRInstruction {
    public SystemCall(BasicBlock curBB) {
        super(curBB);
    }

    @Override
    public void accept(IIRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Register getDefinedRegister() {
        return null;
    }

    @Override
    public void setDefinedRegister(Register newReg) {
        assert false;
    }

    @Override
    public void setUsedRegister(Map<Register, Register> regMap) {
        // do nothing
    }

    @Override
    public void renameDefinedRegister(Function<VirtualRegister, Integer> idSupplier) {
        // do nothing
    }

    @Override
    public void renameUsedRegister(Function<VirtualRegister, Integer> idSupplier) {
        // do nothing
    }
}