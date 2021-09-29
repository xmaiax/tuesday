const WS_URL_CONN_INFO = '/api/v1/websocket/connection-info'

angular.module('darwinApp', []).controller('darwinController', ($scope, $http) => {

  $scope.xuruvis = 'XURUUUUUUUUUUVIS'

  $scope.receivedMessageHandler = (msg) => console.log(msg)
  $scope.connectionErrorHandler = (msg) => console.error(msg)

  $scope.connect = () => $http.get(WS_URL_CONN_INFO).then((response) => {
    $scope.serverMessageChannel = response.data.serverMessageChannel
    $scope.stompClient = Stomp.over(new SockJS(response.data.endpoint))
    $scope.stompClient.connect({}, () => {
      response.data.listeners.forEach((listener) => {
        $scope.stompClient.subscribe(listener, (serverMessage) => {
          $scope.receivedMessageHandler(JSON.parse(serverMessage.body))
        }, $scope.connectionErrorHandler)
      })
    })
  }, (error) => {
    $scope.connectionErrorHandler(`HTTP ${error.status}`)
  })
  $scope.connect()

  $scope.disconnect = () => {
    $scope.stompClient.disconnect(() => $scope.connectionErrorHandler('Disconnecting...'))
    $scope.stompClient = null
  }

  $scope.sendMessage = (msg) => {
    if(!!$scope.stompClient && !!$scope.serverMessageChannel) {
      $scope.stompClient.send($scope.serverMessageChannel, {},
        JSON.stringify({ 'message': msg }))
    }
  }

})
