$(function(){
	
	
	
	// 회원정보 수정
	$("#updateForm").on("submit", function(e){
		if($("#{member_password}")<=0){
			alert("비밀번호를 입력해주세요");
			return false;
		}
		if($("#{member_nickname}")<=0){
			alert("별명을 입력해주세요");
			return false;
		}
		if($("#{member_phone}")<=0){
			alert("전화번호를 입력해주세요");
			return false;
		}
	});
	
	// 회원가입
	$("#signupForm").on("submit", function(e){
		if($("#{member_email}")<=0){
			alert("이메일을 입력해주세요");
			return false;
		}
		if($("#{member_password}")<=0){
			alert("비밀번호를 입력해주세요");
			return false;
		}
		if($("#{member_nickname}")<=0){
			alert("별명을 입력해주세요");
			return false;
		}
		if($("#{member_phone}")<=0){
			alert("전화번호를 입력해주세요");
			return false;
		}
		
	});
	
});