
const connectionConfig = {
  endpoint: '/chat',
  listeners: [
    '/secured/conversation/attendance',
    '/conversation/attendance'
  ],
  serverMessageChannel: '/queue/secured/channel'
}

var stompClient = null;

const sendMessage = (message) => {
  if(!!stompClient) {
    let request = JSON.stringify({
      'message': message
    })
    stompClient.send(connectionConfig.serverMessageChannel, {}, request)
  }
}

const chatConnect = (receivedMessageHandler, connectionErrorHandler) => {
  if(!stompClient) {
    stompClient = Stomp.over(new SockJS(connectionConfig.endpoint))
    stompClient.connect({}, () => {
      connectionConfig.listeners.forEach((listener) => {
        stompClient.subscribe(listener, (serverMessage) => {
          receivedMessageHandler(JSON.parse(serverMessage.body))
        }, connectionErrorHandler)
      })
    })
  }
  else alert('Already connected!')
}

const chatDisconnect = () => {
  stompClient.disconnect(() => console.log('Disconnecting...'))
  stompClient = null
}

chatConnect((msg) => console.log(msg))
