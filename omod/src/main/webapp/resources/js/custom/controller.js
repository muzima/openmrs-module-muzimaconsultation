function CreateConsultationCtrl($scope, $location, $routeParams, $person, $notification) {
    // init the page
    $scope.compose = true;
    $scope.view = false;

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

function EditConsultationCtrl($scope, $location, $routeParams, $notification) {
    // initialize the page
    $scope.uuid = $routeParams.uuid;
    $scope.view = true;
    $scope.compose = false;
    $notification.getNotificationByUuid($scope.uuid).
        then(function(response) {
            $scope.notification = response.data;
        });

    // actions
    $scope.reply = function() {
        $scope.compose = true;
    }

    $scope.cancel = function() {
        $location.path('/consults/true');
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