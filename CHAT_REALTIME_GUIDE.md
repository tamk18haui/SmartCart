# Chat realtime Spring Boot + WebSocket/STOMP + JWT

Đã thêm module chat vào project SmartCart.

## 1. File đã thêm/sửa

### Sửa `pom.xml`
Thêm dependency:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### Sửa security
File:

```text
src/main/java/com/gr6/SmartCart/common/security/SecurityFilterChainConfig.java
```

Đã cho phép handshake WebSocket:

```text
/ws/**
/ws-chat/**
```

Lưu ý: JWT vẫn được kiểm tra trong `StompAuthChannelInterceptor` khi client CONNECT vào WebSocket.

### Thêm entity
```text
src/main/java/com/gr6/SmartCart/common/domain/Conversation.java
```

### Sửa entity có sẵn
```text
src/main/java/com/gr6/SmartCart/common/domain/Message.java
```

Đã thêm:

```java
private Conversation conversation;
private LocalDateTime readAt;
```

### Thêm module chat
```text
src/main/java/com/gr6/SmartCart/modules/chat/
├── config
│   ├── StompAuthChannelInterceptor.java
│   ├── StompPrincipal.java
│   └── WebSocketConfig.java
├── controller
│   ├── ChatRestController.java
│   └── ChatWebSocketController.java
├── dto
│   ├── ChatMessageRequest.java
│   ├── ChatMessageResponse.java
│   └── ConversationResponse.java
├── repository
│   ├── ChatMessageRepository.java
│   └── ConversationRepository.java
└── service
    └── ChatService.java
```

## 2. API REST

Tất cả API dưới đây cần header:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

### Lấy danh sách cuộc trò chuyện

```http
GET http://localhost:8080/api/v1/chat/conversations
```

### Lấy lịch sử chat với một user

```http
GET http://localhost:8080/api/v1/chat/messages/{partnerId}?page=0&size=20
```

Ví dụ:

```http
GET http://localhost:8080/api/v1/chat/messages/2?page=0&size=20
```

### Đánh dấu đã đọc

```http
PATCH http://localhost:8080/api/v1/chat/messages/{partnerId}/read
```

Ví dụ:

```http
PATCH http://localhost:8080/api/v1/chat/messages/2/read
```

## 3. WebSocket/STOMP

### Endpoint

```text
ws://localhost:8080/ws-chat
```

Với Android Emulator:

```text
ws://10.0.2.2:8080/ws-chat
```

### Header khi CONNECT

```text
Authorization: Bearer YOUR_JWT_TOKEN
```

### Subscribe nhận tin nhắn

```text
/user/queue/messages
```

### Send tin nhắn

Destination:

```text
/app/chat.send
```

Body JSON:

```json
{
  "receiverId": 2,
  "content": "Xin chào, đây là tin nhắn realtime"
}
```

## 4. Test bằng Postman

### Bước 1: Login lấy token
Dùng API login hiện có của project.

Ví dụ nếu project dùng:

```http
POST http://localhost:8080/api/v1/auth/login
```

Sau đó copy token trả về.

### Bước 2: Test REST
Mở tab HTTP request trong Postman, thêm header:

```text
Authorization: Bearer TOKEN_CUA_BAN
```

Test:

```http
GET /api/v1/chat/conversations
GET /api/v1/chat/messages/2?page=0&size=20
PATCH /api/v1/chat/messages/2/read
```

### Bước 3: Test WebSocket bằng Postman
Postman WebSocket thường hỗ trợ raw WebSocket, nhưng STOMP frame cần gửi đúng định dạng.

Connect tới:

```text
ws://localhost:8080/ws-chat
```

Gửi frame CONNECT:

```text
CONNECT
accept-version:1.2
heart-beat:10000,10000
Authorization:Bearer YOUR_JWT_TOKEN

\u0000
```

Subscribe:

```text
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

\u0000
```

Send message:

```text
SEND
destination:/app/chat.send
content-type:application/json

{"receiverId":2,"content":"Hello realtime"}\u0000
```

Nếu Postman khó dùng STOMP, nên dùng extension WebSocket King hoặc viết màn Android test luôn.

## 5. Android Java gợi ý

Thêm dependency STOMP client, ví dụ dùng thư viện `ua.naiksoftware:stomp` nếu project Android đang dùng Java.

```gradle
implementation 'com.github.NaikSoftware:StompProtocolAndroid:1.6.6'
implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
```

Code mẫu:

```java
StompClient stompClient = Stomp.over(
        Stomp.ConnectionProvider.OKHTTP,
        "ws://10.0.2.2:8080/ws-chat/websocket"
);

List<StompHeader> headers = new ArrayList<>();
headers.add(new StompHeader("Authorization", "Bearer " + token));

stompClient.connect(headers);

stompClient.topic("/user/queue/messages").subscribe(topicMessage -> {
    String json = topicMessage.getPayload();
    // Parse json thành ChatMessageResponse rồi cập nhật RecyclerView
});

JSONObject body = new JSONObject();
body.put("receiverId", 2);
body.put("content", "Xin chào");

stompClient.send("/app/chat.send", body.toString()).subscribe();
```

Nếu dùng endpoint không SockJS thì thử:

```text
ws://10.0.2.2:8080/ws-chat
```

Nếu dùng SockJS thì thử:

```text
ws://10.0.2.2:8080/ws-chat/websocket
```

## 6. Luồng hoạt động

1. User A login lấy JWT.
2. User B login lấy JWT.
3. Cả 2 client connect WebSocket `/ws-chat` với header Authorization.
4. Cả 2 subscribe `/user/queue/messages`.
5. User A gửi body `{ "receiverId": B_ID, "content": "..." }` tới `/app/chat.send`.
6. Backend kiểm tra JWT, lưu tin nhắn vào MySQL, gửi realtime cho cả A và B.
7. App gọi REST API để load lịch sử hoặc danh sách cuộc trò chuyện.

## 7. Bảng database tự sinh

Vì project đang dùng:

```yaml
spring.jpa.hibernate.ddl-auto: update
```

Khi chạy lại backend, Hibernate sẽ tự tạo/cập nhật bảng:

```text
conversations
messages
```

