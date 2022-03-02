/////////////////////////////////////////////////////
// functions for roomName, message
/////////////////////////////////////////////////////
// if enter is pressed on roomName, make sure its valid
let joined = false;
let currentRoom = "";
let myUserName;

// sending something here would be good to say it closed
// safari might not send opcode 8 when browser window is closed
// closing the socket on window close or reload fixes it
window.addEventListener('beforeunload', function (e) {
  e.preventDefault();
  ws.close();
});

function handleRoomNameKeyPress(event){
  if (event.keyCode == 13 || event.button == 0){
    event.preventDefault();
    if (validateName(roomNameEl.value) && validateUserName(userNameEl.value)){
      roomName = roomNameEl.value;
      if (roomName != currentRoom) { // only if not current room allow to join
        sendAndJoinRoom(roomName);
        currentRoom = roomName;
      } else {
        console.log("Already joined this room, ignoring");
      }
    }
  }
}

function sendAndJoinRoom(roomName){
  console.log("attempting to join " + roomName);
  ws.send("join " + roomName);
  document.getElementById("joinInfo").textContent = "(Joined)";
  joined = true;
}

function getCurrentTime() {
  let currentDate = new Date();
  let fillMins = "";
  if (currentDate.getMinutes() < 10){
    fillMins = "0";
  }
  let currentTimeString = "time: " + currentDate.getHours() % 12 + ":" + fillMins + currentDate.getMinutes();
  return currentTimeString;
}

//iterate through roomName to make sure its all lowercase
function validateName(roomName){
  // check each char for lowercase
  for (let i = 0; i < roomName.length; i++){
    if (roomName[i] < 'a' || roomName[i]> 'z'){
      invalidRoomNameWarning();
      return false;
    }
  }
  return true;
}

//iterate through userName to make sure no spaces
function validateUserName(userName){
  if (userName == "join"){
    // dont let username equal join
    alert("Username cannot equal 'join'.");
    return false;
  }
  for (let i = 0; i < userName.length; i++){
    if (userName[i] == " "){
      invalidUserNameWarning();
      return false;
    }
  }
  return true;
}

// throw alert if roomName is not valid, all lowercase
function invalidRoomNameWarning(){
  alert("Roomname must be all lowercase");
}

function invalidUserNameWarning(){
  alert("Username cannot contain spaces");
}

// on enter pressed on message textarea
// pass the value to variable to store
// store username as value
function handleMessageKeyPress(event){
  if (event.keyCode == 13 && validateUserName(userNameEl.value)){
    if (joined){
      messageText = messageEl.value;
      messageText = parseMessageText(messageText);
      userName = userNameEl.value;
      myUserName = userNameEl.value;
      sendMessage();
      event.preventDefault();
      messageEl.value = "";
    } else {
      alert("Please join room first");
      messageEl.value = "";
    }
  }
}

// because im stupidly sending json strings back from the server
// as a string in json format, " (double quote) chars break the json
// format, change all double quote messages to single quote
function parseMessageText(messageText){
  return messageText.replace(/"/g, "'");
}

function sendMessage(){
  // ws
  ws.send(userName + " " + messageText);
  console.log("attempting to send userName: " + userName + ", message: " + messageText);
}

function handleLoadCB() {
  console.log(this.responseText);
}

function handleErrorCB(){
  console.log("handleErrorCB");
}
/////////////////////////////////////////////////////
// websocket
/////////////////////////////////////////////////////
let ws = new WebSocket( "ws://10.17.132.149:8080/");

ws.onmessage =  handleMessageCB;
ws.onopen =  handleConnectCB;
ws.onclose =  handleCloseCB;
ws.onerror = handleWSErrorCB;

function handleMessageCB( event ){
  // console.log(event);
  // console.log(event.data);
  // i cant figure out how to get json to send over
  // java outputStream, so now just gonna parse strings
  let response = JSON.parse(event.data);
  console.log("recieving user: " + response.user);
  console.log("rec message: " + response.message);
  // let response = parseString(event.data);
  updateChat(response);
}

function updateChat(response){
  // username
  let newElement = document.createElement("p");
  newElement.className = "userName";
  if (response.user == myUserName){
    newElement.classList.add("fromSelf");
    newElement.classList.add("fromSelfUser");
  }
  let newText = document.createTextNode(response.user + ": ");
  newElement.appendChild(newText);
  newText = document.createElement("br");
  newElement.appendChild(newText);
  chatTextEl.appendChild(newElement);

  // message
  newElement = document.createElement("p");
  newText = document.createTextNode(response.message);
  newElement.className = "messageBody";
  if (response.user == myUserName){
    newElement.classList.add("fromSelf");
    newElement.classList.add("fromSelfMessage");
  }
  newElement.appendChild(newText);
  chatTextEl.appendChild(newElement);

  // timestamp (received at)
  newElement = document.createElement("p");
  newText = document.createTextNode("Received at: " + getCurrentTime());
  console.log("final" + getCurrentTime());
  newElement.className = ("timeStamp");
  if (response.user == myUserName){
    console.log("adding class .fromSelf");
    newElement.classList.add("fromSelf");
    newElement.classList.add("fromSelfTimestamp");
  }
  newElement.appendChild(newText);
  chatTextEl.appendChild(newElement);

  // scroll to the element
  newElement.scrollIntoView();
  newElement.id = "message";
}

function handleConnectCB(event){
  console.log("handleConnectCB (ws.onopen)");
}

function handleCloseCB(){
  console.log("handleClose");
}

function handleWSErrorCB(event){
  console.log("Error:");
  console.log(event);
  console.log("handleError");
}

/////////////////////////////////////////////////////
// elements stored as vars
/////////////////////////////////////////////////////
let userNameEl = document.getElementById("userName");
let roomNameEl = document.getElementById("roomName");
let chatTextEl = document.getElementById("chatText");
let messageEl = document.getElementById("message");
let joinButtonEl = document.getElementById("joinButton");

let messageText;
let roomName;
let userName;

roomNameEl.addEventListener("keypress", handleRoomNameKeyPress);
messageEl.addEventListener("keypress", handleMessageKeyPress);
joinButtonEl.addEventListener("click", handleRoomNameKeyPress);
