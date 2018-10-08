

// $.ajaxSetup({
//     cache: false //关闭AJAX相应的缓存
// });

$(function () {
    var backgroundImg=document.getElementById("backgroundImg");
    var mattingImg=document.getElementById("mattingImg");
    var slideBtn = document.getElementById("slideBtn");
    var slide_indicator=document.getElementById("slide_indicator");
    var maxMove=$('.form-group').width()-$('#slideBtn').width();
    maxMove=parseInt(maxMove)

    const SUCCESS="1";
    const FAIL="2";
    const INIT="0";
    const DOWN="1";
    const MOVE="2";
    const UP ="3"
    var state=INIT;
    var action=INIT
    var x;


    getImg();

    var $body = $("body");

    $body.on('dblclick', "#backgroundImg", function () {
        if(state==SUCCESS){
            return;
        }
        getImg()
    });

    $(".tips-box").hover(showImg,hideImg) ;
    // $('#slideBtn').hover(blueBtn,whiteBtn)
    $('#slideBtn').on('mouseover',blueBtn)
    $('#slideBtn').on('mouseout',whiteBtn)


    $body.on('mousedown','#slideBtn',function (e) {
        x=0;
        if(state==SUCCESS || action!=INIT){
            return;
        }
        action=DOWN;
        e.preventDefault();
        var initX = e.clientX;

        mattingImg.style.transition = "none";
        slideBtn.style.transition = "none";
        slide_indicator.style.transition='none'

        $(document).on('mousemove',{initX:initX},move)
        $(document).on('mouseup',up)
    })

    // 拖拽
    function move(event) {
        if(!action==DOWN){
            return
        }
        action=MOVE
        x = event.clientX - event.data.initX;
        $('#slideBtn').off('mouseover').off('mouseout')
        $(".tips-box").off('mouseenter').off('mouseleave')
        if (x < 0) {
            mattingImg.style.left = "0px";
            slide_indicator.style.width="0px";
            slideBtn.style.left="0px"
        } else if (x >= 0 && x <= maxMove) {
            mattingImg.style.left = x+"px";
            slide_indicator.style.border="1px solid #1991fa"
            slide_indicator.style.width=x+40+"px";
            slideBtn.style.left=x+"px"
        } else {
            mattingImg.style.left =maxMove+"px";
            slide_indicator.style.width=$('.form-group').width()
            slideBtn.style.left=maxMove+"px";
        }
    };

    // 鼠标放开
    function up() {
        if(!action==MOVE){
            return
        }
        action=UP
        $(document).off('mousemove');
        $(document).off('mouseup')
        $(".tips-box").hover(showImg,hideImg) ;

        var postData={
            moveX:x,
            boxWidth:$('.form-group').width()
        }
        if(x>20){
            checkMove(postData)
        }else{
            mattingImg.style.left = "0px";
            slide_indicator.style.width="0px";
            slideBtn.style.left="0px"
        }
        action=INIT
    }

    function checkMove(postData) {
        $.post("checkMove.do?r="+Math.random(),postData, function (JsonResult) {
            if(JsonResult.state =="0"){
                state=SUCCESS;
                slide_indicator.style.backgroundColor="#d2f4ef"
                slide_indicator.style.borderColor="#52ccba"
                slideBtn.style.backgroundColor="#52ccba"
                $("#slide_icon").css("background-position","0px 0px")
                return;
            } else {
                $("#slide_icon").css("background-position","0px -83px")
                slide_indicator.style.backgroundColor="#fce1e1"
                slide_indicator.style.borderColor="#f57a7a"
                slideBtn.style.backgroundColor="#f57a7a"
                setTimeout(function () {
                    mattingImg.style.left = "0px";
                    mattingImg.style.transition = "left 0.3s";

                    slideBtn.style.left = "0px";
                    slideBtn.style.backgroundColor="#ffffff"
                    $("#slide_icon").css("background-position","0px -26px")
                    slideBtn.style.transition = "left 0.3s";

                    slide_indicator.style.width="0px";
                    slide_indicator.style.backgroundColor="#d1e9fe"
                    slide_indicator.style.border="1px solid transparent"
                    slide_indicator.style.transition="width 0.3s"

                    // $('#slideBtn').hover(blueBtn,whiteBtn)
                    $('#slideBtn').on('mouseover',blueBtn)
                    $('#slideBtn').on('mouseout',whiteBtn)
                },500)
                setTimeout(getImg,500)
            }
        }, "json");
    }

    //获取图片
    function getImg() {
        $.post("drawImg.do?x="+Math.random(),null, function (result) {
            if(result.state == "0"){
                var imgUrl={};
                imgUrl=result.data;
                var mattUrl=imgUrl.mattUrl;
                var bcUrl=imgUrl.bcUrl;
                mattingImg.setAttribute('src',mattUrl);
                backgroundImg.setAttribute('src',bcUrl);
            } else {
                alert("网络异常")
            }
        }, "json");
    }

    function showImg() {
        $('.slide-image-img').css({'display':'block','bottom':'15px','transition':'display 0.5s ease-in-out'})
    }
    function hideImg() {
        $('.slide-image-img').css({'display':'none','bottom':'0px'})
    }
    function blueBtn() {
        $('#slideBtn').css('background-color','#1991fa')
        $('.slideBtn span').css('background-position','0 -13px')
    }
    function whiteBtn() {
        $('#slideBtn').css('background-color','#fff')
        $('.slideBtn span').css('background-position','0 -26px')
    }

})