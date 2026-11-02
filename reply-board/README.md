# 답변형 게시판 REST 댓글

게시글 목록, 글쓰기, 답글, 상세 화면이 있고, 상세 화면의 댓글은 REST API로 등록·수정·삭제합니다. 댓글을 달아도 페이지는 새로고침되지 않습니다.

## 실행

Java 17 이상이 필요합니다. IntelliJ에서 `reply-board` 폴더를 열고 `BoardApplication`을 실행한 뒤 http://localhost:8080 을 엽니다.

샘플 글 제목은 `게시판 글 제목`이고, 댓글 두 개가 들어 있습니다.

## API

- `GET /api/posts/{postId}/comments?order=asc|desc`
- `POST /api/posts/{postId}/comments` JSON `{ "writer", "content", "parentId" }`
- `PUT /api/comments/{id}` JSON `{ "content" }`
- `DELETE /api/comments/{id}`

`parentId`가 있으면 그 댓글의 답글입니다. `order=asc`는 등록순, `desc`는 최신순입니다.

## H2

앱이 실행 중일 때 http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:reply_board`
- User Name: `sa`
- Password: 비움

```sql
SELECT * FROM posts;
SELECT * FROM comments;
```
