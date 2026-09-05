# Redmi Note 13 Pro 安装说明

在 Linux 开发机用 USB 调试安装，避免 HyperOS 拦截「未知来源」APK。

## 手机

1. **设置 → 我的设备 → 全部参数 → 连点 OS 版本**，打开开发者选项。
2. **设置 → 更多设置 → 开发者选项**，开启：
   - USB 调试
   - USB 安装
   - USB 调试（安全设置）（安装时手机上要点允许）
3. 用数据线连接电脑，通知栏 USB 模式选 **传输文件（MTP）**，不要停在「仅充电」。
4. 首次连接会弹出 RSA 授权，勾选始终允许。

## 电脑

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
adb devices
```

状态应为 `device`。若为 `unauthorized`：在手机上点允许 USB 调试。没有弹窗时，拔线重插，或开关一次 USB 调试。

一键安装并推送清洗后的备份：

```bash
./tools/install_device.sh
```

或：

```bash
./gradlew :app:installDebug
adb push data/backup_cleaned.json /sdcard/Download/backup_cleaned.json
```

然后打开「打卡」→ 设置 → 导入备份 → 选择 `Download/backup_cleaned.json`。

## 常见问题

- 列表为空：换数据线或 USB 口，确认传输文件模式。
- 安装失败 `INSTALL_FAILED_USER_RESTRICTED`：打开 USB 安装、USB 调试（安全设置）。
- 仍失败：开发者选项里临时关闭 MIUI/HyperOS 优化后重试。
