yunjeong@choeyunjeong-ui-MacBookAir live_backend % cd live-config && git checkout Master
M       application-dev.yml
M       application-local.yml
M       application-test.yml
'Master' 브랜치로 전환합니다
브랜치가 'origin/Master'보다 1개 커밋 뒤에 있고, 앞으로 돌릴 수 있습니다.
(로컬 브랜치를 업데이트하려면 "git pull"을 사용하십시오)
yunjeong@choeyunjeong-ui-MacBookAir live-config % git add .
추가"
[Master 3d945c1] feat: 카카오 api 설정 값 추가
3 files changed, 19 insertions(+), 6 deletions(-)
yunjeong@choeyunjeong-ui-MacBookAir live-config % git push origin Master
To https://github.com/Live-2025-Hanium/live-config.git
! [rejected]        Master -> Master (non-fast-forward)
error: 레퍼런스를 'https://github.com/Live-2025-Hanium/live-config.git'에 푸시하는데 실패했습니다
hint: Updates were rejected because the tip of your current branch is behind
hint: its remote counterpart. If you want to integrate the remote changes,
hint: use 'git pull' before pushing again.
hint: See the 'Note about fast-forwards' in 'git push --help' for details.