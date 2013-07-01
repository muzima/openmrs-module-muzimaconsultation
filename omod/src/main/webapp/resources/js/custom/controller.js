function CreateConsultationCtrl($scope, $location, $person, $notification) {
    // initialize the page
    $scope.mode = "compose";

    $scope.recipient = undefined;
    $person.getAllPerson().
        then(function(response) {
            $scope.persons = response.data;
        });
    $person.getAuthenticatedPerson().
        then(function(response) {
            $scope.sender = response.data.name;
        });
    $scope.payload = undefined;
    $scope.subject = undefined;

    // actions
    $scope.save = function() {
        // save action
    }

    $scope.cancel = function() {
        $location.path('/consults/true');
    };
}

function EditConsultationCtrl($scope, $location, $routeParams, $person, $notification) {
    // initialize the page
    $scope.mode = "view";

    // page parameter
    $scope.uuid = $routeParams.uuid;
    // get the current notification
    $notification.getNotificationByUuid($scope.uuid).
        then(function(response) {
            $scope.notification = response.data;
        });

    // actions
    $scope.reply = function() {
        $scope.mode = "reply";
        // pull sender and recipient information from the notification
        var notification = $scope.notification;
        if ($scope.outgoing) {
            // we're replying an outgoing notification
            $scope.sender = notification.sender.name;
            $scope.recipient = notification.recipient.name;
        } else {
            // we're replying an incoming notification
            $scope.recipient = notification.sender.name;
            $scope.sender = notification.recipient.name;
        }
        // empty subject and payload
        $scope.payload = undefined;
        $scope.subject = undefined;
    }

    $scope.cancel = function() {
        $location.path('/consults/true');
    }
}

function ListConsultationsCtrl($scope, $routeParams, $person, $notifications) {
    $scope.outgoing = $routeParams.outgoing;
    $person.getAuthenticatedPerson().
        then(function (response) {
            return response.data;
        }).
        then(function (person) {
            $notifications.getNotifications(person.uuid, $scope.outgoing).
                then(function (response) {
                    $scope.notifications = response.data;
                });
        });
}