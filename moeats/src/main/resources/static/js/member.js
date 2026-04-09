$(function(){

	function toggleSubmitBtn() {
		    const terms = $("#agreeTerms").is(":checked");
		    const privacy = $("#agreePrivacy").is(":checked");
			
			const email = $("#email_id").val().trim();
			const domain = $("#email_domain").val().trim();
			const pw = $("#memberPassword").val().trim();
			const pw2 = $("#memberPassword1").val().trim();
			const nick = $("#memberNickname").val().trim();
			const p1 = $("#phone1").val().trim();
			const p2 = $("#phone2").val().trim();
			const p3 = $("#phone3").val().trim();

			const allFilled =
			    email && domain && pw && pw2 && nick && p1 && p2 && p3;

			// 💡 핵심 변경점: disabled 속성 대신 클래스를 조작합니다.
			if (terms && privacy && allFilled) {
			    $("#submitBtn").removeClass("fake-disabled"); // 파란색으로 활성화!
			} else {
			    $("#submitBtn").addClass("fake-disabled"); // 회색 + 마우스 X 표시
			}
		}

	// 전체 동의
	$("#agreeAll").on("change", function(){
	    const checked = $(this).is(":checked");

	    $("#agreeTerms").prop("checked", checked);
	    $("#agreePrivacy").prop("checked", checked);

	    toggleSubmitBtn();
	});

	// 개별 체크
	$("#agreeTerms, #agreePrivacy").on("change", function(){
	    const allChecked =
	        $("#agreeTerms").is(":checked") &&
	        $("#agreePrivacy").is(":checked");

	    $("#agreeAll").prop("checked", allChecked);

	    toggleSubmitBtn();
	});

	// ⭐ 초기 상태 반영 (중요)
	toggleSubmitBtn();
	$("#signupForm input").on("input", toggleSubmitBtn);
		
	// 이메일 중복확인
	$("#btnOverlapId").on("click", function(){

	    const emailId = $("#email_id").val().trim();
	    const emailDomain = $("#email_domain").val().trim();

	    if(emailId === "" || emailDomain === ""){
	        alert("이메일을 입력하세요");
	        return;
	    }

	    const email = emailId + "@" + emailDomain;

	    $.ajax({
	        url: "/members/email-check",
	        type: "get",
	        data: { memberEmail: email },

	        success: function(res){
	            if(res){
	                alert("사용 가능한 이메일입니다.");
	                $("#isIdCheck").val("true");
	            }else{
	                alert("이미 사용중인 이메일입니다.");
	                $("#isIdCheck").val("false");
	            }
				toggleSubmitBtn();
	        },
	        error: function(){
	            alert("서버 오류 발생");
	        }
	    });

	});
	
	// 이메일 도메인 체크
	$("#selectDomain").on("change", function(){
		console.log("change 실행됨");
		let str=$(this).val();
		
		if(str == "직접입력"){
			$("#email_domain").val("");
			$("#email_domain").prop("readonly", false).focus();
		}else if(str == "naver.com"){
			$("#email_domain").val("naver.com");
			$("#email_domain").prop("readonly", true);
		}else if(str == "daum.net"){
			$("#email_domain").val("daum.net");
			$("#email_domain").prop("readonly", true);
		}else if(str == "gmail.com"){
			$("#email_domain").val("gmail.com");
			$("#email_domain").prop("readonly", true);
		}
		
		toggleSubmitBtn();
	})
	
	
    // =========================
    // 🔥 [추가] 기존 값 분리 세팅
    // =========================

    // 이메일 분리
    const originEmail = $("#originEmail").val();
    if(originEmail){
        const emailParts = originEmail.split("@");

        if(emailParts.length === 2){
            $("#email_id").val(emailParts[0]);

            // select에 해당 값이 있으면 선택
            $("#email_domain").val(emailParts[1]);
        }
    }

    // 전화번호 분리
    const originPhone = $("#originPhone").val();
    if(originPhone){
        const phoneParts = originPhone.split("-");

        if(phoneParts.length === 3){
            $("#phone1").val(phoneParts[0]);
            $("#phone2").val(phoneParts[1]);
            $("#phone3").val(phoneParts[2]);
        }
    }

    // =========================
    // 기존 코드 유지
    // =========================

    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[\W_]).{8,20}$/;
	const nicknameRegex = /^[가-힣a-zA-Z0-9]{1,10}$/;
	
    // 회원정보 수정
    $("#updateForm").on("submit", function(){

        const password = $("#memberPassword").val().trim();
        const nickname = $("#memberNickname").val().trim();

        const p1 = $("#phone1").val().trim();
        const p2 = $("#phone2").val().trim();
        const p3 = $("#phone3").val().trim();

        if(password === ""){
            alert("비밀번호를 입력해주세요");
            return false;
        }

        if(!passwordRegex.test(password)){
            alert("비밀번호는 8~20자, 대소문자/숫자/특수문자를 포함해야 합니다.");
            return false;
        }

        if(nickname === ""){
            alert("별명을 입력해주세요");
            return false;
        }

        if(p1.length !== 3 || p2.length !== 4 || p3.length !== 4){
            alert("전화번호 형식을 확인해주세요 (010-1234-5678)");
            return false;
        }

        if(!/^\d+$/.test(p1+p2+p3)){
            alert("전화번호는 숫자만 입력해주세요");
            return false;
        }

        $("#memberPhone").val(p1 + "-" + p2 + "-" + p3);
    });

    // 회원가입
    $("#signupForm").on("submit", function(){

        const emailId = $("#email_id").val().trim();
        const emailDomain = $("#email_domain").val();

        const password = $("#memberPassword").val().trim();
        const password1 = $("#memberPassword1").val().trim();
        const nickname = $("#memberNickname").val().trim();

        const p1 = $("#phone1").val().trim();
        const p2 = $("#phone2").val().trim();
        const p3 = $("#phone3").val().trim();

        if(emailId === ""){
            alert("이메일 아이디를 입력해주세요");
            return false;
        }

        if(emailId.length < 5){
            alert("이메일 아이디는 5자 이상 입력해주세요");
            return false;
        }

        if(!/^[a-zA-Z0-9]+$/.test(emailId)){
            alert("이메일 아이디는 영문과 숫자만 가능합니다.");
            return false;
        }

        if(emailDomain === ""){
            alert("이메일 도메인을 선택해주세요");
            return false;
        }

        $("#memberEmail").val(emailId + "@" + emailDomain);

        if(password === ""){
            alert("비밀번호를 입력해주세요");
            return false;
        }

        if(password1 === ""){
            alert("비밀번호 확인을 입력해주세요");
            return false;
        }
		
		if(password != password1){
			alert("비밀번호가 일치하지 않습니다.")
			return false;
		}
		
        if(!passwordRegex.test(password)){
            alert("비밀번호는 8~20자, 대소문자/숫자/특수문자를 포함해야 합니다.");
            return false;
        }

        if(nickname === ""){
            alert("별명을 입력해주세요");
            return false;
        }
	
		if(!nicknameRegex.test(nickname)){
		    alert("닉네임은 한글, 영어, 숫자만 사용 가능하며 10자 이하로 입력해주세요.");
		    return false;
		}
		
        if(p1.length !== 3 || p2.length !== 4 || p3.length !== 4){
            alert("전화번호 형식을 확인해주세요 (010-1234-5678)");
            return false;
        }

        if(!/^\d+$/.test(p1+p2+p3)){
            alert("전화번호는 숫자만 입력해주세요");
            return false;
        }
		
		
		if($("#isIdCheck").val() !== "true"){
		    alert("이메일 중복 확인을 해주세요");
		    return false;
		}
		
		// 약관 체크
		if(!$("#agreeTerms").is(":checked") || !$("#agreePrivacy").is(":checked")){
		    alert("약관 동의 필요");
		    return false;
		}
		
        $("#memberPhone").val(p1 + "-" + p2 + "-" + p3);
		
		if(!confirm("회원가입 하시겠습니까?")){
			return false;
		}
    });
	
	// =========================
		// 🔥 [수정] 이메일 실시간 입력 제한 (경고창 추가)
		// =========================

		// 1. 이메일 아이디: 영문 대소문자, 숫자만 허용
		$("#email_id").on("input", function() {
			let currentVal = $(this).val();
			
			// 영문, 숫자가 아닌 문자가 하나라도 입력되었는지 검사
			if (/[^a-zA-Z0-9]/.test(currentVal)) {
				alert("이메일 아이디는 영문과 숫자만 입력 가능합니다.");
				// 잘못 입력된 문자(한글 등)만 지우고, 원래 쓰던 영문/숫자는 그대로 복구
				$(this).val(currentVal.replace(/[^a-zA-Z0-9]/g, ''));
			}
		});

		// 2. 이메일 도메인: 영문 대소문자, 마침표(.)만 허용
		$("#email_domain").on("input", function() {
			let currentVal = $(this).val();
			
			// 영문, 마침표(.)가 아닌 문자가 하나라도 입력되었는지 검사
			if (/[^a-zA-Z.]/.test(currentVal)) {
				alert("이메일 도메인은 영문과 마침표(.)만 입력 가능합니다.");
				// 잘못 입력된 문자만 지우고, 원래 쓰던 영문/마침표는 그대로 복구
				$(this).val(currentVal.replace(/[^a-zA-Z.]/g, ''));
			}
		});
		
		// 3. 별명 검사 (두 단계로 분리)
			
			// (1) 타이핑 중: 특수문자와 공백만 실시간 차단 (자음/모음은 조합을 위해 임시 허용)
			$("#memberNickname").on("input", function() {
				let currentVal = $(this).val();
				
				// 영문, 숫자, 한글(자음/모음 포함)이 아닌 문자(특수문자, 띄어쓰기)가 들어오면
				if (/[^a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣]/.test(currentVal)) {
					alert("별명에는 특수문자나 공백을 사용할 수 없습니다.");
					$(this).val(currentVal.replace(/[^a-zA-Z0-9ㄱ-ㅎㅏ-ㅣ가-힣]/g, ''));
				}
			});

			// (2) 입력 완료 후 (포커스가 벗어날 때): 단독 자음/모음이 남아있는지 최종 검사
			$("#memberNickname").on("blur", function() {
				let currentVal = $(this).val();
				
				// 완성되지 않은 자음이나 모음(ㄱ~ㅎ, ㅏ~ㅣ)이 텍스트에 섞여 있다면
				if (/[ㄱ-ㅎㅏ-ㅣ]/.test(currentVal)) {
					alert("별명에 자음이나 모음만 단독으로 남겨둘 수 없습니다. (예: ㅋㅋ, ㅇㄴㅁ)");
					// 단독으로 쓰인 자음/모음만 싹 지워버림
					$(this).val(currentVal.replace(/[ㄱ-ㅎㅏ-ㅣ]/g, ''));
				}
			});

			// 4. 전화번호 (가운데, 끝 자리): 숫자만 허용
			$("#phone2, #phone3").on("input", function() {
				let currentVal = $(this).val();
				
				// 숫자가 아닌 문자가 하나라도 입력되었는지 검사
				if (/[^0-9]/.test(currentVal)) {
					alert("전화번호는 숫자만 입력 가능합니다.");
					// 숫자가 아닌 문자를 모두 지움
					$(this).val(currentVal.replace(/[^0-9]/g, ''));
				}
			});
});