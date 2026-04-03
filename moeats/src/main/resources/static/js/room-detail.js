// room-detail.js
// 주문방 협업 화면 전용 실전 JS
// 실시간 통신: WebSocket/STOMP → SSE (EventSource) 로 변경
// SSEService.join(roomIdx) 가 연결을 수락하고 roomMap에 등록
// SSEService.send(roomIdx, event) 가 해당 방 전체에 브로드캐스트

document.addEventListener('DOMContentLoaded', function () {

    const roomIdx    = document.getElementById('roomIdx')?.value;
    const roomCode   = document.getElementById('roomCode')?.value;  // URL에 사용
    const myRole     = document.getElementById('myRole')?.value;
    const roomStatus = document.getElementById('roomStatus')?.value;

    // ── 방 코드 복사 ─────────────────────────────────────────────────────────
    const btnCopyCode = document.getElementById('btnCopyCode');
    if (btnCopyCode) {
        btnCopyCode.addEventListener('click', function () {
            const code = this.dataset.code;
            if (!code) return;
            navigator.clipboard.writeText(code)
                .then(function () { alert('방 코드가 복사되었습니다: ' + code); })
                .catch(function () {
                    prompt('코드를 직접 복사하세요:', code);
                });
        });
    }

    // ── 결제 진행 버튼 비활성화 안내 ─────────────────────────────────────────
    const btnProceed = document.getElementById('btnProceedPayment');
    if (btnProceed && btnProceed.disabled) {
        btnProceed.addEventListener('mouseenter', function () {
            alert('모든 참여자가 선택을 완료하고 최소 주문 금액을 충족해야 결제를 진행할 수 있습니다.');
        });
    }

	// ── KICK (강제 퇴장) ─────────────────────────────────────────────────────
    // 실전 동작: 방장만 접근 가능 (서버에서도 leaderMemberIdx로 재검증)
    // method : POST /rooms/code/{roomCode}/kick
    // 파라미터: memberIdx (form hidden input)
    // 성공   : redirect:/rooms/code/{roomCode} (현재 페이지 갱신)
    document.querySelectorAll('.formKick').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const name = this.dataset.name || '이 참여자';
            if (!confirm(name + '님을 방에서 내보내시겠습니까?')) {
                e.preventDefault();
            }
        });
    });
	
	// SELECT(선택완료) / UNSELECT(선택취소) 방어 로직 (선택적 추가)
	const selectForm = document.querySelector('form[action$="/select"]');
	if(selectForm) {
	    selectForm.addEventListener('submit', function(e) {
	        if(!confirm('메뉴 선택을 완료하시겠습니까? 완료 후에는 장바구니를 수정할 수 없습니다.')) {
	            e.preventDefault();
	        }
	    });
	}
	
    // ── 방 나가기 확인 ───────────────────────────────────────────────────────
    // 수정: form action이 /leave 를 포함하도록 선택자 변경
    // (기존: form[action*="/members/me"] → 수정: form[action*="/leave"])
    const leaveForm = document.getElementById('formLeaveRoom');
    if (leaveForm) {
        leaveForm.addEventListener('submit', function (e) {
            if (!confirm('주문방에서 나가시겠습니까?')) {
                e.preventDefault();
            }
        });
    }

	// ── SSE 연결 (실시간 참여자 현황 갱신) ──────────────────────────────────
    // SSEService.join(roomIdx) 에 연결 → roomMap 에 등록됨
    // 백엔드 Controller 예정:
    //   @GetMapping("/rooms/code/{room_code}/sse")
    //   public SseEmitter subscribe(@PathParam("room_code") String roomCode, ...) {
    //       OrderRoom orderRoom = orderRoomService.findByCode(roomCode);
    //       return sseService.join(orderRoom.getRoomIdx());
    //   }
    //
    // 수신 이벤트 목록 (백엔드가 SSEService.send()로 전송):
    //   "connnect"        - 최초 연결 확인 (오타 주의: n 3개, SSEService 원본과 동일하게 맞춰야 함)
    //   "participantUpdate" - 참여자 입장/퇴장/kick/selectionStatus 변경 시
    //                         data: JSON { participants: [...] }
    //   "roomStatusChange"  - 방 상태 변경 시 (잠금 등)
    //                         data: JSON { roomStatus: "LOCKED" }
    //
    // EventSource는 연결 끊김 시 자동으로 재연결 시도 (WebSocket과 다른 점)
    // 단방향(서버→클라이언트)이므로 참여자 액션(담기/선택완료 등)은 기존 HTTP 폼/AJAX 그대로 사용

    if (roomStatus === 'OPEN' || roomStatus === 'SELECTING') {
        startSSE();
    }

    function startSSE() {
        // 연결 URL: GET /rooms/code/{roomCode}/sse
        const eventSource = new EventSource('/rooms/code/' + roomCode + '/sse');

        // 최초 연결 확인
        eventSource.addEventListener('connect', function (e) {
            console.log('[SSE] 연결 성공:', e.data);
        });

        // 참여자 현황 변경 (입장 / 퇴장 / kick / selectionStatus 변경)
        // data 형식: JSON 문자열 → 파싱 후 참여자 테이블 DOM 갱신
        eventSource.addEventListener('participantUpdate', function (e) {
            const data = JSON.parse(e.data);
            updateParticipantTable(data.participants);
        });

        // 방 상태 변경 (잠금 등)
        // data 형식: JSON 문자열 → roomStatus 값 확인 후 페이지 처리
        eventSource.addEventListener('roomStatusChange', function (e) {
            const data = JSON.parse(e.data);
            if (data.roomStatus === 'LOCKED') {
                // 방 잠금: 페이지 새로고침으로 결제 화면 버튼 표시
                location.reload();
            }
        });

        // 연결 오류 처리 (EventSource는 자동 재연결하므로 로그만 기록)
        eventSource.onerror = function (e) {
            console.warn('[SSE] 연결 오류 또는 재연결 중:', e);
        };

        // 페이지 이탈 시 연결 해제 (SSEService의 onCompletion/onTimeout 콜백 트리거)
        window.addEventListener('beforeunload', function () {
            eventSource.close();
        });
    }

    // ── 참여자 테이블 DOM 갱신 함수 (SSE participantUpdate 이벤트 수신 시 호출) ─
    // 백엔드에서 participants 배열을 JSON으로 전송하면 이 함수로 테이블을 교체
    // 예상 데이터 구조:
    // [{ memberIdx, memberName, role, selectionStatus, cartTotal }]
    function updateParticipantTable(participants) {
        const tbody = document.getElementById('participantTbody');
        if (!tbody || !participants) return;

        tbody.innerHTML = participants.map(function (p) {
            const isLeader    = p.role === 'LEADER';
            const statusLabel = p.selectionStatus === 'SELECTED' ? '[완료]' : '[선택중]';
            const cartTotal   = (p.cartTotal || 0).toLocaleString() + '원';

            // kick 버튼은 방장(myRole=LEADER)이 보는 화면에서 참여자에게만 표시
            // myRole은 hidden input에서 JS 변수로 이미 읽혀 있음
            const kickCell = (myRole === 'LEADER' && !isLeader)
                ? `<td>
                       <form class="formKick"
                             action="/rooms/code/${roomCode}/kick"
                             method="post"
                             data-name="${p.memberName}">
                           <input type="hidden" name="memberIdx" value="${p.memberIdx}">
                           <button type="submit" class="btnKick">내보내기</button>
                       </form>
                   </td>`
                : '<td>-</td>';

            return `<tr data-role="${p.role}" data-member-idx="${p.memberIdx}">
                        <td>${p.memberName}</td>
                        <td>${isLeader ? '방장' : '참여자'}</td>
                        <td>${statusLabel}</td>
                        <td class="participantCartTotal">${cartTotal}</td>
                        ${kickCell}
                    </tr>`;
        }).join('');

        // DOM 갱신 후 kick 폼에 confirm 이벤트 재등록
        document.querySelectorAll('.formKick').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                const name = this.dataset.name || '이 참여자';
                if (!confirm(name + '님을 방에서 내보내시겠습니까?')) {
                    e.preventDefault();
                }
            });
        });
    }
	
	// 구형코드 : see 채용 이전 내용
    // ── WebSocket 연결 (STOMP) ────────────────────────────────────────────────
    // 실제 연동 시 sockjs-client + stomp.js 사용
    // 현재는 폴링 비활성화 (주석 해제 시 3초마다 참여자 현황 갱신)
    // if (roomStatus === 'OPEN' || roomStatus === 'SELECTING') {
    //    startPolling();
    // }
	//
    //  function startPolling() {
    //    /*
    //    setInterval(function () {
    //        // location.reload() 대신 필요한 데이터만 AJAX로 가져와 DOM 업데이트
    //        $.get('/rooms/code/' + roomCode + '/status', function (data) {
    //            // 참여자 현황 테이블 업데이트 예정
    //        });
    //    }, 3000);
    //    */
    //    console.log('[room-detail] 폴링 준비 완료. 실제 연동 시 WebSocket으로 교체 예정.');
    //	}

});	// document.addEventListener('DOMContentLoaded', function () {});
