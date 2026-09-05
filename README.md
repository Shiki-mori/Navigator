# Navigator

个人本地打卡应用（Kotlin + Jetpack Compose）。数据只存在手机上。

## 构建

需要 JDK 17 与 Android SDK（`local.properties` 中的 `sdk.dir`）。

```bash
python3 tools/clean_backup.py          # 清洗 backup.txt → data/backup_cleaned.json
./gradlew :app:testDebugUnitTest
./tools/install_device.sh              # 见 docs/DEVICE_SETUP.md
```

## 文档

- [需求](docs/REQUIREMENTS.md)
- [架构](docs/ARCHITECTURE.md)
- [数据契约](docs/DATA.md)
- [红米安装](docs/DEVICE_SETUP.md)
- [真机验收](docs/TEST_PLAN.md)

原始 `backup.txt` 含个人记录，不要提交到 Git。
