/**
 * /static/js/test/cart-item.test.js
 */
$(function() {
    console.log("🚀 [테스트 모드] 메뉴 선택(장바구니) 로직 활성화");

    // 1. 장바구니에 담기 버튼 클릭 시
    $('#btnTestAddToCart').on('click', function() {
        alert("장바구니에 메뉴가 담겼습니다.");
        // 실전에서는 담기 후 상세 페이지로 리다이렉트됨
        location.href = "room-detail.html";
    });

    // 2. 테스트 네비게이션: 돌아가기
    $('#btnTestBackToDetail').on('click', function() {
        location.href = "room-detail.html";
    });
});