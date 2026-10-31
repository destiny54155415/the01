# IntelliJ 오류 수정 내용

1. `build.gradle`은 **JDK 25**로 실행합니다.
2. `settings.gradle`에 Foojay toolchain resolver를 넣어, JDK 25가 없으면 Gradle이 자동으로 받습니다.
3. `application.properties` 끝의 잘못된 `` ``` `` 문자를 제거했습니다.
4. MySQL이 없어도 실행되도록 기본 DB를 H2로 바꿨습니다. MySQL 설정은 주석으로 남겨 두었습니다.
5. 없는 MyBatis 설정을 제거했습니다.
6. `/signup` 화면 파일 `signup.html`을 추가했습니다.
7. `/products` 매핑을 컨트롤러에 추가했습니다.

## IntelliJ에서 JDK 25로 실행

1. 이 폴더(`build.gradle`이 있는 곳)를 IntelliJ에서 Open 합니다.
2. **File → Project Structure → Project SDK** 를 **25** (Temurin 25 등)로 맞춥니다.
3. **Settings → Build Tools → Gradle → Gradle JVM** 도 **25**로 맞춥니다.
4. Gradle 새로고침 후 `ShoppingProjectApplication`을 실행합니다.
5. http://localhost:8080 을 엽니다.

JDK 25가 아직 없다면 IntelliJ에서 SDK를 받거나, Gradle 새로고침 시 toolchain이 자동으로 설치합니다.
