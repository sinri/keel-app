# Keel-App 问题清单

> 摘自 `DESIGN_AND_ISSUES.md`，按优先级与分类整理。
> 优先级定义：`P0` 阻断发布；`P1` 功能/可靠性风险；`P2` 可维护性与 API 质量；`P3` 文档与长期负担。

---

## P1 — 功能 / 可靠性风险

### B-01 构建：Internal Nexus 硬依赖

- **分类**：构建与发布
- **现象**：`build.gradle.kts` 首个仓库为 Internal Nexus，`url` / `credentials` 依赖 `internalNexus*` 属性。
- **影响**：开源贡献者仅需本地编译，不涉及 publish；如有自定义需要可自行修改 kts 文件，实际影响较小。
- **位置**：`build.gradle.kts` — `repositories { maven { name = "InternalNexus" ... } }`
- **建议**：暂不处理。
- [x] 不予处理（影响可忽略）

### A-01 架构：CommonApplication 默认绑定阿里云 SLS

- **分类**：架构与耦合
- **现象**：`CommonApplication.buildLoggerFactory` 总是构造 `SlsLoggerFactory` 并 `deployMe`，未配置 SLS 时仅将配置元素置 `null`。
- **影响**：`CommonApplication` 本身就是面向特定场景（含阿里云 SLS）的 `Application` 子类实现，属于设计意图；通用场景应基于 `Application` 进行开发，不存在"通用层绑定阿里云"的问题。
- **位置**：`CommonApplication.java` 第 98–114 行
- **建议**：暂不处理。
- [x] 不予处理（设计意图如此）

### R-01 可靠性：Program 启动失败时调用 System.exit(1)

- **分类**：可靠性与生命周期
- **现象**：`Program.runWithCommandLine` 使用 `Future` 链异步启动，失败时 `handleError` 中调用 `System.exit(1)`。
- **影响**：所有实现 `AppLifeCycleMixin` 接口的类均被预期承担一个程序进程的完整生命周期，不存在"嵌入"的预期用法；启动失败时调用 `System.exit(1)` 符合设计意图。
- **位置**：`Program.java` 第 54–61、133–141 行；`CommandLineExecutable.java` 第 71–76 行
- **建议**：暂不处理。
- [x] 不予处理（设计意图如此）

### T-01 测试：无有效测试类

- **分类**：测试
- **现象**：`tasks.test { include("...*UnitTest.class") }`，但 `src/test/java` 无测试类。
- **影响**：CI / 本地 `test` 任务几乎不执行有效回归。
- **位置**：`build.gradle.kts` 第 97–101 行
- **修复**：新增 `CommandLineParserUnitTest`，覆盖别名校验、选项/标志/参数解析、值校验、错误情况等 18 个测试用例。
- [x] 已修复

---

## P2 — 可维护性与 API 质量

### B-02 构建：keel 依赖版本不一致

- **分类**：构建与发布
- **现象**：`keelTestVersion=5.0.2`，其余 `keel*` 多为 `5.0.1`。
- **影响**：不构成问题。
- **位置**：`gradle.properties` 第 32–37 行
- **建议**：暂不处理。
- [x] 不予处理（非问题）

### B-03 构建：缺少 README

- **分类**：构建与发布
- **现象**：仓库根目录无 `README*`。
- **影响**：集成方式、必需 Gradle 属性、签名与 JReleaser 环境缺少对外说明。
- **位置**：仓库根目录
- **修复**：新增 `README.md`，包含项目简介、特性、依赖引入、最简示例、构建与测试说明。
- [x] 已修复

### A-02 架构：CommonApplication 类注释与实现不一致

- **分类**：架构与耦合
- **现象**：类注释描述启动顺序含「业务初始化服务」，`buildServices()` 仅装配 Monitor / Queue / Sundial / Receptionist。
- **影响**：文档与实现不一致，易误导二次开发者。
- **位置**：`CommonApplication.java` 第 21–31 行与第 117–153 行
- **修复**：移除注释中不存在的「业务初始化服务」，修正为实际的四个服务并标注英文名称。
- [x] 已修复

### R-02 可靠性：WrappedService 生命周期异常

- **分类**：可靠性与生命周期
- **现象**：`WrappedService` 已标记 `@Deprecated`，`startVerticle` 成功后定时 100ms `undeployMe`，失败时可能 `getKeel().close()`。
- **影响**：已标记为废弃待移除，阻止用户使用，不构成实际问题。
- **位置**：`WrappedService.java` 全文
- **建议**：暂不处理。
- [x] 不予处理（已废弃，阻止使用）

### C-01 CLI：receptionistPort 端口校验不正确

- **分类**：CLI 与 API 质量
- **现象**：`receptionistPort` 使用正则 `^[1-9][0-9]+$`。
- **影响**：拒绝一位数端口（如 `9`）；且未按 `1–65535` 统一校验。
- **位置**：`CommonApplication.java` 第 64–69 行
- **修复**：改为数值范围校验 `Integer.parseInt` + `1–65535`，移除未使用的 `Pattern` import。
- [x] 已修复

### C-02 CLI：CommandLineOption 错误提示不准确

- **分类**：CLI 与 API 质量
- **现象**：`CommandLineOption.validatedAlias` 在模式不匹配时抛出消息 "Alias cannot be null"。
- **影响**：错误提示与真实原因不符，增加排错成本。
- **位置**：`CommandLineOption.java` 第 88–92 行
- **修复**：错误消息改为包含实际的模式信息。
- [x] 已修复

### C-03 CLI：KeelVertxApplicationHooks 配置类型丢失

- **分类**：CLI 与 API 质量
- **现象**：`KeelVertxApplicationHooks` 将非嵌套 `JsonObject` 的值一律 `toString()`。
- **影响**：`ConfigElement` 设计为纯字符串值存储，`toString()` 行为符合设计；数字/布尔值可通过 `readInteger`/`readBoolean` 等方法正确还原；数组不在 `ConfigElement` 的支持范围内。
- **位置**：`KeelVertxApplicationHooks.java` 第 26–37、44–56 行
- **处理**：完善了 `transformJsonObjectToConfigElements` 的 javadoc，明确了转换行为。
- [x] 已处理（补充文档）

---

## P3 — 长期负担

### M-01 模块化：大量 requires transitive

- **分类**：其他
- **现象**：`module-info.java` 中大量 `requires transitive`（Vert.x、Jackson、CommonMark 等）。
- **影响**：`keel-app` 定位为一站式应用框架入口，`requires transitive` 是有意为之，使下游消费者无需逐一声明依赖。
- **位置**：`module-info.java`
- **建议**：暂不处理。
- [x] 不予处理（设计意图如此）

---

## 统计

| 优先级 | 数量 | Issue IDs |
|--------|------|-----------|
| P1     | 0    | |
| P2     | 0    | |
| P3     | 0    | |
| 已关闭   | 12   | B-01, A-01, R-01, B-02, R-02, M-01（不予处理）; T-01, C-01, C-02, A-02, B-03（已修复）; C-03（补充文档） |
| **合计** | **12** | |
