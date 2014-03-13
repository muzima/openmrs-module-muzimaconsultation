function CreateConsultationCtrl($scope, $location, $person, $notification) {
    // initialize the page
    $scope.mode = "compose";

    $person.getAllPersons().
        then(function (response) {
            $scope.persons = response.data;
        });

    $person.getAllRoles().
        then(function(response) {
            console.log(response.data);
            $scope.roles = response.data;
            if ($scope.roles.length > 1) {
                $scope.selectedRole = $scope.roles[0];
            }
        });

    $person.getAuthenticatedPerson().
        then(function (response) {
            $scope.sender = response.data.name;
        });

    // actions
    $scope.send = function (compose) {
        // send action
        var recipient = compose.recipient.uuid;
        var recipientRole = compose.role.uuid;
        $notification.sendNotification(recipient, recipientRole, compose.subject, compose.source, compose.payload).
            then(function () {
                $location.path('/consults/outgoing/false/role/false');
            });
    }

    $scope.cancel = function () {
        $location.path('/consults/outgoing/false/role/false');
    };
}

function EditConsultationCtrl($scope, $location, $routeParams, $person, $notification) {
    // initialize the page
    $scope.mode = "view";

    // page parameter
    $scope.uuid = $routeParams.uuid;
    // get the current notification
    $notification.getNotificationByUuid($scope.uuid).
        then(function (response) {
            $scope.notification = response.data;
        });

    // actions
    $scope.reply = function () {
        $scope.mode = "reply";
        // pull sender and recipient information from the notification
        var notification = $scope.notification;
        // we're replying an incoming notification
        $scope.recipient = notification.sender.name;
        $scope.sender = notification.recipient.name;
        if ($scope.outgoing) {
            // we're replying an outgoing notification
            $scope.sender = notification.sender.name;
            $scope.recipient = notification.recipient.name;
        }
        $scope.source = notification.source;
        $scope.subject = "Re: " + notification.subject;

    };

    $scope.send = function (subject, source, payload) {
        // save action
        var recipientRole = $scope.notification.role.uuid;
        var recipient = $scope.notification.recipient.uuid;
        if ($scope.outgoing) {
            recipient = $scope.notification.sender.uuid;
        }

        $notification.sendNotification(recipient, recipientRole, subject, source, payload).
            then(function () {
                if (recipientRole === "") {
                    $location.path('/consults/outgoing/true/role/false');
                } else {
                    $location.path('/consults/outgoing/true/role/true');
                }
            });
    };

    $scope.cancel = function () {
        var recipientRole = $scope.notification.role.uuid;
        if (recipientRole === "") {
            $location.path('/consults/outgoing/true/role/false');
        } else {
            $location.path('/consults/outgoing/true/role/true');
        }
    };
}

function ListConsultationsCtrl($scope, $routeParams, $person, $notifications) {
    $scope.maxSize = 5;
    $scope.pageSize = 5;
    $scope.currentPage = 1;
    $scope.outgoing = $routeParams.outgoing;
    $scope.role = $routeParams.role;

    $scope.standardNavigation = function(whichNavigation) {
        // all of this can be simplified, if only the $scope.role and !$scope.role work properly.
        // after some testing: when the $scope.role == true, then !$scope.role should be false, but !$scope.role == true.
        // returning simple and / or operation on $scope.role and $scope.outgoing is not working here somehow.
        switch(whichNavigation) {
            case "incoming-user":
                return $scope.role || $scope.outgoing;
                if ($scope.role === 'true') {
                    return true;
                } else{
                    if ($scope.outgoing === 'true') {
                        return true;
                    } else{
                        return false;
                    }
                }
                break;
            case "outgoing-user":
                if ($scope.role === 'true') {
                    return true;
                } else{
                    if ($scope.outgoing === 'true') {
                        return false;
                    } else{
                        return true;
                    }
                }
                break;
            case "incoming-role":
                if ($scope.role === 'true') {
                    if ($scope.outgoing === 'true') {
                        return true;
                    } else{
                        return false;
                    }
                } else{
                    return true;
                }
                break;
            case "outgoing-role":
                if ($scope.role === 'true') {
                    if ($scope.outgoing === 'true') {
                        return false;
                    } else{
                        return true;
                    }
                } else {
                    return true;
                }
                break;
        }
    };

    $scope.selectedNavigation = function(whichNavigation) {
        switch(whichNavigation) {
            case "incoming-user":
                if ($scope.role === 'true') {
                    return false;
                } else{
                    if ($scope.outgoing === 'true') {
                        return false;
                    } else {
                        return true;
                    }
                }
                break;
            case "outgoing-user":
                if ($scope.role === 'true') {
                    return false;
                } else{
                    if ($scope.outgoing === 'true') {
                        return true;
                    } else {
                        return false;
                    }
                }
                break;
            case "incoming-role":
                if ($scope.role === 'true') {
                    if ($scope.outgoing === 'true') {
                        return false;
                    } else {
                        return true;
                    }
                } else{
                    return false;
                }
                break;
            case "outgoing-role":
                if ($scope.role === 'true') {
                    if ($scope.outgoing === 'true') {
                        return true;
                    } else{
                        return false;
                    }
                } else{
                    return false;
                }
                break;
        }
    };

    $scope.uuid = "";
    $scope.selectedRole = {};

    $person.getAllRoles().
        then(function(response) {
            console.log(response.data);
            $scope.roles = response.data;
            if ($scope.roles.length > 1) {
                $scope.selectedRole = $scope.roles[0];
            }
        });

    if ($scope.role === 'true') {
        $scope.roleSelected = function() {
            $scope.uuid = $scope.selectedRole.uuid;
            $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
                    $scope.search, $scope.currentPage, $scope.pageSize).
                then(function (response) {
                    var serverData = response.data;
                    $scope.notifications = serverData.objects;
                    $scope.noOfPages = serverData.pages;
                });

        };
    } else {
        $person.getAuthenticatedPerson().
            then(function (response) {
                $scope.person = response.data;
                $scope.uuid = $scope.person.uuid;
            }).
            then(function () {
                $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
                        $scope.search, $scope.currentPage, $scope.pageSize).
                    then(function (response) {
                        var serverData = response.data;
                        $scope.notifications = serverData.objects;
                        $scope.noOfPages = serverData.pages;
                    });
            });
    }

    $scope.$watch('currentPage', function (newValue, oldValue) {
        if (newValue != oldValue) {
            $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
                    $scope.search, $scope.currentPage, $scope.pageSize).
                then(function (response) {
                    var serverData = response.data;
                    $scope.notifications = serverData.objects;
                    $scope.noOfPages = serverData.pages;
                });
        }
    }, true);

    $scope.$watch('search', function (newValue, oldValue) {
        if (newValue != oldValue) {
            $scope.currentPage = 1;
            $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
                    $scope.search, $scope.currentPage, $scope.pageSize).
                then(function (response) {
                    var serverData = response.data;
                    $scope.notifications = serverData.objects;
                    $scope.noOfPages = serverData.pages;
                });
        }
    }, true);
}
