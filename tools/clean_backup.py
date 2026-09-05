#!/usr/bin/env python3
"""Strip yy/yw from a legacy backup.txt and write data/backup_cleaned.json."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "backup.txt"
DEST = ROOT / "data" / "backup_cleaned.json"
DROP_KEYS = {"yy", "yw"}


def clean(payload: dict) -> dict:
    records = []
    for raw in payload.get("records", []):
        item = {k: v for k, v in raw.items() if k not in DROP_KEYS}
        records.append(item)
    return {
        "version": payload.get("version"),
        "backup_time": payload.get("backup_time"),
        "first_start": payload.get("first_start"),
        "records": records,
    }


def main() -> int:
    src = Path(sys.argv[1]) if len(sys.argv) > 1 else SOURCE
    dest = Path(sys.argv[2]) if len(sys.argv) > 2 else DEST
    if not src.is_file():
        print(f"missing source: {src}", file=sys.stderr)
        return 1
    payload = json.loads(src.read_text(encoding="utf-8"))
    cleaned = clean(payload)
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(
        json.dumps(cleaned, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    print(f"wrote {dest} ({len(cleaned['records'])} records)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
