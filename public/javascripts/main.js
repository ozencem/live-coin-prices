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
        for (var key in obj) {
            $("#" + key.replace('/', '\\/')).html(obj[key] + " BTC");
        }
    }

    console.log( "chat app is running!" );
});