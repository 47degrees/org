
// Sliding Panel

$(document).ready(function(){
  $('.menu-panel-button,.menu-panel-fade-screen,.menu-panel-close').on('click touchstart',function (e) {
    $('.menu,.menu-panel-fade-screen').toggleClass('is-visible');
    e.preventDefault();
  });
});
