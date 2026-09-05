#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
BACKUP="$ROOT/data/backup_cleaned.json"

if [[ ! -f "$APK" ]]; then
  echo "APK missing; build first: ./gradlew :app:assembleDebug" >&2
  exit 1
fi
if [[ ! -f "$BACKUP" ]]; then
  python3 "$ROOT/tools/clean_backup.py"
fi

state="$(adb devices | awk 'NR>1 && $1 != "" {print $2; exit}')"
if [[ "$state" != "device" ]]; then
  echo "手机未就绪（当前: ${state:-none}）。请在 Redmi Note 13 Pro 上点允许 USB 调试。" >&2
  adb devices -l
  exit 1
fi

adb install -r "$APK"
adb push "$BACKUP" /sdcard/Download/backup_cleaned.json
echo "已安装，并推送备份到 Download/backup_cleaned.json"
echo "打开「打卡」→ 设置 → 导入备份 → 选择该文件。"
