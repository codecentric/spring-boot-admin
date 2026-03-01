import { describe, it, expect } from "vitest";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

function walk(dir: string, out: string[]) {
  const entries = fs.readdirSync(dir, { withFileTypes: true });
  for (const e of entries) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) {
      walk(p, out);
    } else {
      out.push(p);
    }
  }
}

describe("ui click affordance", () => {
  it("applications/instances list should have pointer cursor rule", () => {
    const here = path.dirname(fileURLToPath(import.meta.url));
    const root = path.resolve(here, "..");
    const files: string[] = [];
    walk(root, files);

    const rxCursor = /cursor\s*:\s*pointer/i;
    const rxKw = /(applications|instances|instance|application)/i;

    let hit = false;
    for (const f of files) {
      const lower = f.toLowerCase();
      if (
        lower.endsWith(".css") ||
        lower.endsWith(".scss") ||
        lower.endsWith(".vue") ||
        lower.endsWith(".ts") ||
        lower.endsWith(".tsx") ||
        lower.endsWith(".js") ||
        lower.endsWith(".jsx")
      ) {
        const text = fs.readFileSync(f, "utf8");
        if (rxCursor.test(text) && rxKw.test(text)) {
          hit = true;
          break;
        }
      }
    }

    expect(hit).toBe(true);
  });
});
