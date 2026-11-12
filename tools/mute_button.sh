#!/bin/bash
# 가상 핸들 MUTE 버튼.
# 에뮬레이터에 가상 하드웨어 키보드(uinput)를 만들고,
# 터미널에서 스페이스나 엔터를 누를 때마다 MUTE 키 한 번을 보낸다.
#   q : 종료
# 사용법: ./tools/mute_button.sh

ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"

if ! "$ADB" get-state >/dev/null 2>&1; then
  echo "에뮬레이터가 연결되어 있지 않습니다. 에뮬레이터를 먼저 켜 주세요." >&2
  exit 1
fi

echo "가상 MUTE 버튼 준비 중..." >&2
echo "  스페이스/엔터 = MUTE 한 번 누르기,  q = 종료" >&2

count=0
{
  # 키보드 장치 등록 (EV_KEY, KEY_MUTE=113)
  echo '{"id":1,"command":"register","name":"Virtual Steering MUTE","vid":6353,"pid":4661,"bus":"usb","configuration":[{"type":100,"data":[1]},{"type":101,"data":[113]}]}'
  sleep 2
  echo "준비 완료. 눌러 보세요." >&2
  while IFS= read -rsn1 key; do
    [[ "$key" == "q" || "$key" == "Q" ]] && break
    if [[ "$key" == " " || -z "$key" ]]; then
      count=$((count + 1))
      echo '{"id":1,"command":"inject","events":[1,113,1,0,0,0]}'
      echo '{"id":1,"command":"delay","duration":80}'
      echo '{"id":1,"command":"inject","events":[1,113,0,0,0,0]}'
      echo "  MUTE 누름 #$count" >&2
    fi
  done
  echo '{"id":1,"command":"delay","duration":300}'
  echo "종료했습니다. 총 ${count}번 눌렀습니다." >&2
} | "$ADB" shell uinput - >/dev/null 2>&1
