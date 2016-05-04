package com.abcdabcd987.compiler2016.BackEnd;

import com.abcdabcd987.compiler2016.CompilerOptions;
import com.abcdabcd987.compiler2016.IR.*;

import java.util.*;

/**
 * Created by abcdabcd987 on 2016-05-02.
 */
public class StupidAllocator extends RegisterAllocator {
    private Function func;
    private List<PhysicalRegister> regs = new ArrayList<>();
    private Map<VirtualRegister, StackSlot> slots = new HashMap<>();

    public StupidAllocator(Collection<PhysicalRegister> regs, Function func) {
        this.func = func;
        func.usedPhysicalGeneralRegister = new HashSet<>();
        this.regs.addAll(regs);
    }

    private StackSlot getStackSlot(VirtualRegister vr) {
        StackSlot slot = slots.get(vr);
        if (slot == null) {
            slot = new StackSlot(func, vr.getHintName());
            slots.put(vr, slot);
        }
        return slot;
    }

    @Override
    public void run() {
        Map<Register, Register> regRenameMap = new HashMap<>();
        for (BasicBlock BB : func.getReversePostOrder()) {
            for (IRInstruction inst = BB.getHead(); inst != null; inst = inst.getNext()) {
                int cnt = 0;
                if (!(inst instanceof Call)) {
                    Collection<Register> used = inst.getUsedRegister();
                    if (!used.isEmpty()) {
                        regRenameMap.clear();
                        used.forEach(x -> regRenameMap.put(x, x));
                        for (Register reg : used)
                            if (reg instanceof VirtualRegister || (reg instanceof StackSlot && !(inst instanceof Load) && !(inst instanceof Store))) {
                                PhysicalRegister pr = regs.get(cnt++);
                                regRenameMap.put(reg, pr);
                                func.usedPhysicalGeneralRegister.add(pr);
                                Register addr = reg instanceof VirtualRegister ? getStackSlot((VirtualRegister) reg) : reg;
                                inst.prepend(new Load(BB, pr, CompilerOptions.getSizeInt(), addr, 0));
                            }
                        inst.setUsedRegister(regRenameMap);
                    }
                } else {
                    // call instruction don't deserve register allocation
                    List<IntValue> args = ((Call) inst).getArgs();
                    for (int i = 0; i < args.size(); ++i) {
                        IntValue val = args.get(i);
                        if (val instanceof VirtualRegister) {
                            Register addr = getStackSlot((VirtualRegister) val);
                            args.set(i, addr);
                        }
                    }
                }

                Register defined = inst.getDefinedRegister();
                if (defined instanceof VirtualRegister || defined instanceof StackSlot) {
                    PhysicalRegister pr = regs.get(cnt++);
                    func.usedPhysicalGeneralRegister.add(pr);
                    inst.setDefinedRegister(pr);
                    Register addr = defined instanceof VirtualRegister ? getStackSlot((VirtualRegister) defined) : defined;
                    inst.append(new Store(BB, CompilerOptions.getSizeInt(), addr, 0, pr));
                    inst = inst.getNext(); // skip the new added store
                }
            }
        }
    }
}
