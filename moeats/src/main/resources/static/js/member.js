$(function(){
	
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
			$("#email_domain").prop("readonly", false);
		}else if(str == "네이버"){
			$("#email_domain").val("naver.com");
			$("#email_domain").prop("readonly", true);
		}else if(str == "다음"){
			$("#email_domain").val("daum.net");
			$("#email_domain").prop("readonly", true);
		}else if(str == "Gmail"){
			$("#email_domain").val("gmail.com");
			$("#email_domain").prop("readonly", true);
		}
		
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
		
        $("#memberPhone").val(p1 + "-" + p2 + "-" + p3);
    });

});