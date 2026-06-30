// CS2 (clientscript) codec. Ported from
// net.runelite.cache.definitions.loaders.ScriptLoader / savers.ScriptSaver.
import { InStream, OutStream } from './io.js';
import { OPCODE_NAMES } from './opcodes.js';

const SCONST = 3, RETURN = 21, POP_INT = 38, POP_STRING = 39;

export const OPCODE_BY_NAME = (() => {
  const m = {};
  for (const [k, v] of Object.entries(OPCODE_NAMES)) m[v] = Number(k);
  return m;
})();

export function opcodeName(op) { return OPCODE_NAMES[op] || `OP_${op}`; }

export function decodeScript(id, b) {
  const s = new InStream(b);
  s.setOffset(s.length - 2);
  const switchLength = s.readUnsignedShort();
  const endIdx = s.length - 2 - switchLength - 12;
  s.setOffset(endIdx);
  const numOpcodes = s.readInt();
  const localIntCount = s.readUnsignedShort();
  const localStringCount = s.readUnsignedShort();
  const intStackCount = s.readUnsignedShort();
  const stringStackCount = s.readUnsignedShort();

  const numSwitches = s.readUnsignedByte();
  const switches = [];
  for (let i = 0; i < numSwitches; i++) {
    const count = s.readUnsignedShort();
    const cases = [];
    for (let j = 0; j < count; j++) cases.push({ key: s.readInt(), offset: s.readInt() });
    switches.push(cases);
  }

  s.setOffset(0);
  const name = s.readStringOrNull();

  const instructions = [];
  while (s.getOffset() < endIdx) {
    const opcode = s.readUnsignedShort();
    const ins = { opcode };
    if (opcode === SCONST) {
      ins.stringOperand = s.readString();
    } else if (opcode < 100 && opcode !== RETURN && opcode !== POP_INT && opcode !== POP_STRING) {
      ins.intOperand = s.readInt();
    } else {
      ins.intOperand = s.readUnsignedByte();
    }
    instructions.push(ins);
  }

  return { id, name, localIntCount, localStringCount, intStackCount, stringStackCount, switches, instructions };
}

export function encodeScript(def) {
  const out = new OutStream();
  // Preserve the script name (readStringOrNull): a present name is written as a
  // null-terminated string, a null name as a single 0 byte.
  if (def.name == null) out.writeByte(0);
  else out.writeString(def.name);
  for (const ins of def.instructions) {
    out.writeShort(ins.opcode);
    if (ins.opcode === SCONST) {
      out.writeString(ins.stringOperand || '');
    } else if (ins.opcode < 100 && ins.opcode !== RETURN && ins.opcode !== POP_INT && ins.opcode !== POP_STRING) {
      out.writeInt(ins.intOperand | 0);
    } else {
      out.writeByte(ins.intOperand | 0);
    }
  }
  out.writeInt(def.instructions.length);
  out.writeShort(def.localIntCount || 0);
  out.writeShort(def.localStringCount || 0);
  out.writeShort(def.intStackCount || 0);
  out.writeShort(def.stringStackCount || 0);
  const switchStart = out.getOffset();
  const switches = def.switches || [];
  out.writeByte(switches.length);
  for (const cases of switches) {
    out.writeShort(cases.length);
    for (const cse of cases) { out.writeInt(cse.key); out.writeInt(cse.offset); }
  }
  const switchLength = out.getOffset() - switchStart;
  out.writeShort(switchLength);
  return out.flip();
}

// Human-readable disassembly, one instruction per line.
export function disassemble(def) {
  const lines = [];
  lines.push(`; script ${def.id}  locals(int=${def.localIntCount} str=${def.localStringCount})  stack(int=${def.intStackCount} str=${def.stringStackCount})`);
  def.instructions.forEach((ins, i) => {
    let operand = '';
    if (ins.opcode === SCONST) operand = JSON.stringify(ins.stringOperand ?? '');
    else operand = String(ins.intOperand ?? 0);
    lines.push(`${String(i).padStart(4, ' ')}  ${opcodeName(ins.opcode).padEnd(22, ' ')} ${operand}`);
  });
  if (def.switches && def.switches.length) {
    def.switches.forEach((cases, i) => {
      lines.push(`; switch ${i}`);
      for (const c of cases) lines.push(`;   case ${c.key} -> +${c.offset}`);
    });
  }
  return lines.join('\n');
}
