const swiper = new Swiper('.product-carousel', {
    slidesPerView: 'auto',
    centeredSlides: true,
    spaceBetween: 15,
    navigation: {
        nextEl: '.swiper-button-next',
        prevEl: '.swiper-button-prev',
    }
});