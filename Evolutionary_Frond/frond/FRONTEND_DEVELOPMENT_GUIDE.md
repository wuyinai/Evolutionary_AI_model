# Evolutionary AI 前端开发规范

**文档版本**: v1.0  
**创建日期**: 2026-06-30  
**适用项目**: Evolutionary AI 前端项目  
**技术栈**: Vue 3 + TypeScript + Vite + Pinia

---

## 目录

1. [项目技术评估](#1-项目技术评估)
2. [问题诊断报告](#2-问题诊断报告)
3. [开发规范总纲](#3-开发规范总纲)
4. [项目目录结构规范](#4-项目目录结构规范)
5. [命名规范](#5-命名规范)
6. [代码风格规范](#6-代码风格规范)
7. [组件设计原则](#7-组件设计原则)
8. [状态管理最佳实践](#8-状态管理最佳实践)
9. [API请求处理规范](#9-api请求处理规范)
10. [错误处理机制](#10-错误处理机制)
11. [注释规范](#11-注释规范)
12. [Git提交规范](#12-git提交规范)
13. [性能优化指南](#13-性能优化指南)
14. [测试策略](#14-测试策略)
15. [工具配置与使用](#15-工具配置与使用)

---

## 1. 项目技术评估

### 1.1 技术栈分析

#### 核心技术栈
- **Vue 3.5.32**: 采用Composition API,使用`<script setup>`语法
- **TypeScript 6.0**: 类型系统完整,但存在any类型滥用问题
- **Vite 8.0.8**: 构建工具配置简洁,缺少优化配置
- **Pinia 3.0.4**: 状态管理合理,缺少持久化机制
- **Vue Router 5.0.4**: 路由配置清晰,采用路由守卫鉴权
- **Axios 1.17.0**: HTTP请求封装良好,错误处理不完善

#### 开发工具链
- **ESLint + Oxlint**: 双重lint检查,配置合理
- **Prettier 3.8.3**: 代码格式化工具
- **Vue DevTools**: 开发调试工具

### 1.2 项目结构评估

#### 当前目录结构
```
frond/
├── src/
│   ├── assets/          # 静态资源(CSS、图片)
│   ├── components/      # 组件(分类不够系统)
│   ├── composables/     # 组合式函数(仅1个)
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia状态管理
│   ├── types/           # TypeScript类型定义
│   ├── utils/           # 工具函数和API
│   └── views/           # 页面视图
├── public/              # 公共资源
└── dist/                # 构建输出
```

#### 评估结论
- **优点**: 目录划分基本合理,符合Vue 3项目标准结构
- **缺点**: 
  - components分类不够系统化(chat、layout、icons等分类需要优化)
  - composables利用率低,仅有useToast一个组合式函数
  - 缺少常量配置目录(constants)
  - 缺少hooks目录(与composables重复,建议合并)

### 1.3 组件设计评估

#### 组件分类统计
- **业务组件**: 40个Vue文件
- **布局组件**: 4个(layout目录)
- **功能组件**: chat、icons等
- **视图组件**: 17个views

#### 组件设计问题
1. **组件职责不单一**: ChatMainContent.vue超过300行代码,包含消息显示、输入框、模型选择等多个功能
2. **组件复用性低**: 大量内联SVG图标(约50+处),未封装为Icon组件
3. **缺少组件文档**: 组件缺少Props、Events、Slots的详细说明
4. **样式内联过多**: 部分组件使用scoped样式,但缺少全局样式变量

### 1.4 状态管理评估

#### Pinia Store结构
- **业务Store**: user、conversation、agent、modelConfig等9个
- **使用模式**: Composition API风格的`defineStore`
- **状态持久化**: 无localStorage/sessionStorage持久化机制

#### 评估结论
- **优点**: Store划分合理,状态管理清晰
- **缺点**: 
  - 缺少状态持久化策略(用户登录状态等需要持久化)
  - Store初始化时机不统一(有的在组件mounted,有的在router)
  - 缺少状态重置机制

### 1.5 API交互评估

#### API封装结构
- **request.ts**: Axios统一封装,拦截器配置合理
- **业务API**: 12个API文件(chat、agent、user、conversation等)
- **流式请求**: chat.ts实现SSE流式对话

#### 评估结论
- **优点**: API封装统一,拦截器处理认证
- **缺点**: 
  - 错误处理不完善,仅console.error输出
  - 缺少请求重试机制
  - 缺少请求取消机制
  - 缺少API文档和接口定义说明

### 1.6 性能评估

#### 性能指标分析
- **bundle大小**: dist目录约2MB,未配置代码分割
- **懒加载**: 路由组件采用动态import,但缺少预加载策略
- **资源优化**: 图片未压缩,CSS未提取公共样式
- **缓存策略**: 无Service Worker或HTTP缓存配置

#### 评估结论
- **优点**: 路由懒加载配置合理
- **缺点**: 
  - 缺少代码分割和chunk优化
  - 缺少资源压缩策略
  - 缺少性能监控机制

---

## 2. 问题诊断报告

### 2.1 代码质量问题

#### 严重问题(必须修复)

1. **console.log滥用** ⚠️
   - **统计**: 139处console.log/error/warn
   - **问题**: 生产代码中存在大量调试日志
   - **影响**: 性能损失、信息安全风险
   - **修复方案**: 
     ```typescript
     // 创建统一的日志工具
     export const logger = {
       debug: (message: string, ...args: any[]) => {
         if (import.meta.env.DEV) {
           console.log(`[DEBUG] ${message}`, ...args)
         }
       },
       error: (message: string, error?: Error) => {
         console.error(`[ERROR] ${message}`, error)
         // 生产环境可上报错误监控平台
       }
     }
     ```

2. **TODO未实现功能** ⚠️
   - **统计**: 3处TODO标记(Navigation.vue)
   - **问题**: 预留功能未实现,缺少追踪机制
   - **修复方案**: 使用Issue追踪系统或JSDoc标记

3. **类型定义不完整** ⚠️
   - **问题**: 部分API返回类型为any
   - **影响**: TypeScript类型安全失效
   - **示例**: 
     ```typescript
     // ❌ 错误示例(auth.ts)
     export const getUserInfo = (): any => {
       const userInfo = localStorage.getItem(USER_INFO_KEY)
       return userInfo ? JSON.parse(userInfo) : null
     }
     
     // ✅ 正确示例
     export const getUserInfo = (): User | null => {
       const userInfo = localStorage.getItem(USER_INFO_KEY)
       return userInfo ? JSON.parse(userInfo) as User : null
     }
     ```

#### 一般问题(建议修复)

4. **错误处理不统一** 
   - **问题**: 有的catch直接throw,有的console.error
   - **修复方案**: 建立统一错误处理机制(见第10章)

5. **注释缺失**
   - **问题**: 复杂逻辑缺少注释说明
   - **示例**: ChatMainContent.vue的流式处理逻辑复杂,但缺少注释
   - **修复方案**: 添加详细注释(见第11章)

6. **魔法数字和字符串**
   - **问题**: 状态码、错误码等硬编码
   - **示例**: `if (response.code === 200)`
   - **修复方案**: 定义常量枚举

### 2.2 命名不规范问题

#### 文件命名问题
- **不一致**: utils文件使用camelCase,types文件使用camelCase,组件文件使用PascalCase
- **建议**: 统一规范(见第5章)

#### 变量命名问题
- **不一致**: currentTask(驼峰)、isLoadingMessages(前缀不一致)、selectedRoleName
- **建议**: 统一命名风格(见第5章)

#### 函数命名问题
- **不一致**: fetchModelConfigs(动词)、loadConversations(动词)、getUserInfo(动词)
- **建议**: 
  - API请求: fetch/get/post/put/delete
  - 加载操作: load
  - 获取操作: get
  - 设置操作: set/update

### 2.3 组件设计问题

1. **组件过大**
   - ChatMainContent.vue: 300+行代码
   - 建议:拆分为MessageDisplay、InputBox、ModelSelector等子组件

2. **SVG图标未封装**
   - 统计:约50+处内联SVG
   - 建议:封装为Icon组件库

3. **缺少Props验证**
   - 问题:Props类型定义简单,缺少验证
   - 建议:添加required、default、validator

### 2.4 状态管理问题

1. **缺少持久化**
   - userStore的登录状态应持久化
   - 建议:使用pinia-plugin-persistedstate或localStorage

2. **Store初始化时机不统一**
   - modelConfigStore.init()在组件mounted调用
   - userStore.initUserState()在组件mounted调用
   - 建议:统一在App.vue或路由守卫中初始化

3. **缺少状态重置**
   - 问题:logout只清除token,未重置其他状态
   - 建议:添加reset方法

### 2.5 API请求问题

1. **缺少请求重试**
   - 建议:添加retry机制

2. **缺少请求取消**
   - 建议:添加AbortController支持

3. **错误处理不完善**
   - request.ts仅console.error输出
   - 建议:统一错误处理和用户提示

### 2.6 性能问题

1. **缺少代码分割**
   - vite.config.ts未配置手动chunk分割
   - 建议:配置manualChunks

2. **资源未压缩**
   - 图片、CSS未优化
   - 建议:配置vite-plugin-compression

3. **缺少预加载**
   - 路由组件仅懒加载,无预加载
   - 建议:使用router prefetch

---

## 3. 开发规范总纲

### 3.1 规范制定原则

1. **一致性**: 所有代码遵循统一风格
2. **可读性**: 代码易于理解和维护
3. **可扩展性**: 架构支持功能扩展
4. **性能优化**: 关注代码性能和用户体验
5. **类型安全**: TypeScript严格模式,杜绝any

### 3.2 规范执行机制

1. **自动化检查**: ESLint + Prettier + Oxlint
2. **代码审查**: Pull Request必须经过Review
3. **持续集成**: CI/CD流程集成规范检查
4. **文档更新**: 规范变更需同步更新文档

### 3.3 规范适用范围

- **前端开发**: 所有Vue 3相关代码
- **TypeScript**: 所有TS文件
- **样式编写**: CSS/SCSS文件
- **配置文件**: JSON/YAML配置

---

## 4. 项目目录结构规范

### 4.1 标准目录结构

```
frond/
├── .vscode/              # VSCode配置
│   └── settings.json     # 工作区设置
│   └── extensions.json   # 推荐扩展
├── public/               # 公共静态资源(不经过构建)
│   └── favicon.ico       # 网站图标
│   └── robots.txt        # SEO配置
├── src/                  # 源代码目录
│   ├── api/              # API接口定义(新增)
│   │   ├── index.ts      # API统一导出
│   │   ├── modules/      # API模块
│   │   │   ├── user.ts   # 用户API
│   │   │   ├── chat.ts   # 聊天API
│   │   │   └── agent.ts  # Agent API
│   ├── assets/           # 静态资源(经过构建)
│   │   ├── images/       # 图片资源
│   │   ├── styles/       # 全局样式
│   │   │   ├── variables.css  # CSS变量
│   │   │   ├── reset.css      # 样式重置
│   │   │   └── global.css     # 全局样式
│   ├── components/       # 公共组件
│   │   ├── common/       # 通用组件
│   │   │   ├── Button.vue
│   │   │   ├── Input.vue
│   │   │   ├── Modal.vue
│   │   │   └── Icon.vue  # SVG图标组件
│   │   ├── business/     # 业务组件
│   │   │   ├── ChatMessage.vue
│   │   │   ├── ModelSelector.vue
│   │   │   └── KnowledgeSelector.vue
│   │   └ layout/         # 布局组件
│   │   │   ├── MainLayout.vue
│   │   │   ├── Navigation.vue
│   │   │   └ Sidebar.vue
│   ├── composables/      # 组合式函数(Hooks)
│   │   ├── useAuth.ts    # 认证逻辑
│   │   ├── useRequest.ts # 请求逻辑
│   │   ├── useToast.ts   # 提示逻辑
│   │   └ useStorage.ts   # 存储逻辑
│   ├── constants/        # 常量定义(新增)
│   │   ├── index.ts      # 常量统一导出
│   │   ├── enums.ts      # 枚举定义
│   │   ├── http.ts       # HTTP状态码
│   │   └ api.ts          # API常量
│   ├── directives/       # 自定义指令(新增)
│   │   ├── index.ts      # 指令统一注册
│   │   ├── permission.ts # 权限指令
│   │   └ loading.ts      # 加载指令
│   ├── plugins/          # 插件配置(新增)
│   │   ├── index.ts      # 插件统一导出
│   │   ├── persistedState.ts # Pinia持久化
│   ├── router/           # 路由配置
│   │   ├── index.ts      # 路由实例
│   │   ├── routes/       # 路由模块
│   │   │   ├── chat.ts   # 聊天路由
│   │   │   ├── system.ts # 系统路由
│   │   ├── guards.ts     # 路由守卫
│   ├── stores/           # Pinia状态管理
│   │   ├── index.ts      # Store统一导出
│   │   ├── modules/      # Store模块
│   │   │   ├── user.ts
│   │   │   ├── conversation.ts
│   │   │   ├── modelConfig.ts
│   ├── types/            # TypeScript类型定义
│   │   ├── index.ts      # 类型统一导出
│   │   ├── api.ts        # API类型
│   │   ├── user.ts       # 用户类型
│   │   ├── chat.ts       # 聊天类型
│   │   ├── global.d.ts   # 全局类型声明
│   ├── utils/            # 工具函数
│   │   ├── index.ts      # 工具统一导出
│   │   ├── auth.ts       # 认证工具
│   │   ├── request.ts    # HTTP请求封装
│   │   ├── storage.ts    # 存储工具
│   │   ├── validate.ts   # 验证工具
│   │   ├── logger.ts     # 日志工具(新增)
│   │   ├── error.ts      # 错误处理(新增)
│   ├── views/            # 页面视图
│   │   ├── chat/         # 聊天模块
│   │   │   ├── ChatView.vue
│   │   │   ├── ChatMain.vue
│   │   │   ├── ChatSidebar.vue
│   │   ├── system/       # 系统管理
│   │   │   ├── UserView.vue
│   │   │   ├── RoleView.vue
│   │   │   ├── MenuView.vue
│   │   ├── auth/         # 认证模块
│   │   │   ├── LoginView.vue
│   │   │   ├── RegisterView.vue
│   ├── App.vue           # 根组件
│   ├── main.ts           # 入口文件
├── tests/                # 测试目录(新增)
│   ├── unit/             # 单元测试
│   │   ├── components/   # 组件测试
│   │   ├── utils/        # 工具测试
│   ├── e2e/              # E2E测试
│   ├── setup.ts          # 测试配置
├── .env.development      # 开发环境变量
├── .env.production       # 生产环境变量
├── .editorconfig         # 编辑器配置
├── .eslintrc.js          # ESLint配置
├── .prettierrc           # Prettier配置
├── .gitignore            # Git忽略配置
├── package.json          # 项目配置
├── tsconfig.json         # TypeScript配置
├── vite.config.ts        # Vite配置
└── README.md             # 项目说明
```

### 4.2 目录规范说明

#### 文件组织原则
1. **按功能分类**: 相关文件放置在同一目录
2. **就近原则**: 组件、样式、类型就近放置
3. **统一导出**: 每个目录使用index.ts统一导出
4. **命名一致**: 目录名使用kebab-case,文件名遵循命名规范

#### 目录职责说明

| 目录 | 职责 | 说明 |
|------|------|------|
| api | API接口定义 | 定义所有HTTP请求接口,替代utils下的API文件 |
| assets | 静态资源 | 图片、样式等需要构建的资源 |
| components | 公共组件 | 可复用的Vue组件 |
| composables | 组合式函数 | Vue 3 Composition API的可复用逻辑 |
| constants | 常量定义 | 枚举、常量、配置等 |
| directives | 自定义指令 | Vue自定义指令 |
| plugins | 插件配置 | Pinia插件、第三方插件配置 |
| router | 路由配置 | 路由定义和守卫 |
| stores | 状态管理 | Pinia Store定义 |
| types | 类型定义 | TypeScript接口、类型 |
| utils | 工具函数 | 纯函数工具,不包含API请求 |
| views | 页面视图 | 页面级Vue组件 |

---

## 5. 命名规范

### 5.1 文件命名规范

#### 组件文件
- **规范**: PascalCase(大驼峰)
- **示例**: 
  ```
  UserProfile.vue
  ChatMessage.vue
  ModelSelector.vue
  ```

#### TypeScript文件
- **规范**: camelCase(小驼峰)
- **示例**: 
  ```typescript
  userApi.ts
  chatApi.ts
  authUtils.ts
  ```

#### 目录命名
- **规范**: kebab-case(短横线)
- **示例**: 
  ```
  user-profile/
  chat-module/
  api-modules/
  ```

#### 常量文件
- **规范**: UPPER_SNAKE_CASE(大写蛇形)
- **示例**: 
  ```typescript
  HTTP_STATUS_CODES.ts
  API_ENDPOINTS.ts
  ```

### 5.2 变量命名规范

#### 基本变量
- **规范**: camelCase(小驼峰)
- **前缀**: 根据类型添加语义前缀
- **示例**: 
  ```typescript
  // 状态变量: is/has/can前缀
  isLoading: boolean
  hasError: boolean
  canEdit: boolean
  
  // 计数变量: count/number后缀
  messageCount: number
  pageNumber: number
  
  // 数组变量: List/Items后缀
  userList: User[]
  messageItems: Message[]
  
  // 对象变量: Info/Data后缀
  userInfo: UserInfo
  responseData: ResponseData
  
  // 普通变量
  userName: string
  currentModel: Model
  selectedId: string
  ```

#### 常量变量
- **规范**: UPPER_SNAKE_CASE(大写蛇形)
- **示例**: 
  ```typescript
  const API_BASE_URL = 'http://localhost:8234'
  const MAX_RETRY_COUNT = 3
  const DEFAULT_TIMEOUT = 10000
  ```

#### 枚举命名
- **规范**: PascalCase(大驼峰)
- **示例**: 
  ```typescript
  enum ExecutionStatus {
    IDLE = 'IDLE',
    RUNNING = 'RUNNING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED',
  }
  
  enum HttpStatusCode {
    OK = 200,
    BAD_REQUEST = 400,
    UNAUTHORIZED = 401,
    FORBIDDEN = 403,
    NOT_FOUND = 404,
    INTERNAL_SERVER_ERROR = 500,
  }
  ```

### 5.3 函数命名规范

#### 函数命名原则
- **动词开头**: 函数名应以动词开头
- **语义明确**: 函数名应清晰表达功能
- **参数暗示**: 函数名暗示参数类型

#### 函数动词规范

| 动词 | 含义 | 示例 |
|------|------|------|
| fetch | 从服务器获取数据 | fetchUserList, fetchModelConfig |
| get | 从本地/缓存获取 | getUserInfo, getToken |
| set | 设置本地数据 | setUserInfo, setToken |
| load | 加载/初始化 | loadConversations, loadMenus |
| save | 保存数据 | saveMessage, saveConfig |
| delete | 删除数据 | deleteUser, deleteConversation |
| update | 更新数据 | updateUser, updateTitle |
| create | 创建数据 | createConversation, createMessage |
| handle | 处理事件 | handleLogin, handleLogout |
| toggle | 切换状态 | toggleDropdown, toggleGroup |
| validate | 验证数据 | validateForm, validateEmail |
| format | 格式化数据 | formatDate, formatMessage |
| parse | 解析数据 | parseJwt, parseDocument |
| compute | 计算数据 | computeTotal, computeScore |
| render | 渲染视图 | renderMessage, renderChart |

#### 函数命名示例
```typescript
// ✅ 正确示例
async function fetchUserList(): Promise<User[]> {}
function getUserInfo(): User | null {}
function setToken(token: string): void {}
function validateEmail(email: string): boolean {}
function handleLogin(): void {}

// ❌ 错误示例
function user() {}              // 缺少动词
function getData() {}           // 动词不明确
function doSomething() {}       // 无意义动词
function userList() {}          // 缺少动词
```

### 5.4 组件命名规范

#### 组件文件命名
- **规范**: PascalCase + 功能描述
- **后缀**: View(页面)、Component(组件)、Modal(弹窗)、Card(卡片)
- **示例**: 
  ```
  ChatView.vue           // 页面组件
  UserListComponent.vue  // 列表组件
  AddModelModal.vue      // 弹窗组件
  UserInfoCard.vue       // 卡片组件
  ```

#### 组件注册命名
- **规范**: PascalCase
- **示例**: 
  ```vue
  <!-- 使用PascalCase注册 -->
  <script setup lang="ts">
  import UserProfile from './UserProfile.vue'
  </script>
  
  <template>
    <!-- 使用PascalCase使用 -->
    <UserProfile />
  </template>
  ```

#### 组件Props命名
- **规范**: camelCase
- **示例**: 
  ```typescript
  interface Props {
    userId: string
    modelConfig: ModelConfig
    isLoading: boolean
    onConfirm: () => void
  }
  
  const props = defineProps<Props>()
  ```

#### 组件Events命名
- **规范**: kebab-case(短横线)
- **示例**: 
  ```typescript
  const emit = defineEmits<{
    (e: 'update:user'): void
    (e: 'change-model'): void
    (e: 'submit-form'): void
  }>()
  
  emit('update:user')
  emit('change-model')
  ```

### 5.5 CSS命名规范

#### Class命名
- **规范**: BEM(Block Element Modifier)
- **格式**: block__element--modifier
- **示例**: 
  ```css
  /* Block */
  .chat-container {}
  
  /* Element */
  .chat-container__message {}
  .chat-container__input {}
  
  /* Modifier */
  .chat-container--active {}
  .chat-container__message--streaming {}
  ```

#### CSS变量命名
- **规范**: kebab-case(短横线)
- **前缀**: 根据类型添加前缀
- **示例**: 
  ```css
  /* 颜色变量: color前缀 */
  --color-primary: #4a7cf7;
  --color-secondary: #8a8aa0;
  --color-error: #ef4444;
  
  /* 间距变量: spacing前缀 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  
  /* 字体变量: font前缀 */
  --font-size-base: 14px;
  --font-size-lg: 18px;
  
  /* 边框变量: radius/border前缀 */
  --radius-sm: 4px;
  --radius-md: 8px;
  --border-color: #e5e7eb;
  ```

---

## 6. 代码风格规范

### 6.1 TypeScript代码风格

#### 类型定义风格
```typescript
// ✅ 接口定义:用于对象类型
interface User {
  id: string
  name: string
  email: string
  age?: number  // 可选属性
}

// ✅ 类型别名:用于联合类型、交叉类型
type Status = 'idle' | 'loading' | 'success' | 'error'
type Response<T> = {
  code: number
  message: string
  data: T
}

// ✅ 泛型使用
function fetchData<T>(url: string): Promise<Response<T>> {
  return axios.get<Response<T>>(url).then(res => res.data)
}

// ❌ 避免使用any
function processData(data: any) {}  // 不推荐

// ✅ 使用unknown或具体类型
function processData(data: unknown) {
  if (typeof data === 'string') {
    // 类型收窄
  }
}
```

#### 函数定义风格
```typescript
// ✅ 箭头函数:用于回调、工具函数
const getToken = (): string | null => {
  return localStorage.getItem('token')
}

// ✅ 普通函数:用于复杂逻辑
async function fetchUserList(): Promise<User[]> {
  try {
    const response = await get<User[]>('/users')
    return response.data
  } catch (error) {
    handleError(error)
    return []
  }
}

// ✅ 函数参数:使用接口定义
interface FetchOptions {
  url: string
  params?: Record<string, any>
  timeout?: number
}

function fetchData(options: FetchOptions): Promise<any> {}

// ❌ 避免过长参数列表
function fetchData(url: string, params: any, timeout: number, retry: number) {}
```

#### 异步处理风格
```typescript
// ✅ async/await:推荐使用
async function handleLogin() {
  try {
    const response = await login(formData)
    if (response.code === 200) {
      router.push('/')
    }
  } catch (error) {
    handleError(error)
  }
}

// ❌ 避免Promise.then链式调用(复杂场景)
login(formData)
  .then(response => {
    if (response.code === 200) {
      router.push('/')
    }
  })
  .catch(error => {
    handleError(error)
  })

// ✅ 并行请求:使用Promise.all
async function loadData() {
  const [users, models] = await Promise.all([
    fetchUsers(),
    fetchModels(),
  ])
}

// ✅ 错误处理:统一使用try-catch
try {
  await fetchData()
} catch (error) {
  if (error instanceof Error) {
    logger.error('请求失败', error)
  }
}
```

### 6.2 Vue 3组件风格

#### 组件基本结构
```vue
<template>
  <!-- 模板内容 -->
</template>

<script setup lang="ts">
// 1. 导入依赖
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

// 2. Props和Emits定义
interface Props {
  userId: string
}
const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'update'): void
}>()

// 3. Store和组合式函数
const userStore = useUserStore()

// 4. 响应式状态
const isLoading = ref(false)
const userInfo = ref<User | null>(null)

// 5. 计算属性
const userName = computed(() => userInfo.value?.name || '')

// 6. 方法
const handleUpdate = async () => {
  isLoading.value = true
  try {
    await updateUser()
    emit('update')
  } finally {
    isLoading.value = false
  }
}

// 7. 生命周期
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 样式内容 */
</style>
```

#### Props定义规范
```typescript
// ✅ 使用TypeScript接口定义Props
interface Props {
  userId: string            // 必填属性
  userName?: string         // 可选属性
  isLoading?: boolean       // 可选属性,默认值
  onUpdate?: () => void     // 回调函数
}

const props = withDefaults(defineProps<Props>(), {
  userName: '',
  isLoading: false,
})

// ❌ 避免使用runtime定义(除非需要验证)
const props = defineProps({
  userId: {
    type: String,
    required: true,
  },
})
```

#### Emits定义规范
```typescript
// ✅ 使用TypeScript类型定义Emits
const emit = defineEmits<{
  (e: 'update', user: User): void
  (e: 'delete', id: string): void
  (e: 'close'): void
}>()

// 发送事件
emit('update', user)
emit('delete', id)
emit('close')

// ❌ 避免使用runtime定义
const emit = defineEmits(['update', 'delete'])
```

#### 响应式状态规范
```typescript
// ✅ ref:用于基本类型
const count = ref<number>(0)
const userName = ref<string>('')
const isLoading = ref<boolean>(false)

// ✅ reactive:用于对象类型(不推荐,优先使用ref)
const state = reactive<{
  user: User | null
  settings: Settings
}>({
  user: null,
  settings: {},
})

// ✅ computed:用于计算属性
const fullName = computed(() => {
  return `${userInfo.value?.firstName} ${userInfo.value?.lastName}`
})

// ✅ watch:用于监听变化
watch(userInfo, (newVal, oldVal) => {
  console.log('用户信息变化:', newVal)
}, { deep: true })

// ✅ watchEffect:用于自动依赖追踪
watchEffect(() => {
  if (userId.value) {
    loadUserInfo(userId.value)
  }
})
```

### 6.3 代码缩进和空格

#### 缩进规范
- **标准**: 2个空格(不使用Tab)
- **配置**: .editorconfig中设置indent_size = 2

#### 空格规范
```typescript
// ✅ 函数参数:逗号后空格
function fetchData(url: string, params: object) {}

// ✅ 对象属性:冒号后空格
const user = {
  id: 1,
  name: 'John',
}

// ✅ 运算符:前后空格
const result = a + b * c
const isValid = age > 18 && name !== ''

// ✅ 括号:内部无空格
if (isValid) {
  // ...
}

// ✅ 数组:内部无空格
const list = [1, 2, 3]

// ❌ 避免多余空格
const user = { id: 1 , name : 'John' }
```

### 6.4 代码换行规范

#### 语句换行
```typescript
// ✅ 长语句换行:运算符在前
const result = 
  isFirstCondition &&
  isSecondCondition &&
  isThirdCondition

// ✅ 函数调用换行:参数对齐
async function handleComplexOperation(
  userId: string,
  operationType: OperationType,
  config: OperationConfig,
) {
  // ...
}

// ✅ 链式调用换行:点号在前
const result = await fetchData()
  .then(processData)
  .then(formatData)
  .catch(handleError)

// ❌ 避免过长单行代码(超过100字符)
const result = isFirstCondition && isSecondCondition && isThirdCondition && isFourthCondition
```

#### 对象和数组换行
```typescript
// ✅ 对象换行:多属性时换行
const user = {
  id: '123',
  name: 'John Doe',
  email: 'john@example.com',
  age: 25,
}

// ✅ 数组换行:多元素时换行
const permissions = [
  'read',
  'write',
  'delete',
  'admin',
]

// ✅ 单行对象/数组:元素较少时
const point = { x: 10, y: 20 }
const colors = ['red', 'green', 'blue']
```

---

## 7. 组件设计原则

### 7.1 组件分类原则

#### 组件分类
1. **基础组件(Base Components)**: 无业务逻辑,高度可复用
   - Button、Input、Icon、Modal、Card等
2. **业务组件(Business Components)**: 包含业务逻辑,领域内可复用
   - ChatMessage、ModelSelector、KnowledgeSelector等
3. **布局组件(Layout Components)**: 页面布局结构
   - MainLayout、Navigation、Sidebar等
4. **页面组件(Page Components)**: 路由对应的页面
   - ChatView、LoginView、HomeView等

### 7.2 组件职责原则

#### 单一职责原则(SRP)
- **原则**: 每个组件只负责一个功能
- **标准**: 组件代码不超过200行
- **拆分**: 复杂组件拆分为多个子组件

#### 示例:ChatMainContent拆分
```vue
<!-- ❌ 原组件:职责过多(300+行) -->
<template>
  <div class="chat-main-content">
    <!-- 消息区域 -->
    <!-- 输入框 -->
    <!-- 模型选择 -->
    <!-- 知识库选择 -->
  </div>
</template>

<!-- ✅ 拆分后:职责清晰 -->
<template>
  <div class="chat-main-content">
    <MessageDisplay :messages="messages" />
    <ChatInputBox 
      @send="handleSend"
      @model-change="handleModelChange"
    />
  </div>
</template>

<script setup lang="ts">
import MessageDisplay from './MessageDisplay.vue'
import ChatInputBox from './ChatInputBox.vue'
</script>
```

### 7.3 组件通信原则

#### Props Down, Events Up
```vue
<!-- 父组件 -->
<template>
  <ChildComponent 
    :data="parentData"
    @update="handleUpdate"
  />
</template>

<!-- 子组件 -->
<template>
  <button @click="emit('update', newData)">
    更新
  </button>
</template>

<script setup lang="ts">
const props = defineProps<{ data: string }>()
const emit = defineEmits<{
  (e: 'update', data: string): void
}>()
</script>
```

#### 跨组件通信
```typescript
// ✅ 使用Pinia Store
const userStore = useUserStore()

// ✅ 使用Provide/Inject(深层嵌套)
// 父组件
provide('theme', themeRef)

// 子组件
const theme = inject('theme')
```

### 7.4 组件Props设计

#### Props验证
```typescript
interface Props {
  // ✅ 必填属性
  userId: string
  
  // ✅ 可选属性+默认值
  size?: 'sm' | 'md' | 'lg'
  
  // ✅ 类型验证
  age?: number
  
  // ✅ 回调函数
  onConfirm?: () => void
}

const props = withDefaults(defineProps<Props>(), {
  size: 'md',
})
```

#### Props命名
```typescript
// ✅ 使用语义化命名
interface Props {
  isLoading: boolean      // 状态属性:is前缀
  hasError: boolean       // 状态属性:has前缀
  canEdit: boolean        // 状态属性:can前缀
  initialData: Data       // 初始值:initial前缀
  defaultModel: Model     // 默认值:default前缀
}

// ❌ 避免无意义命名
interface Props {
  loading: boolean
  error: boolean
  edit: boolean
}
```

### 7.5 组件样式规范

#### Scoped样式
```vue
<style scoped>
/* ✅ 使用scoped避免样式污染 */
.chat-container {
  /* 组件样式 */
}

/* ✅ 子组件样式穿透 */
.chat-container :deep(.child-class) {
  /* 子组件样式 */
}
</style>
```

#### CSS变量使用
```vue
<style scoped>
.button {
  /* ✅ 使用全局CSS变量 */
  background-color: var(--color-primary);
  padding: var(--spacing-md);
  border-radius: var(--radius-md);
}

/* ❌ 避免硬编码 */
.button {
  background-color: #4a7cf7;
  padding: 16px;
  border-radius: 8px;
}
</style>
```

### 7.6 组件文档规范

#### Props文档
```typescript
/**
 * 用户卡片组件
 * 
 * @component UserCard
 * @author 开发者姓名
 * @since 2026-06-30
 * 
 * @example
 * <UserCard 
 *   :user="currentUser"
 *   :show-actions="true"
 *   @edit="handleEdit"
 * />
 */
interface Props {
  /** 用户信息对象 */
  user: User
  
  /** 是否显示操作按钮 */
  showActions?: boolean
  
  /** 卡片尺寸 */
  size?: 'sm' | 'md' | 'lg'
}
```

---

## 8. 状态管理最佳实践

### 8.1 Pinia Store结构

#### Store基本结构
```typescript
// stores/modules/user.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  // 1. 状态定义
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const isLoading = ref<boolean>(false)
  
  // 2. 计算属性(Getters)
  const isLoggedIn = computed(() => !!token.value)
  const userName = computed(() => user.value?.name || '')
  
  // 3. 方法(Actions)
  const login = async (credentials: LoginCredentials) => {
    isLoading.value = true
    try {
      const response = await authApi.login(credentials)
      token.value = response.token
      user.value = response.user
    } finally {
      isLoading.value = false
    }
  }
  
  const logout = () => {
    token.value = null
    user.value = null
  }
  
  const reset = () => {
    user.value = null
    token.value = null
    isLoading.value = false
  }
  
  // 4. 返回所有状态和方法
  return {
    // 状态
    user,
    token,
    isLoading,
    // 计算属性
    isLoggedIn,
    userName,
    // 方法
    login,
    logout,
    reset,
  }
})
```

### 8.2 状态持久化

#### 使用pinia-plugin-persistedstate
```typescript
// plugins/persistedState.ts
import { createPersistedState } from 'pinia-plugin-persistedstate'

export const persistedStatePlugin = createPersistedState({
  storage: localStorage,  // 或sessionStorage
  key: (id) => `__persisted__${id}`,
  serializer: {
    serialize: JSON.stringify,
    deserialize: JSON.parse,
  },
})

// main.ts
import { createPinia } from 'pinia'
import { persistedStatePlugin } from '@/plugins/persistedState'

const pinia = createPinia()
pinia.use(persistedStatePlugin)
```

#### Store持久化配置
```typescript
export const useUserStore = defineStore('user', () => {
  // ... store实现
}, {
  persist: {
    key: 'user-store',
    paths: ['token', 'user'],  // 只持久化指定状态
    storage: localStorage,
  },
})
```

### 8.3 Store命名规范

#### Store文件命名
- **规范**: camelCase + Store功能
- **示例**: 
  ```typescript
  userStore.ts          // 用户状态
  conversationStore.ts  // 对话状态
  modelConfigStore.ts   // 模型配置状态
  ```

#### Store ID命名
- **规范**: camelCase
- **示例**: 
  ```typescript
  defineStore('user', () => {})
  defineStore('conversation', () => {})
  defineStore('modelConfig', () => {})
  ```

### 8.4 Store状态初始化

#### 统一初始化机制
```typescript
// App.vue
<script setup lang="ts">
import { useUserStore } from '@/stores/user'
import { useModelConfigStore } from '@/stores/modelConfig'

const userStore = useUserStore()
const modelConfigStore = useModelConfigStore()

// 应用启动时统一初始化
onMounted(async () => {
  // 初始化用户状态
  userStore.initUserState()
  
  // 初始化模型配置
  await modelConfigStore.init()
})
</script>
```

#### Store初始化方法
```typescript
export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string | null>(null)
  const user = ref<User | null>(null)
  
  /**
   * 初始化用户状态
   * - 从localStorage加载token
   * - 如果token有效,自动登录
   */
  const initUserState = async () => {
    const savedToken = localStorage.getItem('token')
    if (savedToken && !isTokenExpired(savedToken)) {
      token.value = savedToken
      await fetchUserInfo()
    }
  }
  
  return {
    token,
    user,
    initUserState,
  }
})
```

### 8.5 Store状态更新原则

#### 状态更新原则
1. **单一更新**: 每次只更新一个状态
2. **事务更新**: 相关状态批量更新
3. **不可变更新**: 使用新对象替换,不直接修改

```typescript
// ✅ 正确示例:使用新对象
const updateUser = (newUser: User) => {
  user.value = { ...user.value, ...newUser }
}

// ✅ 正确示例:批量更新
const updateUserInfo = (info: UserInfo) => {
  user.value = { ...user.value, ...info }
  token.value = info.token
}

// ❌ 错误示例:直接修改
user.value.name = newName  // 不推荐
```

---

## 9. API请求处理规范

### 9.1 API目录结构

#### API模块划分
```
src/api/
├── index.ts            # API统一导出
├── modules/            # API模块
│   ├── user.ts         # 用户API
│   ├── chat.ts         # 聊天API
│   ├── agent.ts        # Agent API
│   ├── model.ts        # 模型API
│   ├── knowledge.ts    # 知识库API
│   └ system.ts         # 系统API
```

### 9.2 API接口定义

#### API接口命名
```typescript
// api/modules/user.ts

// ✅ 接口命名:动词+名词
export const userApi = {
  // 获取操作
  getUserList: () => get<User[]>('/users'),
  getUserById: (id: string) => get<User>(`/users/${id}`),
  getCurrentUser: () => get<User>('/users/current'),
  
  // 创建操作
  createUser: (user: CreateUserDTO) => post<User>('/users', user),
  
  // 更新操作
  updateUser: (id: string, user: UpdateUserDTO) => put<User>(`/users/${id}`, user),
  
  // 删除操作
  deleteUser: (id: string) => del<void>(`/users/${id}`),
}
```

#### API类型定义
```typescript
// types/api.ts

// ✅ 统一API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: number
}

// ✅ 分页响应类型
export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
}

// ✅ API错误类型
export interface ApiError {
  code: number
  message: string
  details?: string
  stack?: string
}
```

### 9.3 请求封装规范

#### Axios实例配置
```typescript
// utils/request.ts

import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 添加token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求ID(用于追踪)
    config.headers['X-Request-ID'] = generateRequestId()
    
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 直接返回data
    return response.data
  },
  (error) => {
    // 统一错误处理
    handleApiError(error)
    return Promise.reject(error)
  }
)

// 导出请求方法
export const get = <T>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.get(url, config)
}

export const post = <T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
  return request.post(url, data, config)
}
```

### 9.4 错误处理机制

#### 统一错误处理
```typescript
// utils/error.ts

import { ApiError } from '@/types/api'
import { useToast } from '@/composables/useToast'

/**
 * API错误处理器
 */
export const handleApiError = (error: any): ApiError => {
  const toast = useToast()
  
  // 网络错误
  if (!error.response) {
    toast.error('网络连接失败,请检查网络')
    return {
      code: -1,
      message: '网络连接失败',
    }
  }
  
  // HTTP状态码处理
  const { status, data } = error.response
  
  switch (status) {
    case 400:
      toast.error('请求参数错误')
      return {
        code: 400,
        message: '请求参数错误',
        details: data.message,
      }
    
    case 401:
      // Token过期,跳转登录
      localStorage.removeItem('token')
      router.push('/login')
      toast.error('登录已过期,请重新登录')
      return {
        code: 401,
        message: '未授权',
      }
    
    case 403:
      toast.error('无权限访问')
      return {
        code: 403,
        message: '无权限',
      }
    
    case 404:
      toast.error('资源不存在')
      return {
        code: 404,
        message: '资源不存在',
      }
    
    case 500:
      toast.error('服务器错误')
      return {
        code: 500,
        message: '服务器错误',
      }
    
    default:
      toast.error('请求失败')
      return {
        code: status,
        message: '请求失败',
      }
  }
}
```

### 9.5 请求重试机制

#### 重试配置
```typescript
// utils/request.ts

/**
 * 带重试的请求
 */
export const retryRequest = async <T>(
  requestFn: () => Promise<T>,
  maxRetries: number = 3,
  retryDelay: number = 1000,
): Promise<T> => {
  let retries = 0
  
  while (retries < maxRetries) {
    try {
      return await requestFn()
    } catch (error) {
      retries++
      
      // 最后一次重试失败,抛出错误
      if (retries >= maxRetries) {
        throw error
      }
      
      // 等待重试
      await sleep(retryDelay * retries)
    }
  }
  
  throw new Error('请求重试失败')
}
```

### 9.6 请求取消机制

#### AbortController使用
```typescript
// composables/useRequest.ts

import { ref, onUnmounted } from 'vue'

export const useRequest = <T>() => {
  const abortController = ref<AbortController | null>(null)
  const isLoading = ref<boolean>(false)
  
  const fetch = async (url: string) => {
    // 取消之前的请求
    abortController.value?.abort()
    
    // 创建新的AbortController
    abortController.value = new AbortController()
    isLoading.value = true
    
    try {
      const response = await axios.get(url, {
        signal: abortController.value.signal,
      })
      return response.data
    } finally {
      isLoading.value = false
    }
  }
  
  // 组件卸载时取消请求
  onUnmounted(() => {
    abortController.value?.abort()
  })
  
  return {
    fetch,
    isLoading,
    abort: () => abortController.value?.abort(),
  }
}
```

---

## 10. 错误处理机制

### 10.1 错误分类

#### 错误类型
```typescript
// types/error.ts

/**
 * 错误类型枚举
 */
export enum ErrorType {
  NETWORK = 'NETWORK',           // 网络错误
  API = 'API',                   // API错误
  VALIDATION = 'VALIDATION',     // 验证错误
  BUSINESS = 'BUSINESS',         // 业务错误
  SYSTEM = 'SYSTEM',             // 系统错误
  UNKNOWN = 'UNKNOWN',           // 未知错误
}

/**
 * 应用错误基类
 */
export class AppError extends Error {
  type: ErrorType
  code: number
  details?: string
  
  constructor(
    message: string,
    type: ErrorType = ErrorType.UNKNOWN,
    code: number = -1,
    details?: string,
  ) {
    super(message)
    this.type = type
    this.code = code
    this.details = details
  }
}

/**
 * 业务错误
 */
export class BusinessError extends AppError {
  constructor(message: string, code: number, details?: string) {
    super(message, ErrorType.BUSINESS, code, details)
  }
}

/**
 * 验证错误
 */
export class ValidationError extends AppError {
  constructor(message: string, field?: string) {
    super(message, ErrorType.VALIDATION, 400, field)
  }
}
```

### 10.2 错误处理策略

#### 全局错误处理器
```typescript
// utils/errorHandler.ts

import { AppError, ErrorType } from '@/types/error'
import { useToast } from '@/composables/useToast'
import { logger } from '@/utils/logger'

/**
 * 全局错误处理器
 */
export const handleError = (error: Error | AppError): void => {
  const toast = useToast()
  
  // AppError:应用自定义错误
  if (error instanceof AppError) {
    switch (error.type) {
      case ErrorType.NETWORK:
        toast.error('网络连接失败')
        break
      
      case ErrorType.API:
        toast.error(error.message)
        break
      
      case ErrorType.VALIDATION:
        toast.warning(error.message)
        break
      
      case ErrorType.BUSINESS:
        toast.error(error.message)
        break
      
      case ErrorType.SYSTEM:
        toast.error('系统错误,请联系管理员')
        break
      
      default:
        toast.error('未知错误')
    }
    
    logger.error(`[${error.type}] ${error.message}`, error)
  }
  
  // 普通Error:系统错误
  else if (error instanceof Error) {
    toast.error('系统错误')
    logger.error('系统错误', error)
  }
}
```

### 10.3 Try-Catch使用规范

#### 异步错误捕获
```typescript
// ✅ 正确示例:统一错误处理
async function handleLogin() {
  try {
    const response = await authApi.login(credentials)
    if (response.code === 200) {
      router.push('/')
    }
  } catch (error) {
    handleError(error)
  }
}

// ✅ 正确示例:特定错误处理
async function handleDelete(id: string) {
  try {
    await deleteApi(id)
    toast.success('删除成功')
  } catch (error) {
    if (error instanceof BusinessError) {
      toast.error(error.message)
    } else {
      handleError(error)
    }
  }
}

// ❌ 错误示例:空catch或仅console
async function fetchData() {
  try {
    await api.getData()
  } catch (error) {
    console.error(error)  // 不推荐
  }
}
```

### 10.4 错误上报机制

#### 错误上报配置
```typescript
// utils/errorReporter.ts

interface ErrorReport {
  timestamp: number
  type: string
  message: string
  stack?: string
  url: string
  userAgent: string
  userId?: string
}

/**
 * 错误上报到监控平台
 */
export const reportError = async (error: Error | AppError): void => {
  const report: ErrorReport = {
    timestamp: Date.now(),
    type: error instanceof AppError ? error.type : 'UNKNOWN',
    message: error.message,
    stack: error.stack,
    url: window.location.href,
    userAgent: navigator.userAgent,
    userId: userStore.userId,
  }
  
  // 生产环境上报
  if (import.meta.env.PROD) {
    try {
      await axios.post('/api/error-report', report)
    } catch (e) {
      // 上报失败不处理,避免循环错误
    }
  }
}
```

---

## 11. 注释规范

### 11.1 注释类型

#### JSDoc注释
```typescript
/**
 * 用户API模块
 * 
 * 提供用户相关的HTTP请求接口
 * 
 * @module api/user
 * @author 开发者姓名
 * @since 2026-06-30
 */

/**
 * 获取用户列表
 * 
 * @function getUserList
 * @returns {Promise<ApiResponse<User[]>>} 用户列表响应
 * @throws {ApiError} 当请求失败时抛出API错误
 * 
 * @example
 * const response = await getUserList()
 * if (response.code === 200) {
 *   console.log(response.data)
 * }
 */
export const getUserList = (): Promise<ApiResponse<User[]>> => {
  return get<User[]>('/users')
}
```

#### 单行注释
```typescript
// ✅ 功能说明注释
const maxRetryCount = 3  // 最大重试次数

// ✅ 逻辑解释注释
if (user.age >= 18) {
  // 18岁以上可以访问成人内容
  showAdultContent()
}

// ✅ TODO注释(必须包含Issue编号)
// TODO: 实现用户头像上传功能 #123
function uploadAvatar() {}
```

#### 多行注释
```typescript
/*
 * 复杂逻辑说明
 * 
 * 1. 验证用户输入
 * 2. 检查权限
 * 3. 执行操作
 * 4. 返回结果
 */
async function handleComplexOperation() {
  // ...
}
```

### 11.2 注释内容规范

#### 必须注释的内容
1. **复杂算法**: 解释算法原理和步骤
2. **业务逻辑**: 解释业务规则和约束
3. **配置参数**: 解释参数含义和范围
4. **错误处理**: 解释错误处理策略
5. **TODO/FIXME**: 标记待实现功能或待修复问题

#### 不需要注释的内容
1. **简单逻辑**: 代码本身清晰易懂
2. **工具方法**: 标准库或常用方法
3. **自解释代码**: 变量和函数名已表达含义

### 11.3 Vue组件注释

#### 组件文档注释
```vue
<script setup lang="ts">
/**
 * 用户卡片组件
 * 
 * 展示用户基本信息和操作按钮
 * 
 * @component UserCard
 * @example
 * <UserCard 
 *   :user="currentUser"
 *   :show-actions="true"
 *   @edit="handleEdit"
 * />
 */

/**
 * 用户信息对象
 * 
 * @prop {User} user - 用户信息对象,必填
 */
interface Props {
  user: User
}

/**
 * 是否显示操作按钮
 * 
 * @prop {boolean} showActions - 是否显示操作按钮,默认false
 */
const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

/**
 * 编辑用户事件
 * 
 * @event edit - 编辑用户时触发
 * @param {User} user - 编辑后的用户信息
 */
const emit = defineEmits<{
  (e: 'edit', user: User): void
}>()
</script>
```

### 11.4 Store注释规范

#### Store文档注释
```typescript
/**
 * 用户状态管理Store
 * 
 * 管理用户登录状态、用户信息、权限等
 * 
 * @store user
 * @persist true  // 持久化配置
 * 
 * @example
 * const userStore = useUserStore()
 * 
 * // 登录
 * await userStore.login({ username, password })
 * 
 * // 检查登录状态
 * if (userStore.isLoggedIn) {
 *   console.log(userStore.userName)
 * }
 */
export const useUserStore = defineStore('user', () => {
  /**
   * 用户信息
   * 
   * @state {User | null} user - 当前用户信息,未登录时为null
   */
  const user = ref<User | null>(null)
  
  /**
   * 用户登录
   * 
   * @action login
   * @param {LoginCredentials} credentials - 登录凭证
   * @returns {Promise<void>}
   * @throws {BusinessError} 登录失败时抛出业务错误
   */
  const login = async (credentials: LoginCredentials): void => {
    // ...
  }
  
  return {
    user,
    login,
  }
})
```

---

## 12. Git提交规范

### 12.1 Commit Message格式

#### Commit Message结构
```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type类型说明

| Type | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | feat(chat): add message search feature |
| fix | 修复Bug | fix(user): fix login validation error |
| docs | 文档更新 | docs(api): update API documentation |
| style | 代码格式(不影响逻辑) | style: format code with prettier |
| refactor | 重构代码 | refactor(chat): split ChatMainContent component |
| perf | 性能优化 | perf: add lazy loading for images |
| test | 添加测试 | test(user): add unit tests for userStore |
| chore | 构建/工具变更 | chore: update vite configuration |
| revert | 回退提交 | revert: revert commit abc123 |

#### Scope范围说明
- **api**: API相关
- **chat**: 聊天模块
- **user**: 用户模块
- **system**: 系统管理
- **components**: 组件相关
- **stores**: 状态管理
- **utils**: 工具函数
- **styles**: 样式相关
- **config**: 配置文件

#### Commit Message示例
```bash
# 新功能
feat(chat): add message search feature

- Add search input component
- Implement search logic
- Add search result display

Closes #123

# Bug修复
fix(user): fix login validation error

The validation logic was incorrect when checking email format.

Fixes #456

# 重构
refactor(chat): split ChatMainContent component

- Extract MessageDisplay component
- Extract ChatInputBox component
- Simplify component structure

# 文档更新
docs(api): update API documentation

Add API response types and examples.

# 性能优化
perf: add lazy loading for images

Use IntersectionObserver to lazy load images.
```

### 12.2 分支命名规范

#### 分支类型

| 分支类型 | 命名格式 | 示例 |
|----------|----------|------|
| 主分支 | main/master | main |
| 开发分支 | develop | develop |
| 功能分支 | feature/<功能名> | feature/chat-search |
| Bug分支 | fix/<Bug名> | fix/login-validation |
| 发布分支 | release/<版本号> | release/v1.2.0 |
| 热修复分支 | hotfix/<Bug名> | hotfix/critical-error |

#### 分支命名示例
```bash
# 功能分支
feature/chat-search
feature/user-avatar-upload
feature/knowledge-base-management

# Bug分支
fix/login-validation-error
fix/chat-message-display
fix/model-selector-dropdown

# 发布分支
release/v1.2.0
release/v2.0.0-beta

# 热修复分支
hotfix/critical-security-error
hotfix/payment-failure
```

### 12.3 Git工作流程

#### Git Flow工作流程
```bash
# 1. 从develop创建功能分支
git checkout develop
git checkout -b feature/chat-search

# 2. 开发功能
git add .
git commit -m "feat(chat): add search input component"
git commit -m "feat(chat): implement search logic"

# 3. 合并到develop
git checkout develop
git merge feature/chat-search

# 4. 创建发布分支
git checkout -b release/v1.2.0

# 5. 测试和修复
git commit -m "fix(chat): fix search result display"

# 6. 合并到main和develop
git checkout main
git merge release/v1.2.0
git checkout develop
git merge release/v1.2.0

# 7. 打标签
git tag -a v1.2.0 -m "Release version 1.2.0"
```

### 12.4 Pull Request规范

#### PR标题格式
```
[类型] 功能描述

示例:
[Feature] 添加聊天消息搜索功能
[Fix] 修复用户登录验证错误
[Refactor] 重构ChatMainContent组件
```

#### PR描述模板
```markdown
## 变更类型
- [ ] 新功能(Feature)
- [ ] Bug修复(Fix)
- [ ] 重构(Refactor)
- [ ] 性能优化(Performance)
- [ ] 文档更新(Documentation)

## 变更说明
<!-- 详细描述本次变更的内容 -->

## 变更原因
<!-- 说明为什么需要这次变更 -->

## 测试情况
<!-- 描述测试方法和结果 -->

## 相关Issue
<!-- 关联的Issue编号 -->
Closes #123

## 检查清单
- [ ] 代码符合项目规范
- [ ] 已添加必要注释
- [ ] 已更新相关文档
- [ ] 已添加单元测试
- [ ] 所有测试通过
- [ ] 无Lint错误
```

---

## 13. 性能优化指南

### 13.1 代码分割优化

#### Vite配置优化
```typescript
// vite.config.ts

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    // 代码分割策略
    rollupOptions: {
      output: {
        manualChunks: {
          // Vue核心库单独打包
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          
          // UI库单独打包
          'ui-vendor': ['axios'],
          
          // 工具库单独打包
          'utils-vendor': ['lodash', 'dayjs'],
        },
      },
    },
    
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,  // 生产环境移除console
        drop_debugger: true,
      },
    },
    
    // chunk大小警告阈值
    chunkSizeWarningLimit: 500,
  },
})
```

### 13.2 资源优化

#### 图片优化
```typescript
// vite.config.ts

import viteImagemin from 'vite-plugin-imagemin'

export default defineConfig({
  plugins: [
    viteImagemin({
      gifsicle: {
        optimizationLevel: 3,
        interlaced: false,
      },
      optipng: {
        optimizationLevel: 7,
      },
      mozjpeg: {
        quality: 80,
      },
      svgo: {
        plugins: [
          {
            name: 'removeViewBox',
          },
        ],
      },
      webp: {
        quality: 80,
      },
    }),
  ],
})
```

#### CSS优化
```typescript
// vite.config.ts

import viteCompression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    // Gzip压缩
    viteCompression({
      verbose: true,
      algorithm: 'gzip',
      ext: '.gz',
    }),
  ],
})
```

### 13.3 懒加载优化

#### 路由懒加载
```typescript
// router/routes/chat.ts

export const chatRoutes = [
  {
    path: '/chat',
    name: 'chat',
    component: () => import('@/views/chat/ChatView.vue'),
    meta: {
      preload: true,  // 预加载标记
    },
  },
]
```

#### 组件懒加载
```vue
<script setup lang="ts">
import { defineAsyncComponent } from 'vue'

// 懒加载组件
const HeavyComponent = defineAsyncComponent(() =>
  import('./HeavyComponent.vue')
)

// 懒加载+加载状态
const HeavyComponentWithLoading = defineAsyncComponent({
  loader: () => import('./HeavyComponent.vue'),
  loadingComponent: LoadingSpinner,
  delay: 200,
  timeout: 3000,
})
</script>
```

### 13.4 预加载策略

#### 路由预加载
```typescript
// router/index.ts

import { useRouter } from 'vue-router'

const router = useRouter()

// 预加载下一个可能访问的路由
router.beforeEach((to, from, next) => {
  // 预加载目标路由组件
  if (to.meta.preload) {
    const component = to.matched[0]?.components?.default
    if (typeof component === 'function') {
      component()  // 触发预加载
    }
  }
  next()
})
```

### 13.5 缓存策略

#### HTTP缓存配置
```typescript
// vite.config.ts

export default defineConfig({
  server: {
    headers: {
      // 静态资源缓存
      'Cache-Control': 'public, max-age=31536000',
    },
  },
})
```

#### Service Worker配置
```typescript
// vite.config.ts

import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt'],
      manifest: {
        name: 'Evolutionary AI',
        short_name: 'EA',
        theme_color: '#4a7cf7',
        icons: [
          {
            src: 'assets/icon.png',
            sizes: '192x192',
            type: 'image/png',
          },
        ],
      },
    }),
  ],
})
```

### 13.6 性能监控

#### 性能指标收集
```typescript
// utils/performance.ts

/**
 * 性能监控工具
 */
export const performanceMonitor = {
  /**
   * 记录页面加载性能
   */
  recordPageLoad: () => {
    const timing = performance.timing
    const metrics = {
      // DNS查询时间
      dns: timing.domainLookupEnd - timing.domainLookupStart,
      
      // TCP连接时间
      tcp: timing.connectEnd - timing.connectStart,
      
      // 请求响应时间
      request: timing.responseEnd - timing.requestStart,
      
      // DOM解析时间
      domParse: timing.domInteractive - timing.responseEnd,
      
      // 资源加载时间
      resourceLoad: timing.loadEventStart - timing.domContentLoadedEventEnd,
      
      // 总加载时间
      total: timing.loadEventEnd - timing.navigationStart,
    }
    
    console.log('页面加载性能:', metrics)
    
    // 上报性能数据
    reportPerformance(metrics)
  },
  
  /**
   * 记录组件渲染性能
   */
  recordComponentRender: (componentName: string, duration: number) => {
    console.log(`${componentName} 渲染时间: ${duration}ms`)
  },
}
```

---

## 14. 测试策略

### 14.1 测试分类

#### 测试金字塔
```
       /\
      /  \  E2E测试(10%)
     /----\
    /      \  集成测试(20%)
   /--------\
  /          \  单元测试(70%)
 /____________\
```

#### 测试类型说明
- **单元测试**: 测试函数、组件、Store等最小单元
- **集成测试**: 测试模块间交互、API请求等
- **E2E测试**: 测试完整用户流程

### 14.2 单元测试规范

#### 测试框架配置
```typescript
// vitest.config.ts

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    coverage: {
      provider: 'istanbul',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'dist/',
      ],
    },
  },
})
```

#### 单元测试示例
```typescript
// tests/unit/utils/auth.test.ts

import { describe, it, expect, beforeEach } from 'vitest'
import { getToken, setToken, removeToken, isAuthenticated } from '@/utils/auth'

describe('认证工具函数', () => {
  beforeEach(() => {
    localStorage.clear()
  })
  
  describe('getToken', () => {
    it('应该返回null当localStorage中没有token', () => {
      expect(getToken()).toBeNull()
    })
    
    it('应该返回token当localStorage中有token', () => {
      localStorage.setItem('token', 'test-token')
      expect(getToken()).toBe('test-token')
    })
  })
  
  describe('setToken', () => {
    it('应该正确设置token到localStorage', () => {
      setToken('new-token')
      expect(localStorage.getItem('token')).toBe('new-token')
    })
  })
  
  describe('removeToken', () => {
    it('应该正确移除localStorage中的token', () => {
      localStorage.setItem('token', 'test-token')
      removeToken()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })
  
  describe('isAuthenticated', () => {
    it('应该返回false当没有token', () => {
      expect(isAuthenticated()).toBe(false)
    })
    
    it('应该返回true当有token', () => {
      setToken('test-token')
      expect(isAuthenticated()).toBe(true)
    })
  })
})
```

#### Store单元测试
```typescript
// tests/unit/stores/user.test.ts

import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('用户Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
  
  describe('登录功能', () => {
    it('应该正确设置登录状态', async () => {
      const userStore = useUserStore()
      
      await userStore.login({
        username: 'test',
        password: 'password',
      })
      
      expect(userStore.isLoggedIn).toBe(true)
      expect(userStore.token).toBeDefined()
    })
  })
  
  describe('登出功能', () => {
    it('应该正确清除登录状态', () => {
      const userStore = useUserStore()
      
      // 先设置登录状态
      userStore.token = 'test-token'
      userStore.user = { id: '1', name: 'Test' }
      
      // 执行登出
      userStore.logout()
      
      expect(userStore.isLoggedIn).toBe(false)
      expect(userStore.token).toBeNull()
      expect(userStore.user).toBeNull()
    })
  })
})
```

### 14.3 组件测试规范

#### Vue组件测试
```typescript
// tests/unit/components/UserCard.test.ts

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UserCard from '@/components/UserCard.vue'

describe('UserCard组件', () => {
  it('应该正确渲染用户信息', () => {
    const user = {
      id: '1',
      name: 'John Doe',
      email: 'john@example.com',
    }
    
    const wrapper = mount(UserCard, {
      props: { user },
    })
    
    expect(wrapper.find('.user-name').text()).toBe('John Doe')
    expect(wrapper.find('.user-email').text()).toBe('john@example.com')
  })
  
  it('应该触发edit事件当点击编辑按钮', async () => {
    const user = { id: '1', name: 'John' }
    const wrapper = mount(UserCard, {
      props: { user, showActions: true },
    })
    
    await wrapper.find('.edit-btn').trigger('click')
    
    expect(wrapper.emitted('edit')).toBeTruthy()
    expect(wrapper.emitted('edit')[0]).toEqual([user])
  })
  
  it('不应该显示操作按钮当showActions为false', () => {
    const wrapper = mount(UserCard, {
      props: {
        user: { id: '1', name: 'John' },
        showActions: false,
      },
    })
    
    expect(wrapper.find('.actions').exists()).toBe(false)
  })
})
```

### 14.4 E2E测试规范

#### Playwright配置
```typescript
// playwright.config.ts

import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:5173',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: { browserName: 'chromium' },
    },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:5173',
    reuseExistingServer: !process.env.CI,
  },
})
```

#### E2E测试示例
```typescript
// tests/e2e/login.spec.ts

import { test, expect } from '@playwright/test'

test.describe('登录流程', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
  })
  
  test('应该成功登录', async ({ page }) => {
    // 输入用户名和密码
    await page.fill('[name="username"]', 'testuser')
    await page.fill('[name="password"]', 'testpassword')
    
    // 点击登录按钮
    await page.click('button[type="submit"]')
    
    // 验证跳转到主页
    await expect(page).toHaveURL('/')
    
    // 验证显示用户名
    await expect(page.locator('.user-name')).toContainText('testuser')
  })
  
  test('应该显示错误当输入无效凭证', async ({ page }) => {
    await page.fill('[name="username"]', 'invalid')
    await page.fill('[name="password"]', 'invalid')
    await page.click('button[type="submit"]')
    
    // 验证显示错误消息
    await expect(page.locator('.error-message')).toBeVisible()
  })
})
```

### 14.5 测试覆盖率要求

#### 覆盖率标准
- **全局覆盖率**: ≥ 70%
- **关键模块覆盖率**: ≥ 90%
  - 认证模块(auth)
  - API请求模块(request)
  - 用户模块(user)
- **工具函数覆盖率**: ≥ 80%
- **组件覆盖率**: ≥ 60%

#### 覆盖率配置
```json
// package.json

{
  "scripts": {
    "test": "vitest",
    "test:coverage": "vitest run --coverage",
    "test:ui": "vitest --ui"
  }
}
```

---

## 15. 工具配置与使用

### 15.1 ESLint配置

#### ESLint配置文件
```typescript
// eslint.config.ts

import js from '@eslint/js'
import vue from 'eslint-plugin-vue'
import typescript from '@typescript-eslint/eslint-plugin'
import prettier from 'eslint-config-prettier'

export default [
  js.configs.recommended,
  ...vue.configs['flat/recommended'],
  {
    files: ['**/*.ts', '**/*.vue'],
    plugins: {
      '@typescript-eslint': typescript,
    },
    rules: {
      // TypeScript规则
      '@typescript-eslint/no-explicit-any': 'error',
      '@typescript-eslint/explicit-function-return-type': 'warn',
      '@typescript-eslint/no-unused-vars': 'error',
      
      // Vue规则
      'vue/multi-word-component-names': 'error',
      'vue/no-v-html': 'error',
      'vue/require-default-prop': 'warn',
      'vue/require-prop-types': 'error',
      
      // 通用规则
      'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
      'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'warn',
      'prefer-const': 'error',
      'no-var': 'error',
    },
  },
  prettier,
]
```

### 15.2 Prettier配置

#### Prettier配置文件
```json
// .prettierrc.json

{
  "semi": false,
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false,
  "trailingComma": "es5",
  "bracketSpacing": true,
  "arrowParens": "always",
  "endOfLine": "lf",
  "vueIndentScriptAndStyle": false,
  "htmlWhitespaceSensitivity": "ignore",
}
```

### 15.3 EditorConfig

#### EditorConfig文件
```ini
# .editorconfig

root = true

[*]
charset = utf-8
indent_style = space
indent_size = 2
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.md]
trim_trailing_whitespace = false

[*.{yml,yaml}]
indent_size = 2
```

### 15.4 TypeScript配置

#### TypeScript配置
```json
// tsconfig.json

{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    
    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    
    /* Linting options */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noImplicitReturns": true,
    "noImplicitAny": true,
    "strictNullChecks": true,
    
    /* Path mapping */
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue"],
  "exclude": ["node_modules", "dist"]
}
```

### 15.5 VSCode配置

#### VSCode设置
```json
// .vscode/settings.json

{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": "explicit"
  },
  "typescript.tsdk": "node_modules/typescript/lib",
  "typescript.enablePromptUseWorkspaceTsdk": true,
  "vue.inlayHints.missingProps": true,
  "vue.inlayHints.inlineHandlerLeading": true,
  "[vue]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  }
}
```

#### VSCode推荐扩展
```json
// .vscode/extensions.json

{
  "recommendations": [
    "vue.volar",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint",
    "vue.vscode-typescript-vue-plugin"
  ]
}
```

---

## 附录

### A. 参考资源

#### Vue 3官方文档
- [Vue 3官方文档](https://vuejs.org/)
- [Vue Router官方文档](https://router.vuejs.org/)
- [Pinia官方文档](https://pinia.vuejs.org/)

#### TypeScript官方文档
- [TypeScript官方文档](https://www.typescriptlang.org/)
- [TypeScript最佳实践](https://www.typescriptlang.org/docs/handbook/declaration-files/do's-and-don'ts.html)

#### Vite官方文档
- [Vite官方文档](https://vitejs.dev/)
- [Vite插件生态](https://vitejs.dev/plugins/)

#### 测试框架文档
- [Vitest官方文档](https://vitest.dev/)
- [Playwright官方文档](https://playwright.dev/)

### B. 常见问题FAQ

#### Q1: 如何处理TypeScript类型错误?
**A**: 使用strict模式,避免any类型,合理使用unknown和类型收窄。

#### Q2: 如何优化组件性能?
**A**: 使用v-if/v-show合理控制渲染,使用computed缓存计算结果,使用虚拟滚动处理大数据列表。

#### Q3: 如何管理复杂的组件状态?
**A**: 使用Pinia Store管理全局状态,使用Provide/Inject管理深层嵌套状态,避免Props过度传递。

#### Q4: 如何处理API请求错误?
**A**: 统一使用handleError处理,添加Toast提示,上报错误到监控平台。

#### Q5: 如何编写高质量的测试?
**A**: 遵循测试金字塔原则,70%单元测试+20%集成测试+10%E2E测试,关注核心业务逻辑测试覆盖率。

---

**文档维护**: 本规范文档由前端团队共同维护,如有疑问或建议请联系团队负责人。

**更新记录**: 
- 2026-06-30: 初版发布

---

**结束语**: 本规范旨在提升团队协作效率和代码质量,请所有团队成员严格遵守规范,共同维护项目质量。