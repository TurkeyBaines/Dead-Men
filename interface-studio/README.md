# Dead-Men Interface Studio

A standalone web application for **rendering, editing and saving OSRS interfaces**
(widgets / IF1 + IF3 components) and their **CS2 client scripts**, working directly
against your Dead-Men server cache (`main_file_cache.dat2` + `.idxN`).

It is a self-contained project — it has its own `package.json` and does not depend
on the Gradle server build. Point it at any compatible OSRS cache folder and go.

## What it does

- **Open a cache** — you tell it the folder containing `main_file_cache.dat2`
  and the `.idx*` files. It reads index 3 (interfaces), index 8 (sprites) and
  index 12 (CS2 scripts) using the exact same decode logic as the client.
- **Render** — interfaces are decoded, laid out using the real OSRS position/size
  modes (absolute / centred / right / 16384ths, parent-minus, proportional) and
  drawn with their real sprites, text, colours and rectangles.
- **Grouped tree** — every interface archive is a "main interface"; its components
  are shown as a parent/child tree (via each component's `parentId`), so multiple
  sub-components of one interface are nested rather than listed as separate items.
- **Edit** — change any property (position, size, layout modes, text, colour,
  sprite, model, transparency, …) with a live preview.
- **Easy interactions** — add right-click options with one click; the studio sets
  the correct `clickMask` option bits for you and can bind (or generate) a CS2
  script to run on click — all the CS2 plumbing is handled in the background.
- **CS2 Scripts tab** — view and edit the disassembly of any script (with named
  opcodes), including the scripts referenced by the open interface. Save edits
  back to the cache.
- **Create / copy** — make a brand-new interface, or copy an existing one to a new
  id (parent references are re-pointed automatically so the copy is self-contained).
- **Save to cache** — interfaces and scripts are re-encoded byte-for-byte,
  recompressed (gzip), written to new sectors in the data file, and the index +
  reference table (CRC, revision, file list) are updated so the client reloads them.
- **Render Mode** — preview the interface composited inside a fixed (765×503) or
  resizable game frame so you can see its final in-game position.

## Requirements

- Node.js 18+ (developed on Node 22)

## Install & run

```bash
cd interface-studio
npm install
npm start            # serves http://localhost:4173  (override with PORT=...)
```

Then open <http://localhost:4173> in a browser. On the start screen, enter (or click
the auto-detected suggestion for) your cache folder, e.g. `../DeadMen/Cache`, and
press **Open Cache**.

## ⚠️ Back up your cache first

Saving **writes to your cache files in place** (it appends to `main_file_cache.dat2`
and updates the `.idx` files). Always work on a copy until you're confident:

```bash
cp -r ../DeadMen/Cache ../DeadMen/Cache-backup
```

## How it's verified

The cache engine is validated against the bundled Dead-Men cache:

- **25,393 / 25,393** interface components decode → re-encode **byte-for-byte**.
- **3,161 / 3,161** CS2 scripts round-trip **byte-for-byte**.
- After a save, **all interface archives still pass CRC validation** and edits
  read back correctly from a fresh open.

See `npm run verify` notes in `server/cache/` — the decoders/encoders are ports of
the RuneLite cache library logic used by the client.

## Project layout

```
interface-studio/
  server/
    index.js              Express API + static host
    cache/
      io.js               RS binary stream reader/writer (cp1252 strings, bigsmart)
      crc32.js            CRC-32 (matches java.util.zip.CRC32)
      compression.js      Container compression (NONE / GZIP / BZIP2)
      store.js            .dat2/.idx sectors, reference table, archive packing
      interface.js        IF1 + IF3 component decode/encode
      script.js           CS2 decode/encode + disassembly
      sprite.js           Sprite decode + PNG encode
      opcodes.js          CS2 opcode name table
  client/
    index.html  styles.css  app.js   Single-page UI
```

## API (for reference)

| Method | Route | Purpose |
| --- | --- | --- |
| POST | `/api/open` | open a cache folder |
| GET | `/api/tree` | list interface archives |
| GET | `/api/interface/:id` | decoded components + referenced scripts |
| POST | `/api/interface/:id` | save an interface |
| POST | `/api/interface/copy` | copy interface to new id |
| POST | `/api/interface/new` | create a new interface |
| POST | `/api/interface/:id/component` | append a component |
| GET | `/api/sprite/:group/:frame.png` | sprite as PNG |
| GET | `/api/scripts` · `/api/script/:id` | list / read CS2 scripts |
| POST | `/api/script/:id` · `/api/script/new` | save / create CS2 scripts |
