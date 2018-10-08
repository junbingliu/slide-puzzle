
$(function () {

    function SlideCode(targetDom) {
        if (!(this instanceof SlideCode)) return new SlideCode(targetDom);
        // $("#"+targetDom).load('http://p12.local.com:8080/DrawImg/resource/plugin/JigsawValidateUtil.html')
        // $('head').children(':last').attr({
        //     rel: "stylesheet",
        //     type: 'text/css',
        //     href: 'http://p12.local.com:8080/DrawImg/resource/plugin/JigsawValidateUtil.css',
        // });
        var text2="<meta http-equiv=\"Access-Control-Allow-Origin\" content=\"*\" />"
        $('head mata').last().after(text2)
        $('head').children(':last').after('<link href="http://p12.local.com:8080/DrawImg/resource/plugin/JigsawValidateUtil.css" type="text/css" rel="stylesheet" />')
        var url = 'http://p12.local.com:8080/DrawImg/resource/plugin/JigsawValidateUtil.text';
        // $.ajax({
        //     url:url,
        //     dataType: 'jsonp',
        //     // crossDomain: true,
        //     success: function(data) {
        //         // $("#"+targetDom).append(data)
        //         $("#sliderTEST").load(data)
        //         // $('body').children(':last').after(data)
        //     }
        // });
        $.ajax({
            url: url,
            dataType: "text",
            crossDomain: true,
            success: function(data,ts){
                console.log("-=--=")
                $("#sliderTEST").append(data)
            }
        });
        this.init();
    }

    var _proto=SlideCode.prototype

    _proto.init=function () {
        var _this=this;
        this.state='INIT';
        this.action='INIT';
        _this.initImg();
        _this.initMatt();
        _this.initSlideBtn();
        _this.initIndicator();

        this.maxMove=$('#slide_bar').width()-$('#mattingImg')[0].offsetWidth;
        this.maxMove=parseInt(this.maxMove);
        this.moveX=0;
        this.sendProp='';

        var $body = $("body");

        $body.on('dblclick', "#backgroundImg",_this,function () {
            if(_this.state=="success"){
                return;
            }
            _this.initImg();
            _this.initIndicator();
            _this.initSlideBtn();
            _this.initMatt();
        });

        $body.on('mousedown','#slideBtn',_this,function (e) {
            _this.moveX=0;
            if(_this.state=="success" || _this.action!="INIT"){
                return;
            }
            _this.action="DOWN";
            e.preventDefault();
            var initX = e.clientX;

            _this.blueBtn();
            $(document).on('mousemove',{initX:initX,_this:_this},_this.move)
            $(document).on('mouseup',{_this:_this},_this.up)
        })
    }

    _proto.move=function (event) {
        var _this=event.data._this;
        if(!_this.action=="DOWN"){
            return
        }
        _this.action="MOVE"
        _this.moveX = event.clientX - event.data.initX;
        $('#slideBtn').off('mouseover').off('mouseout')
        if (_this.moveX < 0) {
            _this.initIndicator()
            _this.initSlideBtn()
            _this.initMatt()
        } else if (_this.moveX >= 0 && _this.moveX <= _this.maxMove) {
            $('#mattingImg').css('left',_this.moveX+'px')
            $('#slide_indicator').css('border','1px solid #1991fa')
            $('#slide_indicator').css('width',_this.moveX+$('#slideBtn').width()+"px")
            $('#slideBtn').css('left',_this.moveX+'px')
        } else {
            $('#mattingImg').css('left',_this.maxMove+'px')
            $('#slide_indicator').css('border','1px solid #1991fa')
            $('#slide_indicator').css('width',_this.maxMove+$('#slideBtn').width()+'px')
            $('#slideBtn').css('left',_this.maxMove+"px")
        }
    }

    _proto.up=function (event) {
        var _this=event.data._this;
        if(!_this.action=="MOVE"){
            return
        }
        _this.action="UP"
        $(document).off('mousemove');
        $(document).off('mouseup')

        var postData={
            moveX:_this.moveX,
            boxWidth:$('#slide_bar').width(),
            p: _this.sendProp
        }
        _this.checkMove(postData)
        _this.action="INIT"
    }

    _proto.setSendProp=function (prop) {
        this.sendProp=prop;
    }

    _proto.initImg=function () {
        var _this=this
        _this.state="INIT"
        $.post("/" + appId + "/handler/drawImages.jsx?x="+Math.random(),null, function (result) {
            if(result.code == "0"){
                var bgFilePath=result.bgFilePath;
                var mattingFilePath=result.mattingFilePath;
                $('#mattingImg').attr('src',mattingFilePath);
                $('#backgroundImg').attr('src',bgFilePath);
            } else {
                alert(result.msg)
            }
        }, "json");
    }

    _proto.checkMove=function (postData) {
        var _this=this
        $.post("/" + appId + "/handler/getLogMobileCaptcha.jsx?x="+Math.random(),postData, function (resp) {
            if (resp.state == "0") {
                //位移正确
                _this.rightIndicator();
                setTimeout(function () {
                    $('#myModal').modal('hide')//收起模态框
                },1000)

                _this.moveSuccess(resp);

            } else {
                //位移错误
                _this.wrongIndicator();
                setTimeout(function () {
                    $('#mattingImg').animate({
                        left:'0'
                    },200)
                    $('#slideBtn').animate({
                        left:'0'
                    },200)
                    $('#slideBtn').css("background-color","#ffffff")
                    $("#slide_icon").css("background-position","0px -26px")
                    //初始化移入移出效果
                    $('#slideBtn').on('mouseover',_this.blueBtn)
                    $('#slideBtn').on('mouseout',_this.whiteBtn)
                    $('#slide_indicator').animate({
                        width:'0px'
                    },200)
                    $('#slide_indicator').css("background-color","#d1e9fe")
                    $('#slide_indicator').css("border","1px solid transparent")
                    _this.initImg();
                },500)

            }
        }, "json");
    }

    _proto.setSucceed=function (func) {
        var _this=this
        _this.moveSuccess=func;
    }

    _proto.moveSuccess=function (response) {

    }

    _proto.initMatt=function () {
        $('#mattingImg').css('left','0px')
    }

    _proto.initSlideBtn=function () {
        var _this=this
        //初始化位置
        $('#slideBtn').css('left','0px')
        $('#slideBtn').css('background-color','#ffffff')
        $("#slide_icon").css("background-position","0px -26px")
        //初始化移入移出效果
        $('#slideBtn').on('mouseover',_this.blueBtn)
        $('#slideBtn').on('mouseout',_this.whiteBtn)
    }

    _proto.initIndicator=function () {
        $('#slide_indicator').css('width','0px')
        $('#slide_indicator').css('border','1px solid transparent')
        $('#slide_indicator').css('background-color','#d1e9fe')
    }

    _proto.wrongIndicator=function () {
        var _this=this
        _this.state="FAIL";
        $("#slide_icon").css("background-position","0px -83px")
        $('#slide_indicator').css('background-color','#fce1e1')
        $('#slide_indicator').css('border-color','#f57a7a')
        $('#slideBtn').css('background-color','#f57a7a')
    }

    _proto.rightIndicator=function () {
        var _this=this
        _this.state="success";
        $("#slide_icon").css("background-position","0px 0px")
        $('#slide_indicator').css('background-color','#d2f4ef')
        $('#slide_indicator').css('border-color','#52ccba')
        $('#slideBtn').css('background-color','#52ccba')
    }

    _proto.whiteBtn=function () {
        $('#slideBtn').css('background-color','#fff')
        $('.slideBtn span').css('background-position','0 -26px')
    }

    _proto.blueBtn=function () {
        $('#slideBtn').css('background-color','#1991fa')
        $('.slideBtn span').css('background-position','0 -13px')
    }

    _proto.showSlidecodeModal=function () {
        // var _this=this
        // _this.initImg();
        // _this.initMatt();
        // _this.initSlideBtn();
        // _this.initIndicator();
        $('#myModal').modal('show')
    }
    window.SlideCode=SlideCode;
})

