# 数据契约

## 内部模型

| 字段 | 类型 | 含义 |
|------|------|------|
| date | LocalDate | 日历日 |
| masturbationCount | Int | 自慰次数（旧 `zw`） |
| intercourseCount | Int | 房事次数（旧 `fs`） |
| viewedPorn | Boolean | 看黄（旧 `kh`，非 0 为真） |
| nocturnalEmission | Boolean | 遗精（旧 `yj`，非 0 为真） |
| badHabit | BadHabit? | `STAY_UP_LATE` 熬夜 / `ALL_NIGHTER` 通宵 |
| trackingStartDate | LocalDate? | 开始逐日记录（旧 `first_start`） |

忽略 `yy`、`yw`。

## Room

- `daily_records`：主键 `dateEpochDay`（`LocalDate.toEpochDay()`）
- `app_meta`：单行，`trackingStartEpochDay`

## 旧备份（导入兼容）

```json
{
  "version": 2,
  "backup_time": "2026-09-05 16:02",
  "first_start": 20240816,
  "records": [
    {
      "calendar": 20240816,
      "zw": 0,
      "fs": 0,
      "kh": 0,
      "yj": 0,
      "badhabit": "熬夜"
    }
  ]
}
```

`calendar`、`first_start` 为 `yyyyMMdd` 整数。解析时 `ignoreUnknownKeys`，因此带 `yy`/`yw` 的原始文件也可导入。

## 本应用导出

```json
{
  "format": "navigator.backup",
  "version": 1,
  "exportedAt": "2026-09-05T16:02:00",
  "trackingStartDate": "2024-08-16",
  "records": [
    {
      "date": "2024-08-16",
      "masturbationCount": 0,
      "intercourseCount": 0,
      "viewedPorn": false,
      "nocturnalEmission": false,
      "badHabit": null
    }
  ]
}
```

`badHabit` 导出为 `STAY_UP_LATE` / `ALL_NIGHTER`。导入同时接受中文「熬夜」「通宵」。

清洗脚本：`python3 tools/clean_backup.py`，读取 `backup.txt`，写出 `data/backup_cleaned.json`（已 gitignore）。
