# HitHealth에 기여하기

[English](CONTRIBUTING.md) | [日本語](CONTRIBUTING.ja.md) | [简体中文](CONTRIBUTING.zh-CN.md) | **한국어**

HitHealth에 관심을 가져 주셔서 감사합니다. 버그 제보, 번역, 풀 리퀘스트를 환영합니다.

## 버그 제보와 기능 요청

- Issues 페이지의 템플릿을 사용해 주세요.
- Issue와 풀 리퀘스트는 영어, 일본어, 중국어, 한국어 중 어느 언어로 작성해도 됩니다.
- 버그를 제보할 때는 Paper 버전, Java 버전, Geyser를 통해 접속한 플레이어인지 여부, 재현 방법을 적어 주세요.
- 보안 문제는 공개 Issue에 올리지 마세요. [SECURITY.ko.md](SECURITY.ko.md)를 참고해 주세요.

## 풀 리퀘스트

1. 큰 변경이나 새 기능은 먼저 Issue를 열어 방향을 상의해 주세요.
2. 저장소를 포크하고 `main`에서 브랜치를 만들어 주세요.
3. 풀 리퀘스트 하나에는 변경 하나만 담아 주세요.
4. 빌드가 통과하는지 확인해 주세요.

   ```bash
   mvn -B package
   ```

5. HP 표시나 `/hithealth` 동작이 바뀌는 변경이라면 Paper 테스트도 실행해 주세요([빌드](README.ko.md#빌드) 참고).

   ```bash
   python3 scripts/test-hithealth-paper.py
   ```

6. 사용자에게 보이는 동작이 바뀐다면 `README.md`를 업데이트해 주세요. 번역된 README의 업데이트는 필수는 아니지만 해 주시면 큰 도움이 됩니다.

## 번역

명령어 메시지는 `src/main/java/dev/spa/hithealth/Messages.java`에 있습니다. 언어를 추가하려면 여기에 메시지를 추가하고 README에 지원 언어로 적어 주세요.

## 라이선스

기여하신 내용은 [MIT License](LICENSE)로 제공되는 데 동의한 것으로 간주합니다.
