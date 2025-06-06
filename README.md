# KDE Connect Android (Shizuku-supported fork)

This is a personal fork of the [KDE Connect Android](https://github.com/KDE/kdeconnect-android) project, focused on **bypassing Android 10+ clipboard access restrictions** using the [Shizuku](https://shizuku.rikka.app/) service.

本项目是 [KDE Connect Android](https://github.com/KDE/kdeconnect-android) 的一个个人分支，目的是通过 [Shizuku](https://shizuku.rikka.app/) 服务**绕过 Android 10+ 的剪贴板访问限制**。

---

## ✨ Key Features | 主要特性

- 🔓 **Clipboard access without `READ_LOGS`**  
  利用 Shizuku 获取剪贴板权限，无需申请 `READ_LOGS`（该权限无法在普通应用中使用）。
  
- ⚙️ **No need for manual ADB commands**  
  无需用户手动执行 ADB 授权命令，Shizuku 服务可通过图形界面一次性授权。
---

## 📦 Usage | 使用说明

1. **Install [Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) and this app on your device.**  
   **在设备上安装 [Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) 和本应用**

2. **Start Shizuku service by following the in-app guide (via wireless debugging or root).**  
   **根据 Shizuku 内的引导启动服务（无线调试或 root 启动）**

3. **Go to Shizuku's "Apps" tab and allow this app to use Shizuku.**  
   **进入 Shizuku 的“已授权应用”页面，允许 KDE Connect 使用 Shizuku 权限**

4. **Launch KDE Connect and grant "display over other apps" permission in system settings.**  
**打开 KDE Connect 应用，并在系统设置中授予“在其他应用上层显示”权限**


---

## 🔐 Permissions 权限说明

This build does **not request `READ_LOGS`** or use direct ADB shell.  
Clipboard access is handled via **Shizuku service**, which can be launched using **wireless debugging** or **root**.

本版本不会请求 `READ_LOGS` 权限，也不使用传统 ADB 命令手动授权。  
剪贴板访问通过 **Shizuku 服务**实现，Shizuku 可使用**无线调试或 root 权限启动**。

---

## 🚫 Disclaimer | 注意事项

- This is an unofficial personal fork and **will not be merged upstream**.
- Intended for personal experimentation or niche use cases only.
- Requires Android 10 or newer.
- The minimum SDK level has been increased and may not be suitable for general use.

本项目是一个**非官方的个人修改版本**，不会被合并到 KDE Connect 主线中。  
仅适用于个人实验或特殊需求场景，**最低支持 Android 10**，不建议作为主力版本使用。

---

## ❤️ Acknowledgements 致谢

* [KDE Connect](https://kdeconnect.kde.org/)
* [Shizuku](https://github.com/RikkaApps/Shizuku)
* Friends who helped with testing during development  
  感谢在开发测试阶段提供实际使用反馈的朋友们  
  [@mangranbuzhi](https://github.com/mangranbuzhi)

---

## 📫 Contact

You’re welcome to fork or report issues, though this repo may not be actively maintained long-term.

欢迎感兴趣的开发者 Fork 使用或提交 issue。该仓库为个人试验项目，可能不会长期维护。
