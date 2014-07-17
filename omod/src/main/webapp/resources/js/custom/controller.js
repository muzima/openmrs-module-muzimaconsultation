function CreateConsultationCtrl($scope, $location, $person, $notification) {
    // initialize the page
    $scope.mode = "compose";

    $person.getAllPersons().
        then(function (response) {
            $scope.persons = response.data;
        });

    $person.getAllRoles().
        then(function(response) {
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
        var recipient = null;
        if (compose.recipient && compose.recipient !== 'undefined') {
            recipient = compose.recipient.uuid;
        }
        var recipientRole = null;
        if (compose.role && compose.role !== 'undefined') {
            recipientRole = compose.role.uuid;
        }
        $notification.sendNotification(recipient, recipientRole, compose.subject, compose.source, compose.payload).
            then(function () {
                $location.path('/consults/outgoing/false/role/false');
            });
    };

    $scope.cancel = function () {
        $location.path('/consults/outgoing/false/role/false');
    };
}

function EditConsultationCtrl($scope, $location, $person, $routeParams, $notification) {
    // initialize the page
    $scope.mode = "view";
    $scope.outgoing = $routeParams.outgoing;

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
        if (notification.recipient && notification.recipient !== 'undefined') {
            $scope.sender = notification.recipient.name;
        } else {
            $person.getAuthenticatedPerson().
                then(function (response) {
                    $scope.sender = response.data.name;
                });
        }
        if ($scope.outgoing === 'true') {
            // we only have one outgoing type and all outgoing will have sender and recipient information
            $scope.sender = notification.sender.name;
            $scope.recipient = notification.recipient.name;
        }

        $scope.source = notification.source;
        $scope.subject = "Re: " + notification.subject;

    };

    $scope.send = function (subject, source, payload) {
        // save action
        var recipient = $scope.notification.sender.uuid;
        if ($scope.outgoing === 'true') {
            recipient = $scope.notification.recipient.uuid;
        }

        $notification.sendNotification(recipient, null, subject, source, payload).
            then(function () {
                $location.path('/consults/outgoing/false/role/false');
            });
    };

    $scope.cancel = function () {
        var role = $scope.notification.role;
        if (role && role !== 'undefined') {
            $location.path('/consults/outgoing/false/role/true');
        } else {
            $location.path('/consults/outgoing/' + $scope.outgoing + '/role/false');
        }
    };
}

function ListConsultationsCtrl($scope, $routeParams, $person, $notifications) {
    $scope.maxSize = 5;
    $scope.pageSize = 5;
    $scope.currentPage = 1;
    $scope.outgoing = $routeParams.outgoing;
    $scope.role = $routeParams.role;
    $scope.showRead = $routeParams.showRead;

    $scope.nav = function(whichNavigation) {
        switch(whichNavigation) {
            case "incoming-user":
                return (($scope.role === 'true') || ($scope.outgoing === 'true'));
                break;
            case "outgoing-user":
                return (($scope.role === 'true') || ($scope.outgoing !== 'true'));
                break;
            case "incoming-role":
                return (($scope.role !== 'true') || ($scope.outgoing === 'true'));
                break;
            case "outgoing-role":
                return (($scope.role !== 'true') || ($scope.outgoing !== 'true'));
                break;
            default:
                return false;
        }
    };

    $scope.activeNav = function(whichNavigation) {
        switch(whichNavigation) {
            case "incoming-user":
                return (($scope.role !== 'true') && ($scope.outgoing !== 'true'));
                break;
            case "outgoing-user":
                return (($scope.role !== 'true') && ($scope.outgoing === 'true'));
                break;
            case "incoming-role":
                return (($scope.role === 'true') && ($scope.outgoing !== 'true'));
                break;
            case "outgoing-role":
                return (($scope.role === 'true') && ($scope.outgoing === 'true'));
                break;
            default:
                return false;
        }
    };

    $scope.uuid = null;
    $scope.selectedRole = null;

    $person.getAllRoles().
        then(function(response) {
            $scope.roles = response.data;
            if ($scope.roles.length > 1) {
                $scope.selectedRole = $scope.roles[0];
                if ($scope.role === 'true') {
                    $scope.roleSelected();
                }
            }
        });

    $scope.roleSelected = function() {
        $scope.uuid = $scope.selectedRole.uuid;
        $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
                $scope.search, $scope.showRead, $scope.currentPage, $scope.pageSize).
            then(function (response) {
                var serverData = response.data;
                $scope.notifications = serverData.objects;
                $scope.noOfPages = serverData.pages;
            });
    };

    if ($scope.role !== 'true') {
        $person.getAuthenticatedPerson().
            then(function (response) {
                $scope.person = response.data;
                $scope.uuid = $scope.person.uuid;
            }).
            then(function () {
                $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
                        $scope.search, $scope.showRead, $scope.currentPage, $scope.pageSize).
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
                    $scope.search, $scope.showRead, $scope.currentPage, $scope.pageSize).
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
                    $scope.search, $scope.showRead, $scope.currentPage, $scope.pageSize).
                then(function (response) {
                    var serverData = response.data;
                    $scope.notifications = serverData.objects;
                    $scope.noOfPages = serverData.pages;
                });
        }
    }, true);

    $scope.viewRead = function () {
    $notifications.getNotifications($scope.uuid, $scope.outgoing, $scope.role,
        $scope.search, $scope.showRead, $scope.currentPage, $scope.pageSize).
        then(function (response) {
            var serverData = response.data;
            $scope.notifications = serverData.objects;
            $scope.noOfPages = serverData.pages;
        });

    };
}
