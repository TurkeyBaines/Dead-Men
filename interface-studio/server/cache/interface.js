// Interface component (widget) codec. Decodes the IF1 (legacy) and IF3 formats
// used by index 3, and re-encodes them byte-for-byte. Ported from
// net.runelite.cache.definitions.loaders.InterfaceLoader; the IF3 encoder is
// new (the reference InterfaceSaver only supported IF1).
import { InStream, OutStream } from './io.js';

// A decoded component is a plain object. `raw` fields default so unset values
// re-encode to the same bytes. We only emit the bytes that the format dictates
// for the component's type/menuType, exactly like the loader reads them.

export function decodeComponent(id, b) {
  const c = { id };
  const s = new InStream(b);
  if ((b[0] & 0xFF) === 0xFF) {
    decodeIf3(c, s);
  } else {
    decodeIf1(c, s);
  }
  // Some source components carry trailing bytes the client ignores; preserve
  // them verbatim so untouched components save byte-for-byte identically.
  if (s.getOffset() < b.length) {
    c._trailing = Buffer.from(b.subarray(s.getOffset())).toString('base64');
  }
  return c;
}

export function encodeComponent(c) {
  const body = c.isIf3 ? encodeIf3(c) : encodeIf1(c);
  if (c._trailing) return Buffer.concat([body, Buffer.from(c._trailing, 'base64')]);
  return body;
}

// ---------------- IF3 ----------------
function decodeIf3(c, s) {
  s.readUnsignedByte(); // 0xFF marker
  c.isIf3 = true;
  c.type = s.readUnsignedByte();
  c.contentType = s.readUnsignedShort();
  c.originalX = s.readShort();
  c.originalY = s.readShort();
  c.originalWidth = s.readUnsignedShort();
  c.originalHeight = c.type === 9 ? s.readShort() : s.readUnsignedShort();
  c.widthMode = s.readByte();
  c.heightMode = s.readByte();
  c.xPositionMode = s.readByte();
  c.yPositionMode = s.readByte();
  c.parentId = s.readUnsignedShort();
  c.parentId = c.parentId === 0xFFFF ? -1 : (c.parentId + (c.id & ~0xFFFF));
  c.isHidden = s.readUnsignedByte() === 1;

  if (c.type === 0) {
    c.scrollWidth = s.readUnsignedShort();
    c.scrollHeight = s.readUnsignedShort();
    c.noClickThrough = s.readUnsignedByte() === 1;
  }
  if (c.type === 5) {
    c.spriteId = s.readInt();
    c.textureId = s.readUnsignedShort();
    c.spriteTiling = s.readUnsignedByte() === 1;
    c.opacity = s.readUnsignedByte();
    c.borderType = s.readUnsignedByte();
    c.shadowColor = s.readInt();
    c.flippedVertically = s.readUnsignedByte() === 1;
    c.flippedHorizontally = s.readUnsignedByte() === 1;
  }
  if (c.type === 6) {
    c.modelType = 1;
    c.modelId = orMinus(s.readUnsignedShort());
    c.offsetX2d = s.readShort();
    c.offsetY2d = s.readShort();
    c.rotationX = s.readUnsignedShort();
    c.rotationZ = s.readUnsignedShort();
    c.rotationY = s.readUnsignedShort();
    c.modelZoom = s.readUnsignedShort();
    c.animation = orMinus(s.readUnsignedShort());
    c.orthogonal = s.readUnsignedByte() === 1;
    c._t6pad = s.readUnsignedShort();
    if (c.widthMode !== 0) c.modelHeightOverride = s.readUnsignedShort();
    if (c.heightMode !== 0) c._t6heightExtra = s.readUnsignedShort();
  }
  if (c.type === 4) {
    c.fontId = orMinus(s.readUnsignedShort());
    c.text = s.readString();
    c.lineHeight = s.readUnsignedByte();
    c.xTextAlignment = s.readUnsignedByte();
    c.yTextAlignment = s.readUnsignedByte();
    c.textShadowed = s.readUnsignedByte() === 1;
    c.textColor = s.readInt();
  }
  if (c.type === 3) {
    c.textColor = s.readInt();
    c.filled = s.readUnsignedByte() === 1;
    c.opacity = s.readUnsignedByte();
  }
  if (c.type === 9) {
    c.lineWidth = s.readUnsignedByte();
    c.textColor = s.readInt();
    c.lineDirection = s.readUnsignedByte() === 1;
  }
  c.clickMask = s.read24BitInt();
  c.name = s.readString();
  const numActions = s.readUnsignedByte();
  c.actions = [];
  for (let i = 0; i < numActions; i++) c.actions.push(s.readString());
  c.dragDeadZone = s.readUnsignedByte();
  c.dragDeadTime = s.readUnsignedByte();
  c.dragRenderBehavior = s.readUnsignedByte() === 1;
  c.targetVerb = s.readString();

  c.onLoadListener = decodeListener(s);
  c.onMouseOverListener = decodeListener(s);
  c.onMouseLeaveListener = decodeListener(s);
  c.onTargetLeaveListener = decodeListener(s);
  c.onTargetEnterListener = decodeListener(s);
  c.onVarTransmitListener = decodeListener(s);
  c.onInvTransmitListener = decodeListener(s);
  c.onStatTransmitListener = decodeListener(s);
  c.onTimerListener = decodeListener(s);
  c.onOpListener = decodeListener(s);
  c.onMouseRepeatListener = decodeListener(s);
  c.onClickListener = decodeListener(s);
  c.onClickRepeatListener = decodeListener(s);
  c.onReleaseListener = decodeListener(s);
  c.onHoldListener = decodeListener(s);
  c.onDragListener = decodeListener(s);
  c.onDragCompleteListener = decodeListener(s);
  c.onScrollWheelListener = decodeListener(s);
  c.varTransmitTriggers = decodeTriggers(s);
  c.invTransmitTriggers = decodeTriggers(s);
  c.statTransmitTriggers = decodeTriggers(s);
}

function encodeIf3(c) {
  const o = new OutStream();
  o.writeByte(0xFF);
  o.writeByte(c.type);
  o.writeShort(c.contentType || 0);
  o.writeShort(c.originalX || 0);
  o.writeShort(c.originalY || 0);
  o.writeShort(c.originalWidth || 0);
  o.writeShort(c.originalHeight || 0);
  o.writeByte(c.widthMode || 0);
  o.writeByte(c.heightMode || 0);
  o.writeByte(c.xPositionMode || 0);
  o.writeByte(c.yPositionMode || 0);
  o.writeShort(c.parentId === -1 || c.parentId == null ? 0xFFFF : (c.parentId & 0xFFFF));
  o.writeByte(c.isHidden ? 1 : 0);

  if (c.type === 0) {
    o.writeShort(c.scrollWidth || 0);
    o.writeShort(c.scrollHeight || 0);
    o.writeByte(c.noClickThrough ? 1 : 0);
  }
  if (c.type === 5) {
    o.writeInt(c.spriteId ?? -1);
    o.writeShort(c.textureId || 0);
    o.writeByte(c.spriteTiling ? 1 : 0);
    o.writeByte(c.opacity || 0);
    o.writeByte(c.borderType || 0);
    o.writeInt(c.shadowColor || 0);
    o.writeByte(c.flippedVertically ? 1 : 0);
    o.writeByte(c.flippedHorizontally ? 1 : 0);
  }
  if (c.type === 6) {
    o.writeShort(toU16(c.modelId));
    o.writeShort(c.offsetX2d || 0);
    o.writeShort(c.offsetY2d || 0);
    o.writeShort(c.rotationX || 0);
    o.writeShort(c.rotationZ || 0);
    o.writeShort(c.rotationY || 0);
    o.writeShort(c.modelZoom || 0);
    o.writeShort(toU16(c.animation));
    o.writeByte(c.orthogonal ? 1 : 0);
    o.writeShort(c._t6pad || 0);
    if ((c.widthMode || 0) !== 0) o.writeShort(c.modelHeightOverride || 0);
    if ((c.heightMode || 0) !== 0) o.writeShort(c._t6heightExtra || 0);
  }
  if (c.type === 4) {
    o.writeShort(toU16(c.fontId));
    o.writeString(c.text || '');
    o.writeByte(c.lineHeight || 0);
    o.writeByte(c.xTextAlignment || 0);
    o.writeByte(c.yTextAlignment || 0);
    o.writeByte(c.textShadowed ? 1 : 0);
    o.writeInt(c.textColor || 0);
  }
  if (c.type === 3) {
    o.writeInt(c.textColor || 0);
    o.writeByte(c.filled ? 1 : 0);
    o.writeByte(c.opacity || 0);
  }
  if (c.type === 9) {
    o.writeByte(c.lineWidth || 0);
    o.writeInt(c.textColor || 0);
    o.writeByte(c.lineDirection ? 1 : 0);
  }
  o.write24BitInt(c.clickMask || 0);
  o.writeString(c.name || '');
  o.writeByte(c.actions ? c.actions.length : 0);
  if (c.actions) for (const a of c.actions) o.writeString(a || '');
  o.writeByte(c.dragDeadZone || 0);
  o.writeByte(c.dragDeadTime || 0);
  o.writeByte(c.dragRenderBehavior ? 1 : 0);
  o.writeString(c.targetVerb || '');

  for (const key of LISTENER_KEYS) encodeListener(o, c[key]);
  encodeTriggers(o, c.varTransmitTriggers);
  encodeTriggers(o, c.invTransmitTriggers);
  encodeTriggers(o, c.statTransmitTriggers);
  return o.flip();
}

const LISTENER_KEYS = [
  'onLoadListener', 'onMouseOverListener', 'onMouseLeaveListener',
  'onTargetLeaveListener', 'onTargetEnterListener', 'onVarTransmitListener',
  'onInvTransmitListener', 'onStatTransmitListener', 'onTimerListener',
  'onOpListener', 'onMouseRepeatListener', 'onClickListener',
  'onClickRepeatListener', 'onReleaseListener', 'onHoldListener',
  'onDragListener', 'onDragCompleteListener', 'onScrollWheelListener',
];

function decodeListener(s) {
  const n = s.readUnsignedByte();
  if (n === 0) return null;
  const arr = [];
  for (let i = 0; i < n; i++) {
    const t = s.readUnsignedByte();
    if (t === 0) arr.push({ type: 'int', value: s.readInt() });
    else if (t === 1) arr.push({ type: 'string', value: s.readString() });
    else arr.push({ type: 'unknown', tag: t });
  }
  return arr;
}

function encodeListener(o, arr) {
  if (!arr || arr.length === 0) { o.writeByte(0); return; }
  o.writeByte(arr.length);
  for (const item of arr) {
    if (item.type === 'string') { o.writeByte(1); o.writeString(item.value || ''); }
    else { o.writeByte(0); o.writeInt(item.value | 0); }
  }
}

function decodeTriggers(s) {
  const n = s.readUnsignedByte();
  if (n === 0) return null;
  const arr = [];
  for (let i = 0; i < n; i++) arr.push(s.readInt());
  return arr;
}

function encodeTriggers(o, arr) {
  if (!arr || arr.length === 0) { o.writeByte(0); return; }
  o.writeByte(arr.length);
  for (const v of arr) o.writeInt(v | 0);
}

// ---------------- IF1 (legacy) ----------------
function decodeIf1(c, s) {
  c.isIf3 = false;
  c.type = s.readUnsignedByte();
  c.menuType = s.readUnsignedByte();
  c.contentType = s.readUnsignedShort();
  c.originalX = s.readShort();
  c.originalY = s.readShort();
  c.originalWidth = s.readUnsignedShort();
  c.originalHeight = s.readUnsignedShort();
  c.opacity = s.readUnsignedByte();
  c.parentId = s.readUnsignedShort();
  c.parentId = c.parentId === 0xFFFF ? -1 : (c.parentId + (c.id & ~0xFFFF));
  c.hoveredSiblingId = s.readUnsignedShort();
  if (c.hoveredSiblingId === 0xFFFF) c.hoveredSiblingId = -1;

  const numCmp = s.readUnsignedByte();
  c.alternateOperators = [];
  c.alternateRhs = [];
  for (let i = 0; i < numCmp; i++) {
    c.alternateOperators.push(s.readUnsignedByte());
    c.alternateRhs.push(s.readUnsignedShort());
  }
  const numScripts = s.readUnsignedByte();
  c.cs1 = [];
  for (let i = 0; i < numScripts; i++) {
    const len = s.readUnsignedShort();
    const code = [];
    for (let j = 0; j < len; j++) {
      let v = s.readUnsignedShort();
      code.push(v === 0xFFFF ? -1 : v);
    }
    c.cs1.push(code);
  }

  if (c.type === 0) { c.scrollHeight = s.readUnsignedShort(); c.isHidden = s.readUnsignedByte() === 1; }
  if (c.type === 1) { c._t1a = s.readUnsignedShort(); c._t1b = s.readUnsignedByte(); }
  if (c.type === 2) {
    c.itemSlots = c.originalWidth * c.originalHeight;
    c._inv = {
      a: s.readUnsignedByte(), b: s.readUnsignedByte(), c: s.readUnsignedByte(), d: s.readUnsignedByte(),
      xPitch: s.readUnsignedByte(), yPitch: s.readUnsignedByte(),
      slots: [],
    };
    for (let i = 0; i < 20; i++) {
      const present = s.readUnsignedByte();
      if (present === 1) c._inv.slots.push({ present: 1, x: s.readShort(), y: s.readShort(), sprite: s.readInt() });
      else c._inv.slots.push({ present: 0 });
    }
    c.configActions = [];
    for (let i = 0; i < 5; i++) c.configActions.push(s.readString());
  }
  if (c.type === 3) c.filled = s.readUnsignedByte() === 1;
  if (c.type === 4 || c.type === 1) {
    c.xTextAlignment = s.readUnsignedByte();
    c.yTextAlignment = s.readUnsignedByte();
    c.lineHeight = s.readUnsignedByte();
    c.fontId = orMinus(s.readUnsignedShort());
    c.textShadowed = s.readUnsignedByte() === 1;
  }
  if (c.type === 4) { c.text = s.readString(); c.alternateText = s.readString(); }
  if (c.type === 1 || c.type === 3 || c.type === 4) c.textColor = s.readInt();
  if (c.type === 3 || c.type === 4) {
    c.alternateTextColor = s.readInt();
    c.hoveredTextColor = s.readInt();
    c.alternateHoveredTextColor = s.readInt();
  }
  if (c.type === 5) { c.spriteId = s.readInt(); c.alternateSpriteId = s.readInt(); }
  if (c.type === 6) {
    c.modelType = 1;
    c.modelId = orMinus(s.readUnsignedShort());
    c.alternateModelId = orMinus(s.readUnsignedShort());
    c.animation = orMinus(s.readUnsignedShort());
    c.alternateAnimation = orMinus(s.readUnsignedShort());
    c.modelZoom = s.readUnsignedShort();
    c.rotationX = s.readUnsignedShort();
    c.rotationZ = s.readUnsignedShort();
  }
  if (c.type === 7) {
    c.xTextAlignment = s.readUnsignedByte();
    c.fontId = orMinus(s.readUnsignedShort());
    c.textShadowed = s.readUnsignedByte() === 1;
    c.textColor = s.readInt();
    c.xPitch = s.readShort();
    c.yPitch = s.readShort();
    c._t7flag = s.readUnsignedByte();
    c.configActions = [];
    for (let i = 0; i < 5; i++) c.configActions.push(s.readString());
  }
  if (c.type === 8) c.text = s.readString();
  if (c.menuType === 2 || c.type === 2) {
    c.targetVerb = s.readString();
    c.spellName = s.readString();
    c._spellFlags = s.readUnsignedShort();
  }
  if (c.menuType === 1 || c.menuType === 4 || c.menuType === 5 || c.menuType === 6) {
    c.tooltip = s.readString();
  }
  c._if1End = s.getOffset();
}

function encodeIf1(c) {
  const o = new OutStream();
  o.writeByte(c.type);
  o.writeByte(c.menuType || 0);
  o.writeShort(c.contentType || 0);
  o.writeShort(c.originalX || 0);
  o.writeShort(c.originalY || 0);
  o.writeShort(c.originalWidth || 0);
  o.writeShort(c.originalHeight || 0);
  o.writeByte(c.opacity || 0);
  o.writeShort(c.parentId === -1 || c.parentId == null ? 0xFFFF : (c.parentId & 0xFFFF));
  o.writeShort(c.hoveredSiblingId === -1 || c.hoveredSiblingId == null ? 0xFFFF : c.hoveredSiblingId);

  const ops = c.alternateOperators || [];
  o.writeByte(ops.length);
  for (let i = 0; i < ops.length; i++) { o.writeByte(ops[i]); o.writeShort(c.alternateRhs[i]); }

  const scripts = c.cs1 || [];
  o.writeByte(scripts.length);
  for (const code of scripts) {
    o.writeShort(code.length);
    for (const v of code) o.writeShort(v === -1 ? 0xFFFF : v);
  }

  if (c.type === 0) { o.writeShort(c.scrollHeight || 0); o.writeByte(c.isHidden ? 1 : 0); }
  if (c.type === 1) { o.writeShort(c._t1a || 0); o.writeByte(c._t1b || 0); }
  if (c.type === 2) {
    o.writeByte(c._inv.a); o.writeByte(c._inv.b); o.writeByte(c._inv.c); o.writeByte(c._inv.d);
    o.writeByte(c._inv.xPitch); o.writeByte(c._inv.yPitch);
    for (const slot of c._inv.slots) {
      if (slot.present === 1) { o.writeByte(1); o.writeShort(slot.x); o.writeShort(slot.y); o.writeInt(slot.sprite); }
      else o.writeByte(0);
    }
    for (let i = 0; i < 5; i++) o.writeString(c.configActions[i] || '');
  }
  if (c.type === 3) o.writeByte(c.filled ? 1 : 0);
  if (c.type === 4 || c.type === 1) {
    o.writeByte(c.xTextAlignment || 0);
    o.writeByte(c.yTextAlignment || 0);
    o.writeByte(c.lineHeight || 0);
    o.writeShort(toU16(c.fontId));
    o.writeByte(c.textShadowed ? 1 : 0);
  }
  if (c.type === 4) { o.writeString(c.text || ''); o.writeString(c.alternateText || ''); }
  if (c.type === 1 || c.type === 3 || c.type === 4) o.writeInt(c.textColor || 0);
  if (c.type === 3 || c.type === 4) {
    o.writeInt(c.alternateTextColor || 0);
    o.writeInt(c.hoveredTextColor || 0);
    o.writeInt(c.alternateHoveredTextColor || 0);
  }
  if (c.type === 5) { o.writeInt(c.spriteId ?? -1); o.writeInt(c.alternateSpriteId ?? -1); }
  if (c.type === 6) {
    o.writeShort(toU16(c.modelId));
    o.writeShort(toU16(c.alternateModelId));
    o.writeShort(toU16(c.animation));
    o.writeShort(toU16(c.alternateAnimation));
    o.writeShort(c.modelZoom || 0);
    o.writeShort(c.rotationX || 0);
    o.writeShort(c.rotationZ || 0);
  }
  if (c.type === 7) {
    o.writeByte(c.xTextAlignment || 0);
    o.writeShort(toU16(c.fontId));
    o.writeByte(c.textShadowed ? 1 : 0);
    o.writeInt(c.textColor || 0);
    o.writeShort(c.xPitch || 0);
    o.writeShort(c.yPitch || 0);
    o.writeByte(c._t7flag || 0);
    for (let i = 0; i < 5; i++) o.writeString(c.configActions[i] || '');
  }
  if (c.type === 8) o.writeString(c.text || '');
  if (c.menuType === 2 || c.type === 2) {
    o.writeString(c.targetVerb || '');
    o.writeString(c.spellName || '');
    o.writeShort(c._spellFlags || 0);
  }
  if (c.menuType === 1 || c.menuType === 4 || c.menuType === 5 || c.menuType === 6) {
    o.writeString(c.tooltip || '');
  }
  return o.flip();
}

function orMinus(v) { return v === 0xFFFF ? -1 : v; }
function toU16(v) { return v == null || v === -1 ? 0xFFFF : (v & 0xFFFF); }
