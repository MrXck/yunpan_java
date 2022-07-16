let scrollTop = 0;
$(window).scroll(function () {
    scrollTop = $(window).scrollTop()
})
let mouseDown = false;
(function () {
    let main = $('.main')
    main[0].onmousedown = function () {
        mouseDown = true
        var selList = [];
        var fileNodes = document.getElementsByClassName("item");
        for (var i = 0; i < fileNodes.length; i++) {
            if (fileNodes[i].className.indexOf("item") !== -1) {
                fileNodes[i].className = "item";
                selList.push(fileNodes[i]);
            }
        }
        var isSelect = true;
        var evt = window.event || arguments[0];
        var startX = (evt.x || evt.clientX);
        var startY = (evt.y || evt.clientY);
        var selDiv = document.createElement("div");
        selDiv.style.cssText = "position:fixed;width:0px;height:0px;font-size:0px;margin:0px;padding:0px;border:1px solid #0099FF;background-color:#C3D5ED;z-index:1000;filter:alpha(opacity:60);opacity:0.6;display:none;";
        selDiv.id = "selectDiv";
        document.body.appendChild(selDiv);
        selDiv.style.left = startX + "px";
        selDiv.style.top = startY + "px";
        var _x = null;
        var _y = null;
        clearEventBubble(evt);
        document.onmousemove = function () {
            if (mouseDown) {
                evt = window.event || arguments[0];
                if (isSelect) {
                    if (selDiv.style.display == "none") {
                        selDiv.style.display = "";
                    }
                    _x = (evt.x || evt.clientX);
                    _y = (evt.y || evt.clientY);
                    selDiv.style.left = Math.min(_x, startX) + "px";
                    selDiv.style.top = Math.min(_y, startY) + "px";
                    selDiv.style.width = Math.abs(_x - startX) + "px";
                    selDiv.style.height = Math.abs(_y - startY) + "px";
                    // ---------------- 关键算法 ---------------------
                    var _l = selDiv.offsetLeft - parseInt($('.sidebar-sticky').css('width').replace('px', '')),
                        _t = selDiv.offsetTop + scrollTop - parseInt($('.navbar-dark').css('height').replace('px', ''));
                    var _w = selDiv.offsetWidth, _h = selDiv.offsetHeight;
                    for (var i = 0; i < selList.length; i++) {
                        var sl = selList[i].offsetWidth + selList[i].offsetLeft;
                        var st = selList[i].offsetHeight + selList[i].offsetTop;
                        if (sl > _l && st > _t && selList[i].offsetLeft < _l + _w && selList[i].offsetTop < _t + _h) {
                            if (selList[i].className.indexOf("seled") == -1) {
                                selList[i].className = selList[i].className + " seled";
                            }
                        } else {
                            if (selList[i].className.indexOf("seled") != -1) {
                                selList[i].className = "item";
                            }
                        }
                    }
                }
                clearEventBubble(evt);
            }
        }
        document.onmouseup = function () {
            mouseDown = false
            isSelect = false;
            if (selDiv) {
                document.body.removeChild(selDiv);
                showSelDiv(selList);
            }
            selList = null, _x = null, _y = null, selDiv = null, startX = null, startY = null, evt = null;
        }
    }
})();

function clearEventBubble(evt) {
    if (evt.stopPropagation)
        evt.stopPropagation();
    else
        evt.cancelBubble = true;
    if (evt.preventDefault)
        evt.preventDefault();
    else
        evt.returnValue = false;
}

function showSelDiv(arr) {
    vm.operationList = []
    for (var i = 0; i < arr.length; i++) {
        if (arr[i].className.indexOf("seled") != -1) {
            vm.operationList.push($(arr[i]).find('input').val())
        }
    }
}

$(function () {
    $('.main').css('min-height', document.documentElement.clientHeight - parseInt($('.navbar-dark').css('height').replace('px', '')) - 50 - 36)
})
window.onresize = () => {
    return (() => {
        $('.main').css('min-height', document.documentElement.clientHeight - parseInt($('.navbar-dark').css('height').replace('px', '')) - 50 - 36)
    })()
}