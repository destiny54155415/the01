# IntelliJ 오류 수정 내용

1. `build.gradle` Java 버전을 25 → 21로 변경했습니다. JDK 25가 없으면 빌드가 실패합니다.
2. `application.properties` 끝의 잘못된 `` ``` `` 문자를 제거했습니다.
3. MySQL이 없어도 실행되도록 기본 DB를 H2로 바꿨습니다. MySQL은 주석으로 남겨 두었습니다.
4. 없는 MyBatis 설정을 제거했습니다. MyBatis 의존성이 없었습니다.
5. `/signup` 화면 파일 `signup.html`을 추가했습니다.
6. 헤더 검색이 가는 `/products` 매핑을 컨트롤러에 추가했습니다.

## IntelliJ에서 다시 열기

1. 이 폴더(`build.gradle`이 있는 곳)를 IntelliJ에서 Open 합니다.
2. Project SDK를 **21**로 맞춥니다.
3. Gradle 새로고침 후 `ShoppingProjectApplication`을 실행합니다.
4. http://localhost:8080 을 엽니다.
