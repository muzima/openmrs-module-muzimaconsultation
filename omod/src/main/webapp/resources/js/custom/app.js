var muzimaconsultation = angular.module('muzimaconsultation', ['ui.bootstrap']);

muzimaconsultation.
    config(['$routeProvider', '$compileProvider', function ($routeProvider, $compileProvider) {
        $compileProvider.urlSanitizationWhitelist(/^\s*(https?|ftp|mailto|file):/);
        $routeProvider.when('/consults/outgoing/:outgoing/role/:role', {controller: ListConsultationsCtrl,
            templateUrl: '../../moduleResources/muzimaconsultation/partials/consults.html'});
        $routeProvider.when('/consult/:uuid/outgoing/:outgoing', {controller: EditConsultationCtrl,
            templateUrl: '../../moduleResources/muzimaconsultation/partials/consult.html'});
        $routeProvider.when('/newConsult', {controller: CreateConsultationCtrl,
            templateUrl: '../../moduleResources/muzimaconsultation/partials/consult.html'});
        $routeProvider.otherwise({redirectTo: '/consults/outgoing/false/role/false'});
    }]);

muzimaconsultation.factory('$person', function($http) {
    var getAuthenticatedPerson = function() {
        return $http.get('authenticated.json');
    };
    var getAllPersons = function() {
        return $http.get("users.json");
    };
    var getAllRoles = function() {
        return $http.get("roles.json");
    };
    return {
        getAuthenticatedPerson: getAuthenticatedPerson,
        getAllPersons: getAllPersons,
        getAllRoles: getAllRoles
    }
});

muzimaconsultation.factory('$notification', function ($http) {
    var getNotificationByUuid = function (uuid) {
        return $http.get('notification.json?uuid=' + uuid);
    };
    var sendNotification = function (recipient, role, subject, source, payload) {
        return $http.post('notification.json', {"recipient": recipient, "role": role, "subject": subject, "source": source, "payload": payload});
    };
    return {
        getNotificationByUuid: getNotificationByUuid,
        sendNotification: sendNotification
    }
});


muzimaconsultation.factory('$notifications', function ($http) {
    var getNotifications = function (uuid, outgoing, role, search, showRead, pageNumber, pageSize) {
        if (search === undefined) {
            search = '';
        }
        if (showRead === undefined){
            showRead = 'false';
        }
        return $http.get('notifications.json?'
            + 'uuid=' + uuid
            + '&outgoing=' + outgoing
            + '&role=' + role
            + "&search=" + search
            + '&showRead=' + showRead
            + "&pageNumber=" + pageNumber
            + "&pageSize=" + pageSize);
    };
    return {
        getNotifications: getNotifications
    }
});

