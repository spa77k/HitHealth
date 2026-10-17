# HitHealth

[English](README.md) | [日本語](README.ja.md) | [简体中文](README.zh-CN.md) | **한국어**

공격한 대상의 남은 HP를 액션바에 표시하는 PaperMC 플러그인입니다.

```
좀비 ❤ 12.0/20.0
```

## 요구 사항

- Minecraft 서버: PaperMC 26.2·26.3 (Paper API `1.21.4` 이상)
- Java: 21 이상
- 의존 플러그인: 없음
- 베드락 에디션: Geyser를 통해 접속한 플레이어에게도 표시됩니다

## 기능

- 몹이나 플레이어를 공격하면 공격자의 액션바에 대상의 이름과 남은 HP가 표시됩니다.
- 근접 공격(검 등)뿐만 아니라 발사체(활, 쇠뇌, 삼지창 등) 및 길들인 펫(늑대 등)에 의한 공격도 지원합니다.
- 남은 HP 비율에 따라 HP 색상이 변경됩니다 (기본값: 50% 이상 초록색, 25% 이상 노란색, 25% 미만 빨간색).
- 이름표 등으로 이름을 붙인 몹은 지정된 이름을 표시하며, 그 외의 몹은 각 플레이어의 클라이언트 언어에 맞는 이름을 표시합니다.
- 대상을 처치했을 때는 `0.0`으로 표시됩니다. 살아 있는 대상의 소수점은 올림 처리되므로 생존 중에 `0.0`으로 표시되지 않습니다.
- 갑옷 거치대는 표시 대상에서 제외됩니다.
- 플러그인 안내 메시지는 각 플레이어의 클라이언트 언어(영어, 일본어, 중국어 간체, 한국어)에 맞춰 표시됩니다 (기타 언어는 영어).

## 설치

1. Releases에서 `hithealth-1.0.0.jar`를 다운로드합니다.
2. 서버의 `plugins/` 폴더에 JAR 파일을 넣습니다.
3. 서버를 재시작합니다.

## 명령어

| 명령어 | 설명 | 권한 |
|---|---|---|
| `/hithealth` | 내 HP 표시 켜기/끄기 전환 | `hithealth.toggle` (모두) |
| `/hithealth on` | 내 HP 표시 켜기 | `hithealth.toggle` (모두) |
| `/hithealth off` | 내 HP 표시 끄기 | `hithealth.toggle` (모두) |
| `/hithealth reload` | `config.yml` 다시 불러오기 | `hithealth.reload` (OP) |

플레이어별 끄기 설정은 `plugins/HitHealth/players.yml`에 저장되며 서버 재시작 후에도 유지됩니다.

## 권한

| 권한 | 설명 | 기본값 |
|---|---|---|
| `hithealth.use` | 공격한 대상의 HP가 표시됨 | 모두 |
| `hithealth.toggle` | `/hithealth`로 내 HP 표시를 전환할 수 있음 | 모두 |
| `hithealth.reload` | `/hithealth reload`로 설정을 다시 불러올 수 있음 | OP |

## 설정 (`plugins/HitHealth/config.yml`)

```yaml
# MiniMessage 형식. <name>: 대상 이름, <hp>: 남은 HP, <max>: 최대 HP, <color>...</color>: 남은 HP에 따른 색상
format: "<white><name></white> <color>❤ <hp>/<max></color>"

thresholds:
  high: 0.5    # 이 비율 이상이면 colors.high
  low: 0.25    # 이 비율 이상이면 colors.mid, 미만이면 colors.low
colors:
  high: "#55FF55"
  mid: "#FFFF55"
  low: "#FF5555"

decimals: 1          # HP 소수점 자릿수 (0-2)
show-players: true   # 플레이어를 공격할 때도 표시
```

| 설정 키 | 기본값 | 설명 |
|---|---|---|
| `format` | `"<white><name></white> <color>❤ <hp>/<max></color>"` | 액션바 표시 형식 (MiniMessage 형식). 사용 가능한 플레이스홀더: `<name>`, `<hp>`, `<max>`, `<color>` |
| `thresholds.high` | `0.5` | 높은 HP 색상의 기준 비율 (50% 이상) |
| `thresholds.low` | `0.25` | 중간 HP 색상의 기준 비율 (25% 이상, 미만은 낮은 HP 색상) |
| `colors.high` | `"#55FF55"` | HP 비율이 `thresholds.high` 이상일 때의 색상 코드 |
| `colors.mid` | `"#FFFF55"` | HP 비율이 `thresholds.low` 이상 `thresholds.high` 미만일 때의 색상 코드 |
| `colors.low` | `"#FF5555"` | HP 비율이 `thresholds.low` 미만일 때의 색상 코드 |
| `decimals` | `1` | HP 표시의 소수점 자릿수 (0-2) |
| `show-players` | `true` | 다른 플레이어를 공격할 때도 HP를 표시할지 여부 |

### 피해량, 표시 시간 및 제외 설정

설정 파일 형식은 같지만 유지하려면 위 단계에 따라 수동으로 복사해야 합니다. 다음 항목을 `config.yml`에 추가하고 `/hithealth reload`를 실행하면 새 기능을 설정할 수 있습니다.

```yaml
damage:
  enabled: false
  format: " <gray>(-<damage>)</gray>"
display:
  duration-seconds: 0.0
exclusions:
  enabled: true
  worlds: []           # 예: [world_nether, lobby]
  entity-types: []     # 예: [WITHER, ENDER_DRAGON]
```

- `damage.enabled: true`로 설정하면 `좀비 ❤ 12.0/20.0 (-8.0)`처럼 피해량을 덧붙입니다. 피해량은 이벤트의 피해 감소 적용 후 최종 HP 피해이며, 흡수 체력이 막은 양을 제외하고 공격 전 HP를 상한으로 적용합니다. 회복, 죽음 방지 효과 또는 다른 플러그인 때문에 실제 HP 변화량과 다를 수 있습니다.
- `damage.format`은 기본 `format` 뒤에 붙으며 같은 플레이스홀더를 지원합니다. 기본 형식에 `<damage>`를 직접 넣을 수도 있습니다. `damage.enabled`는 뒤에 붙는 표시만 제어합니다. HP와 피해량은 같은 `decimals` 설정을 사용합니다.
- `display.duration-seconds: 0`은 한 번만 보내고 클라이언트의 자연스러운 사라짐 효과를 사용합니다. 양수이면 매초 대상의 HP를 갱신하고 시간이 지나면 표시를 지웁니다. 유효한 공격마다 대상과 피해량을 바꾸고 시간을 다시 시작합니다.
- 표시 시간은 최대 60초이며 양의 소수 값은 서버 tick 단위로 올림합니다. 잘못된 값, 음수 및 유한하지 않은 값은 0으로 되돌리고, 60을 넘으면 60으로 제한합니다. 실제 시간은 서버 tick 속도에 따라 달라집니다.
- 표시 끄기, 서버 나가기, 설정 다시 불러오기 또는 플러그인 종료 시 대기 중이거나 진행 중인 표시를 취소합니다.
- 대상의 월드 이름 또는 Bukkit `EntityType` 이름 중 하나가 목록과 일치하면 제외합니다. 대소문자를 구분하지 않습니다. 번역된 이름이나 `minecraft:zombie` 대신 `ZOMBIE` 같은 열거형 이름을 사용하세요. 잘못된 항목은 콘솔 경고와 함께 무시합니다.
- 빈 목록은 아무것도 제외하지 않습니다. `exclusions.enabled: false`는 목록을 유지한 채 필터를 끕니다. `show-players: false` 및 갑옷 거치대 제외는 계속 적용됩니다.

다른 플러그인도 액션바를 사용하면 서로의 표시를 덮어쓸 수 있습니다. 빌드와 테스트 방법은 [English README](README.md#build-and-tests)를 참고하세요.

## 라이선스

[MIT License](LICENSE)
