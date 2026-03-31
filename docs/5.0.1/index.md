# Keel-App 5.0.1 用户使用文档

本文档面向在业务项目中集成 **Keel-App** 的开发者，说明如何引入依赖、启动应用、扩展命令行与内置服务，以及与 Vert.x 启动器的集成方式。

## 1. 文档与版本说明

- 文档目录 `docs/5.0.1/` 对应 **Keel-App 5.0.x** 用户指南；具体发行版请以 Maven 坐标中的 `version` 为准（例如 `5.0.1` 或
  `5.0.1-SNAPSHOT`）。
- Keel-App 是 [Keel](https://github.com/sinri/keel) 的应用封装层，基于 **Vert.x 5**，要求 **Java 17+**。

## 2. 产品概述

Keel-App 提供：

| 能力                 | 说明                                                                |
|--------------------|-------------------------------------------------------------------|
| 命令行解析              | 短选项（`-o`）、长选项（`--option`）、标志（无值开关）与位置参数                           |
| 应用生命周期             | `Program` → `Application` → `CommonApplication` 分层抽象              |
| 服务编排               | `Application` 按列表顺序部署 Verticle 形态的服务；`CommonApplication` 默认四类服务骨架 |
| Vert.x Launcher 桥接 | `KeelVertxApplicationHooks` 对接官方 Application Launcher             |
| JPMS               | 模块名 `io.github.sinri.keel.app`，汇总暴露常用 Keel 与 Vert.x 依赖            |

## 3. 环境要求

- **JDK**：17 或更高
- **构建**：若参与本仓库开发，需 Gradle 9+（最终用户仅 JVM 与依赖仓库即可）

## 4. 引入依赖

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.sinri:keel-app:5.0.1")
}
```

### Maven

```xml

<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>keel-app</artifactId>
    <version>5.0.1</version>
</dependency>
```

> 若使用快照版本，请将 `version` 改为仓库中实际的 SNAPSHOT 坐标。

### 传递依赖概览

Keel-App 会传递引入 Vert.x（含 `vertx-launcher-application`）、Keel 核心栈（如 `keel-core`、`keel-web`、`keel-mysql`、
`keel-aliyun`、日志扩展等）。具体以发布 POM 为准；在模块化项目中请通过
`requires transitive io.github.sinri.keel.app` 统一引入（见下文「Java 平台模块」）。

## 5. 程序入口与启动方式

可执行程序应通过 **`launch(String[] args)`** 进入框架（定义在 `AppLifeCycleMixin` 中，由 `CommandLineExecutable` 实现）。*
*不要**直接调用受保护的 `runWithCommandLine()`。

典型 `main` 写法：

```java
static void main(String[] args) {
    new MyApplication().launch(a
```

`launch` 会完成：构建（若有）命令行解析器 → 解析参数 → 调用子类的启动流水线。

## 6. 分层模型

### 6.1 `Program<C extends ProgramContext>`

适用于任意「基于 Vert.x / Keel 运行的程序」，不一定是「多 Verticle 应用」。

你需要实现：

- `buildProgramContext()`：构造程序上下文
- `launchAsProgram()`：在 Vert.x 与 Keel 就绪后执行的主体逻辑

可选覆盖：

- `loadLocalConfiguration()`：默认从类路径加载 `config.properties` 到 `ConfigElement.root()`
- `loadRemoteConfiguration()`：默认空实现，可异步拉取远程配置
- `buildVertxOptions()` / `buildClusterManager()`：默认单机、`ClusterManager` 为
  `null`；若返回非 null 管理器则走集群 Vert.x 构建
- `buildLoggerFactory()`：默认标准输出工厂；`CommonApplication` 中另有阿里云 SLS 集成
- `buildMetricRecorder()`：默认无指标记录器
- `whenLaunched(long startTime)`：启动成功后的钩子
- `affix()`：异步链在后台跑完后，主线程收尾（默认空；若进程需常驻可在此阻塞或挂接 CountDownLatch 等）
- `handleError(Throwable)`：致命错误处理，默认打日志并 `System.exit(1)`

启动顺序（概念上）：初始化日志委派 → 将已解析 CLI 写入 `ProgramContext` → 注册 JSON 序列化 → 本地配置 → 创建/共享 `Vertx` 与
`Keel` → 远程配置 → `LoggerFactory` → 可选 `MetricRecorder` → `launchAsProgram()`。

### 6.2 `Application<C extends ProgramContext>` extends `Program<C>`

表示**按顺序部署多个 `Service`（Verticle）** 的应用。

你必须实现：

- `buildCliName()` / `buildCliDescription()`：用于错误输出中的程序名与描述
- `prepare()`：`Future`，部署服务前的准备阶段
- `buildServices()`：返回有序的服务列表，将**依次**部署

命令行：`buildCommandLineParser()` 已用 `CommandLineArgumentsParser.create()` 包装；可覆盖 `buildCliOptions()` 返回
`List<CommandLineOption>` 注册选项。

部署失败时：若某服务的 `isIndispensableService()` 为 `true`（默认），失败会导致启动失败；若为 `false`，该服务失败后继续部署其余服务。

### 6.3 `CommonApplication<C>` extends `Application<C>`

在 `Application` 之上提供**约定俗成的四类服务**构建钩子，并注册一组内置 CLI 开关（见第 10 节）。

## 7. 程序上下文 `ProgramContext`

表示运行期共享的非顶层对象（配置、CLI、日志工厂、指标等）。

- 接口方法包括：`getParsedCliArguments()` / `setParsedCliArguments()`、`getRootConfigElement()`、`getLoggerFactory()`、可选的
  `MetricRecorder` 存取。
- 可直接继承 **`AbstractProgramContext`**，获得 CLI 与 `MetricRecorder` 的惰性持有实现。

`Program` 在启动早期会把解析后的参数注入上下文。

## 8. 服务 `Service<P extends ProgramContext>`

服务是挂靠在 Keel 上的 Verticle，需实现：

- `deployMe(Keel keel, P programContext)`：部署本服务，并应保存 `programContext` 供后续使用
- `getProgramContext()`：部署后可访问上下文

**弃用说明（5.0.1）**：`Service.wrap(...)` 已标记为 `@Deprecated(since = "5.0.1", forRemoval = true)`，新代码请使用显式
`Service` 实现类，避免依赖该方法。

内置抽象基类：

| 类型                            | 作用                                                      |
|-------------------------------|---------------------------------------------------------|
| `AbstractMonitorService`      | 运行时监控快照；可用 `throughLogger` / `throughMetricRecorder` 工厂 |
| `AbstractQueueService`        | 基于 Keel 队列分发                                            |
| `AbstractSundialService`      | 定时任务；可 `wrap` 自定义计划供给                                   |
| `AbstractReceptionistService` | HTTP 服务（`KeelHttpServer`）；端口见下文 CLI                     |

## 9. `CommonApplication` 详解

### 9.1 默认服务顺序

`buildServices()` 实现为（在未被对应 flag 关闭且构造方法非 null 时）依次加入：

1. Monitor（监控）
2. Queue（队列）
3. Sundial（定时）
4. Receptionist（HTTP）

子类需实现四个 `construct*Service()` 方法，不需要某项时返回 `null` 即可从列表中省略。

### 9.2 内置命令行选项

| 常量名                   | 含义                                                |
|-----------------------|---------------------------------------------------|
| `disableMonitor`      | 标志，关闭监控服务                                         |
| `disableQueue`        | 标志，关闭队列服务                                         |
| `disableSundial`      | 标志，关闭定时服务                                         |
| `disableReceptionist` | 标志，关闭 HTTP 服务                                     |
| `receptionistPort`    | 取值，1–65535；`AbstractReceptionistService` 会优先采用该端口 |

使用方式示例：

```text
java -jar app.jar --disableSundial --receptionistPort 8080
```

### 9.3 日志工厂

`CommonApplication` 覆盖 `buildLoggerFactory()`：尝试从根配置读取阿里云 SLS（`AliyunSlsConfigElement`）；未配置则回落提示并以可部署的
`SlsLoggerFactory` 路径工作（具体行为以 Keel 集成与配置为准）。部署使用 **Worker** 线程模型。

## 10. 命令行解析约定

解析器行为简述（详见 `CommandLineArgumentsParser` JavaDoc）：

- **混合格式**：选项与标志在前；若有**位置参数**，必须先出现分隔符 `--`，再跟参数。例：`--opt value -f -- arg1 arg2`
- **纯参数**：仅有位置参数时可不用 `--`

定义选项使用 **`CommandLineOption`**：

- `alias(String)`：可多次调用，名称需符合字母数字与 `_ . -` 等规则
- `flag()`：无值布尔开关
- `description(String)`：说明
- `setValueValidator(Function<String, Boolean>)`：校验选项值

读取结果使用 **`CommandLineArguments`**：

- `readOption(String)`：未提供为 `null`；标志为 `""`；否则为字符串值
- `readFlag(String)`：是否出现该选项/标志
- `readParameter(int)`：`--` 后的位置参数索引（从 0 起）

> **API 稳定性**：`CommandLineOption` 在源码中标注为技术预览性质，后续小版本可能对 API 有调整；升级时请留意发行说明。

## 11. 配置文件 `config.properties`

默认在 `Program.loadLocalConfiguration()` 中从类路径加载 **`config.properties`** 到
`ConfigElement.root()`。构建过程可能排除资源包内的样例（以 `build.gradle.kts` 中
`processResources` 为准），发布时请自行在应用中提供该文件或覆盖加载逻辑。

## 12. 与 Vert.x Application Launcher 集成

若使用 [Vert.x Application Launcher](https://vertx.io/docs/vertx-launcher-application/java/)，可实现 *
*`KeelVertxApplicationHooks`**（扩展 `VertxApplicationHooks`）：

- **`afterConfigParsed(JsonObject config)`**（`default`）：把 Launcher 解析的 JSON 配置树合并进
  `ConfigElement.root()`，便于与 Keel 配置体系统一。
- **`afterVertxStarted(HookContext context)`**：在 Vert.x 已创建后接入，用于调用 `Keel.share(vertx)` 等逻辑（由你在实现类中编写）。

## 13. Java 平台模块（JPMS）

模块 **`io.github.sinri.keel.app`** 导出包包括：

- `io.github.sinri.keel.app.cli`
- `io.github.sinri.keel.app.common`、`...monitor`
- `io.github.sinri.keel.app.launcher`
- `io.github.sinri.keel.app.runner`、`...service`

业务模块常见写法：

```java
module com.example.myapp {
    requires io.github.sinri.keel.app;
    // ...
}
```

## 14. 行为与运维提示

- **进程退出**：默认在不可恢复错误路径上会 `System.exit(1)`。嵌入测试或受控环境时，可能需要自定义
  `handleError` 或结合 Keel/Vert.x 关闭策略评估影响。
- **异步启动**：`Program` 中真正启动 Vert.x 与业务链在 Vert.x `Future` 上异步执行；`affix()` 在主线程便于做「保持进程存活」等同步收尾。
- **单机与集群**：覆盖 `buildClusterManager()` 非 `null` 时走集群 Vert.x；否则为单机 `Vertx.builder()...build()`。

## 15. 相关资源

- 仓库：[https://github.com/sinri/keel-app](https://github.com/sinri/keel-app)
- Keel 框架：[https://github.com/sinri/keel](https://github.com/sinri/keel)
- 许可证：GPL-3.0（见仓库 `LICENSE` 或 POM 中声明）

---

*文档与 Keel-App 源码一致即可；若公共 README 中的示例仍写有 `runWithCommandLine(args)`
，请以本文档与 `AppLifeCycleMixin#launch` 为准。*
