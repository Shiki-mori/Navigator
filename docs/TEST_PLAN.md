# 真机验收清单（Redmi Note 13 Pro）

## 已在本机自动完成

- 单元测试（解析、连续日、统计、导入导出往返、全量 `backup.txt` 896 条）通过
- Debug APK：`app/build/outputs/apk/debug/app-debug.apk`
- 清洗备份：`data/backup_cleaned.json`（896 条，无 `yy`/`yw`）
- 电脑已识别到 USB 设备 `737e6c2e`，当前状态为 `unauthorized`（需在手机上点允许 USB 调试后才能安装）

授权后执行：

```bash
./tools/install_device.sh
```

## 真机手工项

- [ ] `adb devices` 为 `device`
- [ ] 安装后桌面出现「打卡」
- [ ] 设置 → 导入 `backup_cleaned.json`，提示条数与源文件 `records` 一致（896）
- [ ] 首页连续日与「今天是否破戒」符合规则（自慰或看黄即破戒；2026-09-05 备份为破戒，当前连续应为 0）
- [ ] 修改今天并保存，杀进程后数据仍在
- [ ] 月历能区分干净 / 破戒 / 未记录，可打开历史日编辑
- [ ] 不能选择未来日期
- [ ] 统计页本月 / 今年 / 全部数字合理
- [ ] 导出 JSON，再导入后记录与开始追踪日恢复
- [ ] 导入带 `yy`/`yw` 的原始 `backup.txt` 也能成功
