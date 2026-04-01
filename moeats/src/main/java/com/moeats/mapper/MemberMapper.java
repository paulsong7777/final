package com.moeats.mapper;

import org.apache.ibatis.annotations.Mapper;

<<<<<<< HEAD

=======
<<<<<<< HEAD

/* @Mapper는 MyBatis 3.0부터 지원하는 애노테이션으로 이 애노테이션이 붙은
 * 인터페이스는 별도의 구현 클래스를 작성하지 않아도 MyBatis 맵퍼로 인식해
 * 스프링 Bean으로 등록되며 Service 클래스에서 주입 받아 사용할 수 있다. 
 * 
 * @Mapper 애노테이션을 적용한 인터페이스와 XML 맵퍼 파일은 namespace라는
 * 속성으로 연결되기 때문에 XML 맵퍼 파일의 namespace를 정의할 때 맵퍼 
 * 인터페이스의 완전한 클래스 이름과 동일한 namespace를 사용해야 한다. 
 **/



=======
>>>>>>> 1e76dca (myOrderList.html추가 커밋)
>>>>>>> a241daed0341297332c508edf883a682b80f8ba0
import com.moeats.domain.Member;

@Mapper
public interface MemberMapper {
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> a241daed0341297332c508edf883a682b80f8ba0
	
	
	// 회원정보 수정
	public void updateMember(Member member);
	
	// 비밀번호 확인
	public String isPassCheck(int memberIdx);
	
	// 회원가입
	public void insertMember(Member member);
	
	// 회원 이메일 조회
	public Member getMemberFromEmail(String memberEmail);
	
	// 회원 조회
	public Member getMember(int memberIdx);
	
}

<<<<<<< HEAD
=======
=======

	public void updateMember(Member member);
}
>>>>>>> 1e76dca (myOrderList.html추가 커밋)
>>>>>>> a241daed0341297332c508edf883a682b80f8ba0
