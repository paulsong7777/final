<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link href="bootstrap/bootstrap.min.css" rel="stylesheet">
</head>
<body>
	<div class="container">
	<h1>JSP - 메모 리스트</h1>
		<table class="table">
		    <tr>
		      <th>NO</th>
		      <th>제목</th>
		      <th>작성자</th>
		      <th>작성일</th>
		    </tr>
			<c:if test="${ not empty mList }">
				<c:forEach var="memo" items="${ mList }">
			      <tr>
			        <td>${ memo.no }</td>        
			        <td>${ memo.title }</td>
			        <td>${ memo.writer }</td>
			        <td>${ memo.regDate }</td>      
			      </tr>
		      </c:forEach>
		    </c:if>
			<c:if test="${ empty mList }">
		      <tr>
		        <td>작성된 메모가 없습니다.</td>
		      </tr>
		    </c:if>  
		</table>
  	</div>
</body>
</html>