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

## 打包与发布

### 微信小程序打包 + 发布

**1. 打包**

在项目根目录执行：

```bash
npm install
npm run build:mp-weixin
```

打包产物在 **`dist/build/mp-weixin`**。

**2. 用微信开发者工具打开**

- 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
- 打开开发者工具 →「导入项目」
- 项目目录选择：**`dist/build/mp-weixin`**（不要选项目根目录）
- AppID：使用在 [微信公众平台](https://mp.weixin.qq.com/) 注册的小程序 AppID（体验可用「测试号」）

**3. 上传代码**

- 在开发者工具顶部点击「**上传**」
- 填写版本号、项目备注后上传
- 上传后可在 **微信公众平台 → 版本管理 → 开发版本** 中看到

**4. 提交审核与发布**

- 登录 [微信公众平台](https://mp.weixin.qq.com/)
- **版本管理** → 在「开发版本」中选择刚上传的版本 →「**提交审核**」
- 按提示填写类目、测试账号等，等待审核
- 审核通过后，在版本管理中点击「**发布**」即可正式上线

### 其他平台打包

- **H5**：`npm run build:h5`，产物在 `dist/build/h5`
- **App**：`npm run build:app`，需在 HBuilderX 或 Xcode 中继续打包/签名

## 技术栈

- UniApp (Vue 3)
- 本地存储：`uni.setStorageSync` / `uni.getStorageSync`
