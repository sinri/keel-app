# Keel-App

Keel-App 是 [Keel](https://github.com/sinri/keel) 框架的应用封装层，提供一站式的应用程序启动与生命周期管理能力。基于 Vert.x 5 构建，面向 Java 17+。

## 特性

- **命令行解析** — 支持短选项（`-o`）、长选项（`--option`）、标志和位置参数
- **应用生命周期管理** — `Program` → `Application` → `CommonApplication` 三层抽象
- **内置服务编排** — `CommonApplication` 默认按顺序部署 Monitor、Queue、Sundial、Receptionist 四类服务
- **Vert.x Launcher 集成** — 通过 `KeelVertxApplicationHooks` 桥接 Vert.x Application Launcher
- **Java 模块化** — 完整的 `module-info.java`，作为一站式入口传递暴露所有必要依赖

## 快速开始

### 依赖引入

**Gradle (Kotlin DSL)**

```kotlin
dependencies {
    implementation("io.github.sinri:keel-app:5.0.0")
}
```

**Maven**

```xml
<dependency>
    <groupId>io.github.sinri</groupId>
    <artifactId>keel-app</artifactId>
    <version>5.0.0</version>
</dependency>
```

### 最简示例

```java
public class MyApplication extends Application<MyContext> {

    @Override
    protected MyContext buildProgramContext() {
        return new MyContext();
    }

    @Override
    protected Future<Void> prepare() {
        // 初始化逻辑
        return Future.succeededFuture();
    }

    @Override
    protected List<Service<MyContext>> buildServices() {
        // 返回需要部署的服务列表
        return List.of();
    }

    public static void main(String[] args) {
        new MyApplication().runWithCommandLine(args);
    }
}
```

## 构建

```bash
./gradlew build
```

### 要求

- Java 17+
- Gradle 9+

## 测试

```bash
./gradlew test
```

测试类命名约定：`*UnitTest.java`。

## 许可证

[GPL-v3.0](https://www.gnu.org/licenses/gpl-3.0.txt)
