当前项目存在的问题：

1) 房间创建来源未明  
README 引导“ls rooms”和“enter room <id>”，但代码中没有任何地方在服务器启动时创建房间，也没有“创建房间”的命令。<mcsymbol name="RoomManager" filename="RoomManager.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\RoomManager.java" startline="1" type="class"></mcsymbol> 的 createRoom 仅在测试中被直接调用，生产路径没有使用。按当前实现，玩家无法进入任何房间，因为房间列表通常为空。

2) 坐标输入进制不一致  
README 标注“坐标范围：0-14（使用十六进制表示，A=10, B=11, ..., E=14）”，但服务端命令解析使用 Integer.parseInt(parts[1]) 与 Integer.parseInt(parts[2])，即只接受十进制。若玩家按 README 输入“put A B”，将导致数字解析异常并被反馈为“命令格式错误”。显示为十六进制，而输入为十进制，文档与实现存在不一致。

3) 行棋顺序与轮次未校验  
当前并未在 <mcsymbol name="GameRoom" filename="GameRoom.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\GameRoom.java" startline="1" type="class"></mcsymbol> 或 <mcsymbol name="ClientHandler" filename="ClientHandler.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\ClientHandler.java" startline="1" type="class"></mcsymbol> 中维持“当前该谁走”的状态，任何一方在开局后均可随时落子。实际五子棋需要严格的先后手与轮流规则，否则存在同时落子或连续多步的逻辑问题。

4) 开始游戏时颜色提示信息有误  
<mcfile name="ClientHandler.java#startGame" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\ClientHandler.java"></mcfile> 中生成的是“以当前发起 start 的玩家颜色”构造的提示字符串，但却把同一字符串发送给双方，导致另一名玩家收到的自述颜色信息不正确（会把对方的颜色误报为“你是黑/白方”）。

5) 棋盘并发写入缺乏互斥  
<mcfile name="GameBoard.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\GameBoard.java"></mcfile> 的 makeMove 与 <mcfile name="GameRoom.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\GameRoom.java"></mcfile> 的 makeMove 均未同步，两个 ClientHandler 线程可能并发写同一局面，造成竞态条件。尽管 ConcurrentHashMap 保护了房间表，但棋盘自身不是线程安全的。

6) 断线/退出与房间生命周期  
玩家断线后 <mcsymbol name="ClientHandler" filename="ClientHandler.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\ClientHandler.java" startline="1" type="class"></mcsymbol> 的 finally 会把玩家从房间移除，但 <mcsymbol name="GameRoom" filename="GameRoom.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\GameRoom.java" startline="1" type="class"></mcsymbol> 不会重置棋局或“游戏开始/结束”状态，也没有通知留存玩家，<mcsymbol name="RoomManager" filename="RoomManager.java" path="f:\Programming\Workspace\WuziOnline\src\main\java\com\wuzi\server\RoomManager.java" startline="1" type="class"></mcsymbol> 也不会自动清理空房间，房间会一直残留。

7) 错误提示与用户体验  
命令格式校验较薄弱，除了简单的“命令格式错误”之外没有更细的用法提示；客户端虽然本地识别“quit”，但服务器侧没有“help/quit”等命令分支，且“enter room”与“ls rooms”的协作体验取决于房间是否存在。



当前项目待澄清的关键问题  


1) 房间创建流程你期望如何触发？答：提供“创建房间”的命令供玩家动态创建。其他玩家可以选择加入某个房间，或者点击“匹配”，由系统自动与等待中的玩家匹配。  
2) 玩家坐标输入应采用十六进制（与显示一致，允许 A–E）还是十进制？答：采用十六进制，允许大小写，支持诸如“0xA”形式。  
3) 是否需要严格的轮流落子规则与先后手控制（黑先白后），并在服务器端强制校验当前轮次？  需要。
4) 在一方断线或退出后，房间与棋局的期望处理是什么？答：即判负、暂停等待重连，并在60秒内没有重连时，判定掉线玩家失败，并将掉线玩家踢出房间，留剩下的玩家继续在房间内。
5) 游戏开始时的颜色与回合提示应分别对每位玩家个性化显示，当前信息格式是否有既定文案风格或多语言需求？答：当前信息格式为“你是黑方”或“你是白方”，回合提示为“当前该黑方走”或“当前该白方走”。
6) 并发安全与一致性方面，是否有性能/复杂度偏好？例如是否接受对房间或棋盘层面加锁，或倾向于通过消息队列/串行化处理确保顺序？答：接受加锁。  
7) 是否需要扩展命令协议（如 help、create room、leave、rooms info、spectate 等），以及是否需要对命令解析提供更友好的错误提示与引导？答：需要。  
8) 测试用例中关于首次落子的断言预期（testInvalidMove 的第一个“(7,7)”应返回 false）是否为笔误，还是你希望棋盘对某些位置（例如天元）有特殊规则从而第一手无效？答：是笔误，应该返回 true。

        