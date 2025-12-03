# 在线五子棋游戏

这是一个基于Java实现的命令行在线五子棋游戏，支持多人在线对战。

## 功能特点

- 支持多房间同时进行游戏（默认提供10个房间）
- 实时对战和状态同步
- 清晰的命令行界面，提供操作指引
- 完整的五子棋规则实现（支持十六进制坐标）
- 房间管理和玩家匹配系统（支持游戏结束后自动重置）

## 技术栈

- Java 17
- Socket网络编程
- 多线程处理
- JUnit 5测试框架

## 系统要求

- JDK 17或更高版本
- Maven 3.4或更高版本

## 安装说明

1. 克隆项目到本地：
```bash
git clone https://github.com/yourusername/wuzi-online.git
cd wuzi-online
```

2. 使用Maven编译项目：
```bash
mvn clean package
```

## 运行说明

1. 启动服务器：
```bash
java -cp target/wuzi-online-1.0-SNAPSHOT.jar com.wuzi.server.GameServer
```

2. 启动客户端（可以启动多个）：
```bash
java -cp target/wuzi-online-1.0-SNAPSHOT.jar com.wuzi.client.GameClient
```

## 游戏操作说明

1. 启动客户端后，输入你的名字进入游戏，系统会显示欢迎信息。
2. 使用以下命令进行操作：
   - `ls rooms` - 查看所有房间列表
   - `enter room <id>` - 进入指定房间（例如：`enter room 1`）
   - `start` - 准备开始游戏（双方都准备好后自动开始）
   - `put <x> <y>` - 在指定位置落子（支持十六进制坐标 0-E）
   - `help` - 查看帮助信息
   - `quit` - 退出游戏

3. 游戏规则：
   - 黑子先手
   - 在空位置落子
   - 横向、纵向或斜向连成5子即获胜
   - 坐标范围：0-14（使用十六进制表示，A=10, B=11, ..., E=14）

## 测试

运行单元测试：
```bash
mvn test
```

## 项目结构

```
src/
├── main/
│   └── java/
│       └── com/
│           └── wuzi/
│               ├── client/
│               │   └── GameClient.java
│               └── server/
│                   ├── GameServer.java
│                   ├── RoomManager.java
│                   ├── GameRoom.java
│                   ├── GameBoard.java
│                   ├── Player.java
│                   └── ClientHandler.java
└── test/
    └── java/
        └── com/
            └── wuzi/
                └── server/
                    ├── GameBoardTest.java
                    ├── GameRoomTest.java
                    └── RoomManagerTest.java
```

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件 