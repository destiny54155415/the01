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
