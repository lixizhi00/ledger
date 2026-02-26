# Ledger App (UniApp)

一套代码同时生成 **微信小程序** 和 **iOS App**。

## 功能

- **首页**：今年至今 / 本月 / 本日总支出，入口：预算、记账、分类
- **记账**：金额、分类、时间、备注、完成
- **预算**：月预算与年预算（按分类可编辑），月×12=年、年÷12=月
- **分类**：一级 / 二级分类，增删改，二级可排序

## 运行与预览

### 1. 浏览器预览（H5，最快）

```bash
npm install
npm run dev:h5
```

终端会输出本地地址（如 `http://localhost:5173`），用浏览器打开即可预览。

### 2. 微信小程序预览

```bash
npm install
npm run dev:mp-weixin
```

保持该命令运行，然后：

1. 打开 **微信开发者工具**
2. 选择「导入项目」→ 项目目录选 **`dist/dev/mp-weixin`**（不要选项目根目录）
3. 在模拟器或真机预览中查看

### 3. iOS App 预览

- 用 **HBuilderX** 打开本项目，用其「运行 → 运行到手机或模拟器」进行真机/模拟器预览；或  
- 在项目根目录执行：`npm run dev:app`（需已配置 iOS 开发环境），再在 Xcode/模拟器中运行。

---

**打包：** `npm run build:mp-weixin` 生成小程序包；`npm run build:h5` 生成 H5；`npm run build:app` 生成 App。

## 技术栈

- UniApp (Vue 3)
- 本地存储：`uni.setStorageSync` / `uni.getStorageSync`
