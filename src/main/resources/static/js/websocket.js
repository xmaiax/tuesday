const WS_URL_CONN_INFO = '/api/v1/websocket/connection-info'
const MSG_ALREADY_CONNECTED = 'J\u00e1 est\u00e1 conectado no websocket!'
const MSG_PREFIX_ERROR_LOADING_CONN_INFO = 'Erro ao buscar informa\u00e7\u00f5es de conex\u00e3o'
const MSG_DISCONNECTING = 'Desconectando...'

angular.module('darwinApp', []).controller('darwinController', ($scope, $http) => {

  $scope.sendMessage = (msg) => {
    if(!!$scope.stompClient && !!$scope.serverMessageChannel) {
      $scope.stompClient.send($scope.serverMessageChannel, {},
        JSON.stringify({ 'message': msg }))
    }
  }

  $scope.disconnect = () => {
    $scope.stompClient.disconnect(() => $scope.connectionErrorHandler(MSG_DISCONNECTING))
    $scope.stompClient = null
    $scope.serverMessageChannel = null
  }

  $scope.receivedMessageHandler = (msg) => console.log(msg)
  $scope.connectionErrorHandler = (msg) => console.error(msg)

  $scope.connect = () => { if(!$scope.stompClient) {
    $http.get(WS_URL_CONN_INFO).then((response) => {
      $scope.serverMessageChannel = response.data.serverMessageChannel
      $scope.stompClient = Stomp.over(new SockJS(response.data.endpoint))
      $scope.stompClient.connect({}, () => {
        response.data.listeners.forEach((listener) => {
          $scope.stompClient.subscribe(listener, (serverMessage) => {
            $scope.receivedMessageHandler(JSON.parse(serverMessage.body))
          }, $scope.connectionErrorHandler)
        })
      }, $scope.disconnect)
    }, (error) => {
      $scope.connectionErrorHandler(`${MSG_PREFIX_ERROR_LOADING_CONN_INFO} (HTTP ${error.status})`)
    }) } else { $scope.connectionErrorHandler(MSG_ALREADY_CONNECTED) }
  }
  $scope.connect()

  $scope.xuruvis = 'XURUUUUUUUUUUVIS'

})
