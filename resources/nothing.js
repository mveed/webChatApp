
function handleConnectCB(){

}

function handleMessageCB( event ){
  wsTa.value = event.data;
}

function handleCloseCB(){

}

function handleWSErrorCB(){

}




function handleErrorCB() {
  console.log("An ajax error...")
}

function handleLoadCB() {
  resultTa.value = this.responseText;
}

function handleKeyPressCB(event){
  console.log("key pressed: " + event.keyCode);
  if (event.keyCode == 13 || event.type == "click"){
    let x = Number(xTa.value);
    let y = Number(yTa.value);
    console.log("test" + x);
    if (isNaN(x)){
      alert("Please make sure x is a number");
      xTa.value = "Enter Number";
    }

    resultTa.value = x + y;
    console.log(resultTa.value);
    let request = new XMLHttpRequest();
    request.open("GET", "http://localhost:8080/calculate?x=" + x + "&y=" + y);
    request.overrideMimeType("text/html");
    request.addEventListener("load", handleLoadCB);
    request.addEventListener("error", handleErrorCB);
    request.send();

    // ws
    ws.send(x + " " + y);
  }
}

let xTa = document.getElementById("xTA");
let yTa = document.getElementById("yTA");
let resultTa = document.getElementById("resultTA");
let wsTa = document.getElementById("wsTA");

let ws = new WebSocket( "ws://localhost:8080");

ws.onmessage =  handleMessageCB;
ws.onconnect =  handleConnectCB;
ws.onclose =  handleCloseCB;
ws.onerror = handleWSErrorCB;

let button = document.querySelector("button");
button.addEventListener("click", handleKeyPressCB);

xTa.addEventListener("keypress", handleKeyPressCB );
yTa.addEventListener("keypress", handleKeyPressCB );
