# AI角色管理系统实现方案

## 一、需求概述

实现目标：
1. 支持创建AI角色（AI Persona）
2. AI角色可以关联多个文档文件作为系统提示词
3. AI角色也支持纯文本系统提示词
4. 上传的文档支持预览功能
5. 切换不同的AI角色时，模型的系统提示词动态变化
6. 系统提示词构建方式完全自定义

核心特性：
- 一个角色可以关联多个文档
- 支持纯文本系统提示词
- 文档预览功能
- 不需要版本管理（简单实现）

## 二、当前状态分析

### 2.1 基础设施现状

**文档上传和存储** ([MinioService.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/MinioService.java)):
- 完整的文件上传、下载、删除功能
- 支持MinIO对象存储
- 支持生成预览URL（临时访问链接）
- 可直接使用，无需修改

**文档解析服务** ([DocumentParserService.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/DocumentParserService.java)):
- 支持PDF、Word、TXT等文档格式解析
- 提取文本内容功能完善
- 可直接用于解析角色文档内容

**系统提示词传递机制** ([DynamicChatStrategy.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/strategy/DynamicChatStrategy.java)):
- 当前未使用系统提示词
- ChatClient调用时可以添加 `.system()` 方法
- 需要修改以支持动态系统提示词

**对话请求结构** ([ChatRequestDTO.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/entity/dto/ChatRequestDTO.java)):
- 当前包含configId、message、history等字段
- 需要添加roleId字段以支持角色选择

### 2.2 数据模型设计需求

需要新建的数据实体：
1. **AiRole**：AI角色实体
2. **AiRoleDocument**：角色文档关联实体（中间表）

现有数据模型扩展：
- ChatRequestDTO：添加roleId字段
- AiConversation：添加roleId字段（可选，记录会话使用的角色）

## 三、实现方案设计

### 3.1 数据库表结构设计

#### 表1：ai_role（AI角色表）

```sql
CREATE TABLE `ai_role` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色唯一标识',
    `description` TEXT COMMENT '角色描述',
    `system_prompt` TEXT COMMENT '纯文本系统提示词（可选）',
    `user_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `is_public` TINYINT DEFAULT 0 COMMENT '是否公开：0-私有 1-公开',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_by` VARCHAR(100) COMMENT '创建人',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(100) COMMENT '更新人',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI角色表';
```

#### 表2：ai_role_document（角色文档关联表）

```sql
CREATE TABLE `ai_role_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID（雪花算法）',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `document_name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `document_path` VARCHAR(500) NOT NULL COMMENT 'MinIO存储路径',
    `document_type` VARCHAR(20) NOT NULL COMMENT '文档类型（pdf/docx/txt）',
    `document_size` BIGINT COMMENT '文档大小（字节）',
    `document_content` TEXT COMMENT '解析后的文本内容',
    `upload_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `del_flag` TINYINT DEFAULT 0 COMMENT '删除标记(0-未删除 1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_document_type` (`document_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色文档关联表';
```

### 3.2 实体类设计

#### AiRole实体类

文件路径：`src/main/java/com/example/evolutionary_ai_model/entity/AiRole.java`

核心字段：
- roleName：角色名称
- roleCode：角色唯一标识
- description：角色描述
- systemPrompt：纯文本系统提示词
- userId：创建者ID
- isPublic：是否公开
- documents：关联文档列表（非持久化字段）

#### AiRoleDocument实体类

文件路径：`src/main/java/com/example/evolutionary_ai_model/entity/AiRoleDocument.java`

核心字段：
- roleId：角色ID
- documentName：文档名称
- documentPath：MinIO存储路径
- documentType：文档类型
- documentContent：解析后的文本内容

### 3.3 系统提示词构建逻辑

**构建策略**：
- 用户完全自定义系统提示词格式
- 提供模板变量支持：
  - `{role_name}`：角色名称
  - `{description}`：角色描述
  - `{documents}`：所有文档内容（按顺序拼接）
  - `{document_1}`、`{document_2}`等：单个文档内容

**构建流程**：
1. 获取角色信息（roleName、description、systemPrompt）
2. 获取角色关联的所有文档内容
3. 根据用户定义的模板，替换变量构建最终系统提示词
4. 将构建的系统提示词传递给ChatClient

**示例模板**：
```
你的名字是{role_name}，{description}。

你的核心设定如下：
{documents}

请严格按照上述设定回复用户问题。
```

## 四、详细实现步骤

### 步骤1：创建数据库表

**执行SQL脚本**：
- 创建 `ai_role` 表
- 创建 `ai_role_document` 表
- 数据库迁移脚本路径：`.trae/documents/数据库迁移脚本_V1.0.5.sql`

### 步骤2：创建实体类

**新建实体类1**：`AiRole.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/entity/AiRole.java`

主要内容：
- 使用MyBatis-Plus注解
- 包含基础字段和文档关联字段
- 支持逻辑删除

**新建实体类2**：`AiRoleDocument.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/entity/AiRoleDocument.java`

主要内容：
- 文档元数据字段
- 文档内容字段（解析后的文本）
- MinIO路径字段

### 步骤3：创建Mapper接口

**新建Mapper1**：`AiRoleMapper.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/mapper/AiRoleMapper.java`

功能：
- 继承BaseMapper
- 提供基础CRUD操作

**新建Mapper2**：`AiRoleDocumentMapper.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/mapper/AiRoleDocumentMapper.java`

功能：
- 继承BaseMapper
- 提供按roleId查询文档列表的方法

### 步骤4：创建Service层

**新建Service接口**：`AiRoleService.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/service/AiRoleService.java`

核心方法：
- createRole：创建角色
- updateRole：更新角色
- deleteRole：删除角色
- getRoleById：获取角色详情
- getUserRoles：获取用户角色列表
- uploadDocument：上传文档到角色
- deleteDocument：删除角色文档
- buildSystemPrompt：构建系统提示词（核心方法）

**新建Service实现类**：`AiRoleServiceImpl.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/service/impl/AiRoleServiceImpl.java`

关键实现：
1. **uploadDocument方法**：
   - 上传文档到MinIO（路径：`roles/{roleId}/{fileName}`)
   - 解析文档内容
   - 保存文档记录到ai_role_document表
   - 返回文档预览URL

2. **buildSystemPrompt方法**：
   - 获取角色信息
   - 获取关联文档列表
   - 解析所有文档内容（如果未解析）
   - 根据模板构建系统提示词
   - 支持模板变量替换

### 步骤5：创建Controller接口

**新建Controller**：`AiRoleController.java`

文件路径：`src/main/java/com/example/evolutionary_ai_model/controller/AiRoleController.java`

接口列表：

1. **创建角色**：POST `/ai-role/create`
   - 请求参数：roleName、description、systemPrompt
   - 返回：角色ID

2. **更新角色**：PUT `/ai-role/update`
   - 请求参数：roleId、roleName、description、systemPrompt
   - 返回：更新结果

3. **删除角色**：DELETE `/ai-role/{roleId}`
   - 同时删除关联文档
   - 返回：删除结果

4. **获取角色列表**：GET `/ai-role/list`
   - 返回：用户角色列表（包含文档数量）

5. **获取角色详情**：GET `/ai-role/{roleId}`
   - 返回：角色详情 + 文档列表

6. **上传文档**：POST `/ai-role/document/upload`
   - 请求参数：roleId、MultipartFile
   - 返回：文档ID + 预览URL

7. **删除文档**：DELETE `/ai-role/document/{documentId}`
   - 同时删除MinIO文件
   - 返回：删除结果

8. **预览文档**：GET `/ai-role/document/preview/{documentId}`
   - 返回：文档内容（解析后的文本）

9. **获取文档下载链接**：GET `/ai-role/document/download-url/{documentId}`
   - 返回：临时下载URL（有效期7天）

### 步骤6：修改对话流程

**修改ChatRequestDTO** ([ChatRequestDTO.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/entity/dto/ChatRequestDTO.java))：

新增字段：
```java
// AI角色ID，指定使用的角色，可选（不传则使用默认系统提示词）
private Long roleId;
```

**修改DynamicChatStrategy** ([DynamicChatStrategy.java](file:///d:/Project/MyProject/Evolutionary_AI_model/Evolutionary_AI_model/src/main/java/com/example/evolutionary_ai_model/service/strategy/DynamicChatStrategy.java))：

修改内容：
1. 注入 `AiRoleService`
2. 在 `streamChat()` 方法中：
   - 如果request包含roleId，调用 `aiRoleService.buildSystemPrompt(roleId)`
   - 将构建的系统提示词传递给ChatClient的 `.system()` 方法

修改位置（第161-163行）：
```java
// 获取系统提示词
String systemPrompt = null;
if (request.getRoleId() != null) {
    systemPrompt = aiRoleService.buildSystemPrompt(request.getRoleId());
    logger.info("使用角色系统提示词，角色ID: {}", request.getRoleId());
}

// 构建ChatClient调用
Flux<String> chatFlux = chatClient.prompt()
        .system(systemPrompt) // 设置系统提示词（如果存在）
        .user(prompt)
        .stream()
        .content()
        // ... 其他代码不变
```

### 步骤7：前端界面设计（简要说明）

需要新增的前端页面：

1. **角色管理页面**：`AiRoleManagement.vue`
   - 角色列表展示
   - 创建/编辑角色表单
   - 文档上传组件
   - 文档预览功能

2. **角色选择器组件**：`RoleSelector.vue`
   - 在对话界面添加角色选择下拉框
   - 显示角色列表
   - 支持搜索和筛选

前端接口调用：
- 创建角色：调用 `/ai-role/create`
- 上传文档：调用 `/ai-role/document/upload`
- 对话时：在ChatRequestDTO中添加roleId

## 五、关键技术实现细节

### 5.1 文档上传和解析流程

**流程步骤**：
1. 用户上传文档文件（PDF/Word/TXT）
2. Controller接收MultipartFile
3. Service调用MinioService上传文件（路径：`roles/{roleId}/{timestamp}_{fileName}`)
4. Service调用DocumentParserService解析文档内容
5. 保存文档记录到ai_role_document表（包含documentContent字段）
6. 返回文档ID和预览URL

**MinIO路径设计**：
- 角色文档存储路径：`ai-role-documents/{roleId}/{timestamp}_{fileName}`
- 例如：`ai-role-documents/123456/20250625143000_prompt.pdf`

### 5.2 文档预览实现

**两种预览方式**：

1. **内容预览**（推荐）：
   - 直接返回解析后的文本内容
   - 前端在文本框中展示
   - 适合查看系统提示词内容

2. **文件预览**：
   - 生成临时访问URL（有效期7天）
   - 前端通过浏览器预览PDF/Word文件
   - 适合查看原始文件格式

**实现方案**：
- 提供 `/ai-role/document/preview/{documentId}` 接口返回文本内容
- 提供 `/ai-role/document/download-url/{documentId}` 接口返回文件URL

### 5.3 系统提示词模板设计

**模板变量**：
- `{role_name}`：角色名称
- `{description}`：角色描述
- `{system_prompt}`：纯文本系统提示词
- `{documents}`：所有文档内容拼接
- `{document_1}`、`{document_2}`、`{document_3}`等：按顺序的文档内容

**默认模板**（用户可自定义）：
```
你的名字是{role_name}。

{description}

{system_prompt}

以下是你的核心设定文档：
{documents}

请严格按照上述设定回复用户问题，保持一致的角色特征。
```

**buildSystemPrompt方法实现逻辑**：
```java
public String buildSystemPrompt(Long roleId) {
    // 1. 获取角色信息
    AiRole role = getRoleById(roleId);
    
    // 2. 获取文档列表
    List<AiRoleDocument> documents = getDocumentsByRoleId(roleId);
    
    // 3. 构建文档内容字符串
    StringBuilder documentsContent = new StringBuilder();
    for (int i = 0; i < documents.size(); i++) {
        AiRoleDocument doc = documents.get(i);
        documentsContent.append("【文档").append(i + 1).append("：").append(doc.getDocumentName()).append("】\n");
        documentsContent.append(doc.getDocumentContent()).append("\n\n");
    }
    
    // 4. 使用默认模板或用户自定义模板
    String template = role.getSystemPromptTemplate();
    if (template == null || template.isEmpty()) {
        template = DEFAULT_TEMPLATE;
    }
    
    // 5. 替换模板变量
    String systemPrompt = template
        .replace("{role_name}", role.getRoleName())
        .replace("{description}", role.getDescription() != null ? role.getDescription() : "")
        .replace("{system_prompt}", role.getSystemPrompt() != null ? role.getSystemPrompt() : "")
        .replace("{documents}", documentsContent.toString());
    
    // 6. 处理单个文档变量（如果需要）
    for (int i = 0; i < documents.size(); i++) {
        systemPrompt = systemPrompt.replace("{document_" + (i + 1) + "}", documents.get(i).getDocumentContent());
    }
    
    return systemPrompt;
}
```

### 5.4 数据一致性保证

**删除角色时的处理**：
1. 删除ai_role表中的记录（逻辑删除）
2. 删除ai_role_document表中关联的所有文档记录
3. 删除MinIO中存储的所有文档文件
4. 使用事务保证数据一致性

**事务管理**：
```java
@Transactional(rollbackFor = Exception.class)
public void deleteRole(Long roleId, Long userId) {
    // 1. 验证权限
    AiRole role = getRoleById(roleId);
    if (!role.getUserId().equals(userId)) {
        throw new BusinessException("无权删除此角色");
    }
    
    // 2. 获取文档列表
    List<AiRoleDocument> documents = getDocumentsByRoleId(roleId);
    
    // 3. 删除MinIO文件
    for (AiRoleDocument doc : documents) {
        minioService.deleteFile(doc.getDocumentPath());
    }
    
    // 4. 删除数据库记录
    roleMapper.deleteById(roleId);
    roleDocumentMapper.deleteByRoleId(roleId);
    
    logger.info("角色删除成功，角色ID: {}", roleId);
}
```

## 六、影响范围分析

### 6.1 文件修改清单

| 文件路径 | 修改类型 | 主要内容 |
|---------|---------|---------|
| `数据库迁移脚本_V1.0.5.sql` | 新建 | 创建ai_role和ai_role_document表 |
| `AiRole.java` | 新建实体 | AI角色实体类 |
| `AiRoleDocument.java` | 新建实体 | 角色文档实体类 |
| `AiRoleMapper.java` | 新建Mapper | 角色数据访问接口 |
| `AiRoleDocumentMapper.java` | 新建Mapper | 文档数据访问接口 |
| `AiRoleService.java` | 新建接口 | 角色业务逻辑接口 |
| `AiRoleServiceImpl.java` | 新建实现 | 角色业务逻辑实现（包含buildSystemPrompt核心方法） |
| `AiRoleController.java` | 新建Controller | 角色管理接口 |
| `ChatRequestDTO.java` | 扩展字段 | 添加roleId字段 |
| `DynamicChatStrategy.java` | 修改逻辑 | 注入AiRoleService，根据roleId动态构建系统提示词 |

### 6.2 数据库影响

- 新增2张表：ai_role、ai_role_document
- 不影响现有数据表结构
- AiConversation表可选添加roleId字段（记录会话使用的角色）

### 6.3 功能影响

**正面影响**：
- 提供灵活的AI角色管理功能
- 支持文档上传和系统提示词自定义
- 切换角色时系统提示词动态变化
- 文档预览功能提升用户体验

**潜在风险**：
- 文档解析可能失败（需要异常处理）
- 大文件上传可能影响性能（需要限制文件大小）
- 系统提示词过长可能超过模型限制（需要提示词长度检查）

## 七、验证与测试计划

### 7.1 功能测试

**测试用例1：创建角色并上传文档**
- 操作：创建角色，上传PDF和Word文档
- 预期：角色创建成功，文档上传成功，文档内容正确解析
- 验证：查看数据库记录，检查文档内容是否正确

**测试用例2：系统提示词构建**
- 操作：调用buildSystemPrompt方法
- 预期：系统提示词包含角色信息和文档内容
- 验证：检查系统提示词格式和内容

**测试用例3：对话时使用角色**
- 操作：在ChatRequestDTO中设置roleId，发送对话请求
- 预期：AI回复符合角色设定
- 验证：检查AI回复是否符合系统提示词的设定

**测试用例4：切换角色**
- 操作：连续对话，中途切换roleId
- 预期：系统提示词动态变化，AI回复风格改变
- 验证：检查不同角色的AI回复差异

**测试用例5：文档预览**
- 操作：点击文档预览按钮
- 预期：正确显示文档解析后的文本内容
- 验证：检查预览内容与原文档内容一致性

### 7.2 边界测试

**文件大小限制测试**：
- 测试上传10MB、50MB、100MB文档
- 验证系统是否有合理的文件大小限制

**文档数量限制测试**：
- 测试一个角色关联1个、5个、10个文档
- 验证系统提示词长度是否超过模型限制

**异常情况测试**：
- 上传不支持的文件格式（如图片、视频）
- 删除正在被使用的角色
- 解析失败的文档

## 八、实施时间估算

- 数据库设计和脚本编写：30分钟
- 实体类和Mapper创建：40分钟
- Service层实现（核心逻辑）：1.5小时
- Controller接口实现：1小时
- DynamicChatStrategy修改：30分钟
- 测试验证：1小时
- 总计：约5小时

## 九、后续优化建议

1. **角色模板库**：提供预置角色模板（如客服助手、技术专家等）
2. **角色分享功能**：支持公开角色，其他用户可以使用
3. **角色评分和反馈**：用户可以对角色效果评分
4. **智能文档解析**：自动识别文档中的关键信息，优化系统提示词
5. **角色性能统计**：统计不同角色的使用频率和效果
6. **批量文档上传**：支持一次性上传多个文档
7. **文档版本管理**：支持文档版本对比和回退（如果需要）

---

**方案状态**：已完成设计，待用户确认后执行实施