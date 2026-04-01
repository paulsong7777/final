package com.springbootstudy.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.springbootstudy.app.domain.Memo;

/* 이전에는 DAO(Data Access Object) 클래스에 @Repository 애노테이션을
 * 적용하여 해당 클래스가 DB 작업을 하는 클래스 임을 명시하고
 * MyBatis의 Mapper 인터페이스나 XML 맵퍼를 통해서 DB와 통신하였다.
 * 이 때 DAO 클래스에서 XML 맵퍼 파일에 정의한 namespace라는 속성과
 * 맵퍼 파일 안에 작성한 SQL 쿼리(맵핑 구문)의 id를 조합해 SQL 쿼리를
 * 호출했었다. 하지만 요즘에는 아래와 같이 자바 인터페이스에 @Mapper
 * 애노테이션을 적용하고 이 인터페이스의 메서드에 @Select, @Insert, 
 * @Update, @Delete 등의 애노테이션을 지정해 쿼리를 직접 맵핑할 수도 있다.
 * 또한 별도의 XML 맵퍼 파일을 만들고 그 안에 SQL 쿼리를 정의하여 이 맵핑
 * 구문을 호출할 수도 있다. 이 때 한 가지 주의할 점은 @Mapper 애노테이션을
 * 적용한 인터페이스와 XML 맵퍼 파일은 namespace라는 속성으로 연결되기
 * 때문에 XML 맵퍼 파일의 namespace를 정의할 때 맵퍼 인터페이스의 완전한
 * 클래스 이름과 동일한 namespace를 사용해야 한다는 것이다.
 * 
 * @Mapper는 MyBatis 3.0부터 지원하는 애노테이션으로 이 애노테이션이 붙은
 * 인터페이스는 별도의 구현 클래스를 작성하지 않아도 MyBatis 맵퍼로 인식해
 * 스프링 Bean으로 등록되며 Service 클래스에서 주입 받아 사용할 수 있다. 
 **/
@Mapper
public interface MemoMapper {
	
	// 맵퍼 인터페이스는 애노테이션을 사용해 SQL 쿼리를 직접 맵핑 할 수 있다.
	@Select("SELECT * FROM memo")
	List<Memo> memoList();
	
	/* 맵퍼 인터페이스는 별도의 XML 맵퍼 파일에 SQL 쿼리를 정의해 놓고 그 쿼리를
	 * 사용할 수 있는데 이 때 맵퍼 인터페이스는 XML 맵퍼에 정의한 SQL 쿼리 중에서
	 * 인터페이스의 메서드명과 id가 동일한 SQL 쿼리(맵핑 구문)를 찾아 실행한다.
	 **/  
	List<Memo> findAll();
	
	// no에 해당하는 메모를 테이블에서 읽어오는 메서드	
	Memo findByNo(int no);
	
	// 메모를 테이블에 저장하는 메서드
	void addMemo(Memo memo);
	
	// no에 해당하는 메모를 테이블에서 수정하는 메서드
	void updateMemo(Memo memo);
	
	// no에 해당하는 메모를 테이블에서 삭제하는 메서드
	void deleteMemo(int no);
}
