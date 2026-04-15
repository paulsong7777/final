$(function(){
	
	const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[\W_]).{8,20}$/;
	const nicknameRegex = /^[가-힣a-zA-Z0-9]{1,10}$/;
	const emailIdRegex = /^[a-zA-Z0-9]{5,20}$/;

	// [수정] alert 대신 moShowToast 사용을 위한 헬퍼 함수
	function notify(message, variant = 'warning') {
		if (typeof window.moShowToast === 'function') {
			window.moShowToast(message, variant);
		} else {
			alert(message); // 만약 로드되지 않았을 경우를 대비한 폴백
		}
	}

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

	toggleSubmitBtn();
	$("#signupForm input").on("input", toggleSubmitBtn);
		
	// 이메일 중복확인
	$("#btnOverlapId").on("click", function(){
	    const emailId = $("#email_id").val().trim();
	    const emailDomain = $("#email_domain").val().trim();

	    if(emailId === "" || emailDomain === ""){
	        notify("이메일을 입력하세요");
	        return;
	    }
		
		if(!emailIdRegex.test(emailId)){
	        notify("이메일 아이디는 영문/숫자 조합 5~20자로 입력해주세요.");
	        $("#email_id").focus();
	        return;
		}

	    const email = emailId + "@" + emailDomain;

	    $.ajax({
	        url: "/members/email-check",
	        type: "get",
	        data: { memberEmail: email },
	        success: function(res){
	            if(res){
	                notify("사용 가능한 이메일입니다.", "success");
	                $("#isIdCheck").val("true");
	            }else{
	                notify("이미 사용중인 이메일입니다.");
	                $("#isIdCheck").val("false");
	            }
				toggleSubmitBtn();
	        },
	        error: function(){
	            notify("서버 오류 발생");
	        }
	    });
	});
	
	// 이메일 도메인 체크
	$("#selectDomain").on("change", function(){
		let str=$(this).val();
		if(str == "직접입력"){
			$("#email_domain").val("");
			$("#email_domain").prop("readonly", false).focus();
		}else{
			$("#email_domain").val(str);
			$("#email_domain").prop("readonly", true);
		}
		toggleSubmitBtn();
	});

    // 회원정보 수정
    $("#updateForm").on("submit", function(){
        const password = $("#memberPassword").val().trim();
        const nickname = $("#memberNickname").val().trim();
        const p1 = $("#phone1").val().trim();
        const p2 = $("#phone2").val().trim();
        const p3 = $("#phone3").val().trim();

        if(password === ""){
            notify("비밀번호를 입력해주세요");
            return false;
        }
        if(!passwordRegex.test(password)){
            notify("비밀번호는 8~20자, 대소문자/숫자/특수문자를 포함해야 합니다.");
            return false;
        }
        if(nickname === ""){
            notify("별명을 입력해주세요");
            return false;
        }
        if(p1.length !== 3 || p2.length !== 4 || p3.length !== 4){
            notify("전화번호 형식을 확인해주세요");
            return false;
        }
        if(!/^\d+$/.test(p1+p2+p3)){
            notify("전화번호는 숫자만 입력해주세요");
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
            notify("이메일 아이디를 입력해주세요");
            return false;
        }
		if(!emailIdRegex.test(emailId)){
		    notify("이메일 아이디는 영문/숫자 조합 5~20자로 입력해주세요.");
		    return false;
		}
        if(emailDomain === ""){
            notify("이메일 도메인을 선택해주세요");
            return false;
        }
        $("#memberEmail").val(emailId + "@" + emailDomain);

        if(password === ""){
            notify("비밀번호를 입력해주세요");
            return false;
        }
        if(password1 === ""){
            notify("비밀번호 확인을 입력해주세요");
            return false;
        }
		if(password != password1){
			notify("비밀번호가 일치하지 않습니다.");
			return false;
		}
        if(!passwordRegex.test(password)){
            notify("비밀번호는 8~20자, 대소문자/숫자/특수문자를 포함해야 합니다.");
            return false;
        }
        if(nickname === ""){
            notify("별명을 입력해주세요");
            return false;
        }
		if(!nicknameRegex.test(nickname)){
		    notify("닉네임은 한글, 영어, 숫자만 사용 가능하며 10자 이하로 입력해주세요.");
		    return false;
		}
        if(p1.length !== 3 || p2.length !== 4 || p3.length !== 4){
            notify("전화번호 형식을 확인해주세요");
            return false;
        }
        if(!/^\d+$/.test(p1+p2+p3)){
            notify("전화번호는 숫자만 입력해주세요");
            return false;
        }
		if($("#isIdCheck").val() !== "true"){
		    notify("이메일 중복 확인을 해주세요");
		    return false;
		}
		if(!$("#agreeTerms").is(":checked") || !$("#agreePrivacy").is(":checked")){
		    notify("약관 동의가 필요합니다.");
		    return false;
		}
		
        $("#memberPhone").val(p1 + "-" + p2 + "-" + p3);
		
		// 가입 확인은 Toast가 아닌 기존 confirm(또는 커스텀 모달)이 적합합니다.
		if(!confirm("회원가입 하시겠습니까?")){
			return false;
		}
    });
	
	// 실시간 입력 제한 (Toast 남발을 방지하기 위해 최대 글자수 초과 시에만 notify 호출)
	// =========================
		// 🔥 실시간 입력 제한 및 알림
		// =========================
		let isComposing = false;
		$("#email_id, #email_domain, #memberPassword").on("compositionstart", function() { isComposing = true; });
		$("#email_id, #email_domain, #memberPassword").on("compositionend", function() { isComposing = false; $(this).trigger("input"); });

		// 1. 이메일 아이디 제한
		$("#email_id").on("input", function() {
		    if (isComposing) return;
		    let val = $(this).val();
		    let cleanVal = val.replace(/[^a-zA-Z0-9]/g, '');
		    if (val !== cleanVal) $(this).val(cleanVal);
		    if (cleanVal.length > 20) {
		        notify("이메일 아이디는 최대 20자까지 가능합니다.");
		        $(this).val(cleanVal.substring(0, 20));
		    }
		});

		// 2. 비밀번호 실시간 제한 (추가됨)
		$("#memberPassword, #memberPassword1").on("input", function() {
		    if (isComposing) return;
		    let val = $(this).val();
		    if (val.length > 20) {
		        notify("비밀번호는 최대 20자까지 가능합니다.");
		        $(this).val(val.substring(0, 20));
		    }
		});

		// 3. 이메일 도메인 제한
		$("#email_domain").on("input", function() {
		    if (isComposing) return;
		    let currentVal = $(this).val();
		    if (/[^a-zA-Z.]/.test(currentVal)) {
		        $(this).val(currentVal.replace(/[^a-zA-Z.]/g, ''));
		    }
		});

		// 4. 전화번호 숫자 제한
		$("#phone2, #phone3").on("input", function() {
		    let currentVal = $(this).val();
		    if (/[^0-9]/.test(currentVal)) {
		        $(this).val(currentVal.replace(/[^0-9]/g, ''));
		    }
		});
});