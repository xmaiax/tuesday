
const WEBSOCKET_ENDPOINT = '/chat'
const WEBSOCKET_LISTENER = '/secured/conversation/attendance'
const WEBSOCKET_MESSAGE_BROKER = '/queue/secured/channel'

var stompClient = null;

const sendMessage = (message) => {
  if(!!stompClient) {
    let request = JSON.stringify({
      'message': message
    })
    stompClient.send(WEBSOCKET_MESSAGE_BROKER, {}, request)
  }
}

const chatConnect = (receivedMessageHandler) => {
  if(!stompClient) {
    stompClient = Stomp.over(new SockJS(WEBSOCKET_ENDPOINT))
    stompClient.connect({}, (frame) => {
      stompClient.subscribe(WEBSOCKET_LISTENER, (serverMessage) => {
        receivedMessageHandler(JSON.parse(serverMessage.body))
      }, (errorMessage) => {
        stompClient = null
        console.log(errorMessage)
        alert('Connection error...')
      })
    })
  }
  else alert('Already connected!')
}

const chatDisconnect = () => {
  stompClient.disconnect(() => console.log('Disconnecting...'))
  stompClient = null
}
