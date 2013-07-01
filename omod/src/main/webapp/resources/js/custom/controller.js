function CreateConsultationCtrl($scope, $routeParams, $person, $notification) {
    // flag whether we're displaying the user as the sender or recipient.
    $scope.sender = $routeParams.sender;
    console.log("Sender: " + $scope.sender);
    $scope.uuid = $routeParams.uuid;
    console.log("Uuid: " + $scope.uuid);
    $scope.compose = true;
}

function EditConsultationCtrl($scope, $routeParams, $notification) {
    $scope.uuid = $routeParams.uuid;
    $notification.getNotificationByUuid($scope.uuid).
        then(function(response) {
            $scope.notification = response.data;
        });

    $scope.reply = function() {
        $scope.edit = true;
    }
}

function ListConsultationsCtrl($scope, $routeParams, $person, $notifications) {
    $scope.sender = $routeParams.sender;
    $person.getAuthenticatedPerson().
        then(function (response) {
            return response.data;
        }).
        then(function (person) {
            $notifications.getNotifications(person.uuid, $scope.sender).
                then(function (response) {
                    $scope.notifications = response.data;
                });
        });
}