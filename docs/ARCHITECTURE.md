# 架构

单 module `app`，按包分层。UI 不直接访问 Room。

```
ui (Compose + ViewModel)
        ↓
domain (模型、纯函数规则、用例接口)
        ↓
data (Room、JSON 解析、SAF 文件读写)
```

## 包

- `dev.phrolova.navigator.domain.model` — `DailyRecord`、`Streak`、`Stats`、`BadHabit`
- `dev.phrolova.navigator.domain` — `StreakCalculator`、`StatsCalculator`、`BackupParser`、`RecordRepository`
- `dev.phrolova.navigator.domain.usecase` — 打卡、首页、月历、统计、导入导出
- `dev.phrolova.navigator.data.local` — Room Entity / DAO / Database
- `dev.phrolova.navigator.data` — `RoomRecordRepository`、文件网关
- `dev.phrolova.navigator.ui` — 首页 / 月历 / 统计 / 设置 / 编辑日

## 技术

Kotlin 2.0、Jetpack Compose、Material 3、Navigation Compose、Room、KSP、kotlinx.serialization、ViewModel、协程。`minSdk 26`，`targetSdk 35`。无 INTERNET 权限。

## 依赖方向

`Clock` 抽象「今天」，便于单测连续日。解析、连续日、统计均为 JVM 纯逻辑，不依赖 Android SDK。
