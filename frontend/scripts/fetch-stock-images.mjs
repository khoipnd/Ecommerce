import { mkdir, readFile, writeFile } from "node:fs/promises";
import { join, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { STOCK_GROUPS } from "../src/data/stockImages.js";

const stockRoot = resolve(fileURLToPath(new URL("../src/assets/stock/", import.meta.url)));
const force = process.argv.includes("--force");
const entries = Object.entries(STOCK_GROUPS).flatMap(([group, photos]) =>
    photos.map(photo => ({ group, ...photo }))
);
const totals = Object.fromEntries(Object.keys(STOCK_GROUPS).map(group => [group, { ok: 0, failed: 0, skipped: 0 }]));

await mkdir(stockRoot, { recursive: true });
await writeFile(
    join(stockRoot, "CREDITS.md"),
    [
        "# Nguồn ảnh stock",
        "",
        "Ảnh được tải từ Unsplash theo [Unsplash License](https://unsplash.com/license).",
        "",
        ...entries.map(({ group, file, credit, url }) => `- **${group}/${file}** — ${credit} — [Ảnh gốc](${url})`),
        "",
    ].join("\n"),
    "utf8"
);

async function fetchPhoto({ group, file, url }) {
    const destination = join(stockRoot, group, file);
    await mkdir(join(stockRoot, group), { recursive: true });

    if (!force) {
        try {
            if ((await readFile(destination)).length > 0) {
                totals[group].skipped += 1;
                console.log(`SKIP ${group}/${file}`);
                return;
            }
        } catch (error) {
            if (error.code !== "ENOENT") throw error;
        }
    }

    const response = await fetch(url, { signal: AbortSignal.timeout(20000) });
    if (!response.ok || !response.headers.get("content-type")?.startsWith("image/")) {
        throw new Error(`HTTP ${response.status}, ${response.headers.get("content-type") ?? "unknown content type"}`);
    }
    const bytes = Buffer.from(await response.arrayBuffer());
    if (bytes.length === 0) throw new Error("empty image");
    await writeFile(destination, bytes);
    totals[group].ok += 1;
    console.log(`OK   ${group}/${file} (${Math.round(bytes.length / 1024)} KB)`);
}

// A small worker pool keeps the one-time download reasonably quick.
let next = 0;
await Promise.all(Array.from({ length: 5 }, async () => {
    while (next < entries.length) {
        const entry = entries[next++];
        try {
            await fetchPhoto(entry);
        } catch (error) {
            totals[entry.group].failed += 1;
            console.warn(`FAIL ${entry.group}/${entry.file}: ${error.message}`);
        }
    }
}));

for (const [group, { ok, failed, skipped }] of Object.entries(totals)) {
    console.log(`${group}: OK ${ok}, FAIL ${failed}, SKIP ${skipped}`);
}
if (Object.values(totals).some(({ failed }) => failed > 0)) process.exitCode = 1;
