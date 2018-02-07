$( document ).ready(function() {
    if ("WebSocket" in window) {
        console.log("WebSocket is supported by your Browser!");
    } else {
        console.log("WebSocket NOT supported by your Browser!");
        return;
    }
    var getScriptParamUrl = function() {
        var scripts = document.getElementsByTagName('script');
        var lastScript = scripts[scripts.length-1];
        return lastScript.getAttribute('data-url');
    };

    var url = getScriptParamUrl();
    var connection = new WebSocket(url);

    connection.onopen = function() {
        console.log("Connected!");
    };
    connection.onerror = function(error) {
        console.log('WebSocket Error ', error);
    };
    connection.onmessage = function(event) {
        var obj = JSON.parse(event.data)
        var selector = "#" + obj["e"] + "\\-" + obj["t"].replace('/', '\\/')
        $(selector).html(obj["p"] + " BTC");
        $(selector).fadeTo(100, 0.08).fadeTo(200, 1.0)
    }

    console.log( "chat app is running!" );
});