# 3DMGF

**应用访问地址**：http://118.25.143.47/

**项目文档说明**：

**PRD.pdf**：产品文档，包含产品背景、用户需求、功能需求、技术方案与架构设计、3D模型生成API选择、效果评估指标与系统设计、API调用优化

**项目管理文档.pdf**：成员任务分配、开发计划安排、代码规范

**3DMGF.rp**：产品原型文件

**architecture.svg**：架构设计

**使用说明**：

****前端****：
1. 进入前端目录
    ```
    cd frontend
    ```
2. 安装依赖
    ```
    pnpm install
    ```
3. 将 `/src/api/config.ts` 中的 `baseUrl` 值改为实际的后端 API 地址
4. 启动项目：
    ```
    pnpm dev
    ```
5. 访问 `http://localhost:5173`

****后端****：
环境准备
1. 安装 MySQL，创建并配置数据库
2. 准备 COS 对象存储（bucket、region、AK/SK）
3. 获取 Tripo3D OpenAPI 的 base-url 与 api-key
4. 获取 DeepSeek（或其他 OpenAI 兼容）API Key，用于提示词增强

数据库初始化
使用项目提供的 SQL 脚本初始化数据库与表结构：
- 建库与完整表结构：`sql/create_table1.sql`
建议直接执行 `create_table1.sql`（包含完整字段与索引）。

配置说明
编辑 `src/main/resources/application.yml`，至少需要配置以下部分：
1. 服务基础配置
2. 数据库连接
3. LangChain4j（用于 Prompt 增强）
4. Tripo3D（WebClient 与 API Key）
5. COS 对象存储

注意：
- `tripo3d.api.base-url` 与 `tripo3d.api.key` 在代码中通过 `@Value("${tripo3d.api.base-url}")`、`@Value("${tripo3d.api.key}")` 注入，必须正确配置，否则无法调用 Tripo3D。
- LangChain4j 的 `base-url`、`api-key`、`model-name` 请使用你自己的供应商或模型。

构建与运行（Windows）
1. 编译打包
```bash
.\mvnw.cmd clean package
```
2. 直接运行（开发模式）
```bash
.\mvnw.cmd spring-boot:run
```
应用启动后：
- 访问健康检查：http://localhost:8123/api/health/
- OpenAPI JSON：http://localhost:8123/api/v3/api-docs/default



