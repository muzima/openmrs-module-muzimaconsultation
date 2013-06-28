function CreateConsultationCtrl($scope, $routeParams, $person, $notification) {
    // flag whether we're displaying the user as the sender or recipient.
    $scope.sender = $routeParams.sender;
    alert("Sender: " + $scope.sender);
    $scope.uuid = $routeParams.uuid;
    alert("Uuid: " + $scope.uuid);
}

function EditConsultationCtrl($scope, $routeParams, $person, $notification) {

}

function ListConsultationsCtrl($scope, $routeParams, $person, $notifications) {
    $scope.sender = $routeParams.sender;
    $person.getAuthenticatedPerson().
        then(function(person) {
            alert("Sender: " + $scope.sender);
            alert("Uuid: " + person.uuid);
            if ($scope.sender) {
                $scope.notifications = $notifications.getNotificationFrom(person.uuid);
            } else {
                $scope.notifications = $notifications.getNotificationFor(person.uuid);
            }
        });
}