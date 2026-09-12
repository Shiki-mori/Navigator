# 自动打卡

领域用例 `AutoCheckIn`：进入应用且当日未打卡时，为 `[today-5, today]` 内的空缺日写入干净默认记录。

## 产品规则

| 项 | 规则 |
|----|------|
| 开关 | 始终开启，无设置项 |
| 触发 | 界面进入 `STARTED`（冷启动或从后台回到前台） |
| 前置 | 当日无记录；已有则整次跳过 |
| 窗口 | `today.minusDays(5)` 至 `today`，含两端，6 个自然日 |
| 写入 | 仅未记录日；干净默认 `DailyRecord(date)` |
| 覆盖 | 不覆盖已有记录 |
| 追踪日 | `date < trackingStartDate` 则跳过；无追踪日时由 `SaveCheckIn` 把第一天写入设为追踪日 |
| 提示 | 有写入则 Snackbar：`已自动打卡：YYYY-MM-DD、YYYY-MM-DD` |
| 非触发 | 底部 Tab 切换、编辑页进出（Activity 仍在前台） |

## 流程

```
进入界面 STARTED
        │
        ▼
  当日已有记录？ ──是──► 返回空列表
        │否
        ▼
  扫描 today-5 … today
        │
        ├─ 早于追踪日 → 跳过
        ├─ 已有记录   → 跳过
        └─ 空缺       → SaveCheckIn(干净默认)
        │
        ▼
  返回写入日期列表 → 非空则 Snackbar
```

## 挂钩

| 层 | 位置 | 职责 |
|----|------|------|
| domain | `AutoCheckIn`（`CheckInUseCases.kt`） | 判定、扫描、调用 `SaveCheckIn`，返回写入日期 |
| 装配 | `AppContainer.autoCheckIn` | 注入 repository / SaveCheckIn / Clock |
| UI | `NavigatorApp` | `lifecycle.repeatOnLifecycle(STARTED)` 每回前台调用一次；有结果走现有 Snackbar |

不放在 `HomeViewModel.init`：进程可在后台跨日存活，`init` 不会再次执行。

`repeatOnLifecycle(STARTED)` 在进入 `STARTED` 时跑完一次即结束；下次从 `STOPPED` 再进入才再跑。当日已被写入后第二次调用为空操作。

## 写入形态

```kotlin
DailyRecord(
    date = date,
    masturbationCount = 0,
    intercourseCount = 0,
    viewedPorn = false,
    nocturnalEmission = false,
    badHabit = null,
)
```

与手动保存共用 `SaveCheckIn`，因此仍拒绝未来日、校验次数非负、并在首次记录时设定追踪日。

## 例子

假设今天为 `2026-09-12`，窗口为 `2026-09-07` … `2026-09-12`。

| 当日 | 窗口内已有 | 追踪日 | 本次写入 |
|------|------------|--------|----------|
| 无记录 | 全空 | 无 | 07–12 共 6 天；追踪日 = 07 |
| 无记录 | 仅 09 有记录 | 已有且 ≤ 07 | 07、08、10、11、12 |
| 无记录 | 全空 | 2026-09-10 | 10、11、12（07–09 早于追踪日） |
| 已有记录 | 07、08 空缺 | 任意 | 无（整次跳过） |

## 测试

| 用例 | 期望 |
|------|------|
| 当日已有记录 | 不写入任何日期 |
| 窗口全空、无追踪日 | 写入 6 天；追踪日 = today-5 |
| 窗口中部已有记录 | 只补空缺，不改已有内容 |
| 追踪日晚于窗口起点 | 不写入追踪日之前 |
| 全部早于追踪日且当日也早于追踪日 | 不写入（当日被跳过则整窗无写；若当日 ≥ 追踪日则只写追踪日及之后的空缺） |

JVM 单测：`AutoCheckInTest`。真机：杀进程后打开且当日无记录应出现 Snackbar；当日已打卡再进后台再回前台不应再补历史空缺。
