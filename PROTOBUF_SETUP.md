# 在PureYunhu项目中使用Protocol Buffers (Protobuf)

本项目已配置支持Protocol Buffers，但由于Android Gradle Plugin 9.0与protobuf gradle插件存在兼容性问题，我们需要采用手动编译方式生成Kotlin代码。

## 当前配置

- 项目依赖中已包含 `protobuf-kotlin-lite`
- 定义了 `user.proto` 文件，位于 `app/src/main/proto/user.proto`
- 提供了 `ProtoUserHelper.kt` 用于转换protobuf消息与应用模型

## 手动编译Protobuf（Windows）

由于AGP 9.0兼容性问题，需手动编译protobuf文件：

1. 下载并安装protobuf编译器（protoc）：
   - 访问 https://github.com/protocolbuffers/protobuf/releases
   - 下载适用于Windows的预编译二进制文件
   - 将 `protoc.exe` 添加到系统PATH

2. 为Kotlin生成代码，需要protobuf编译器插件：
   - 下载 `protoc-gen-kotlin` 插件
   - 将插件放置在与protoc相同的目录中

3. 在项目根目录运行以下命令：
```bash
protoc --plugin=protoc-gen-kotlin=path/to/protoc-gen-kotlin --kotlin_out=app/src/main/java --proto_path=app/src/main/proto user.proto
```

4. 生成的Kotlin文件将位于 `app/src/main/java/io/github/shanfishapp/pureyunhu/protobuf/`

## 使用Protobuf消息

在代码中使用protobuf消息：

```kotlin
import io.github.shanfishapp.pureyunhu.protobuf.User

// 创建User消息
val user = User.newBuilder()
    .setId(123)
    .setName("John Doe")
    .setEmail("john@example.com")
    .setAge(30)
    .build()

// 序列化为字节数组
val bytes = user.toByteArray()

// 从字节数组反序列化
val deserializedUser = User.parseFrom(bytes)
```

## 与应用模型的转换

使用 `ProtoUserHelper` 类在protobuf消息和应用数据模型之间进行转换：

```kotlin
import io.github.shanfishapp.pureyunhu.models.ProtoUserHelper

// protobuf消息转应用模型
val appUser = ProtoUserHelper.protobufToUser(protoUser)

// 应用模型转protobuf消息
val protoUser = ProtoUserHelper.userToProtobuf(appUser)
```

## 注意事项

1. 由于AGP 9.0兼容性问题，protobuf代码生成需手动完成
2. 请确保protobuf消息版本与后端API兼容
3. 在构建发布版本前，请确保所有protobuf消息已正确生成