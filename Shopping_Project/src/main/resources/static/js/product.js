/* 상품 상세: 하단 썸네일 클릭 시 대표 이미지 교체 */
(function () {
    var main = document.getElementById('productMainImage');
    if (!main) {
        return;
    }
    var thumbs = document.querySelectorAll('.product-thumb[data-full]');
    thumbs.forEach(function (thumb) {
        thumb.addEventListener('click', function () {
            var full = thumb.getAttribute('data-full');
            if (!full) {
                return;
            }
            main.setAttribute('src', full);
            thumbs.forEach(function (t) {
                t.classList.remove('is-active');
            });
            thumb.classList.add('is-active');
        });
    });
})();

/* 바로구매 */
function buyNow(productId) {

    const quantityInput = document.getElementById('quantity');

    if (!quantityInput) {
        alert('상품 수량을 확인할 수 없습니다.');
        return;
    }

    const quantity = parseInt(quantityInput.value, 10);
    const maxStock = parseInt(quantityInput.max, 10);

    // 수량 확인
    if (!Number.isInteger(quantity) || quantity < 1) {
        alert('구매 수량을 1개 이상 입력해주세요.');
        quantityInput.value = 1;
        return;
    }

    // 재고 확인
    if (Number.isInteger(maxStock) && quantity > maxStock) {
        alert('재고가 부족합니다. 최대 구매 가능 수량은 ' + maxStock + '개입니다.');
        quantityInput.value = maxStock;
        return;
    }

    // 주문서 페이지로 이동
    location.href = `/orders/buy-now?productId=${productId}&quantity=${quantity}`;
}
