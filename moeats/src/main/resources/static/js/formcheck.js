$(function(){

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

    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$/;

    // 회원정보 수정
    $("#updateForm").on("submit", function(){

        const password = $("#member_password").val().trim();
        const nickname = $("#member_nickname").val().trim();

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

        $("#member_phone").val(p1 + "-" + p2 + "-" + p3);
    });

    // 회원가입
    $("#signupForm").on("submit", function(){

        const emailId = $("#email_id").val().trim();
        const emailDomain = $("#email_domain").val();

        const password = $("#member_password").val().trim();
        const nickname = $("#member_nickname").val().trim();

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

        $("#member_email").val(emailId + "@" + emailDomain);

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

        $("#member_phone").val(p1 + "-" + p2 + "-" + p3);
    });

});