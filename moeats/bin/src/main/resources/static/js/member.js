$(function(){

	function toggleSubmitBtn() {
	    if ($("#signupForm").length === 0) return;

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

	    const isIdCheck = $("#isIdCheck").val() === "true";

	    const allFilled = email && domain && pw && pw2 && nick && p1 && p2 && p3;

	    if (terms && privacy && allFilled && isIdCheck) {
	        $("#submitBtn").removeClass("fake-disabled");
	    } else {
	        $("#submitBtn").addClass("fake-disabled");
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
	const emailIdRegex = /^[a-zA-Z0-9]{5,20}$/;
	
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

		if(!emailIdRegex.test(emailId)){
		    alert("이메일 아이디는 영문/숫자 조합 5~20자로 입력해주세요.");
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
	// 🔥 [수정] 모바일(천지인) 호환을 위한 입력 제한 및 유효성 검사
	// =========================

	let isComposing = false; // 한글 조합 중인지 체크하는 플래그

	// 한글 조합 시작
	$("#email_id, #email_domain").on("compositionstart", function() {
	    isComposing = true;
	});

	// 한글 조합 완료
	$("#email_id, #email_domain").on("compositionend", function() {
	    isComposing = false;
	    // 조합이 끝난 직후에 한번 검사를 트리거합니다.
	    $(this).trigger("input");
	});

	// 1. 이메일 아이디: 영문 대소문자, 숫자만 허용 (한글 입력 시 튕겨냄)
	$("#email_id").on("input", function() {
	    if (isComposing) return; // 조합 중이면 대기

	    let val = $(this).val();
	    let originalVal = val;
	    val = val.replace(/[^a-zA-Z0-9]/g, ''); // 영문/숫자 외 제거

	    if (originalVal !== val) {
	        // 한글이나 특수문자를 쳤을 때만 (경고창 대신 자연스럽게 지워짐)
	        $(this).val(val);
	    }

	    if (val.length > 20) {
	        alert("이메일 아이디는 최대 20자까지 가능합니다.");
	        $(this).val(val.substring(0, 20));
	    }
	});

	// 2. 이메일 도메인: 영문 대소문자, 마침표(.)만 허용
	$("#email_domain").on("input", function() {
	    if (isComposing) return;

	    let currentVal = $(this).val();
	    if (/[^a-zA-Z.]/.test(currentVal)) {
	        // alert 창은 UX를 해칠 수 있으므로, 모바일 환경에서는 
	        // 텍스트를 바로 치환해주는 방식이 더 안전합니다.
	        $(this).val(currentVal.replace(/[^a-zA-Z.]/g, ''));
	    }
	});

	// 4. 전화번호: 숫자만 허용 (전화번호는 한글 조합 이슈가 없으므로 그대로 유지)
	$("#phone2, #phone3").on("input", function() {
	    let currentVal = $(this).val();
	    if (/[^0-9]/.test(currentVal)) {
	        $(this).val(currentVal.replace(/[^0-9]/g, ''));
	    }
	});
});