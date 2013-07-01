var muzimaconsultation = angular.module('muzimaconsultation', ['ui.bootstrap']);

muzimaconsultation.
    config(['$routeProvider', '$compileProvider', function ($routeProvider, $compileProvider) {
        $compileProvider.urlSanitizationWhitelist(/^\s*(https?|ftp|mailto|file):/);
        $routeProvider.when('/consults/:sender', {controller: ListConsultationsCtrl,
            templateUrl: '../../moduleResources/muzimaconsultation/partials/consults.html'});
        $routeProvider.when('/consult/:uuid', {controller: EditConsultationCtrl,
            templateUrl: '../../moduleResources/muzimaconsultation/partials/consult.html'});
        $routeProvider.when('/newConsult', {controller: CreateConsultationCtrl,
            templateUrl: '../../moduleResources/muzimaconsultation/partials/consult.html'});
        $routeProvider.otherwise({redirectTo: '/consults/true'});
    }]);

muzimaconsultation.factory('$person', function($http) {
    var getAuthenticatedPerson = function() {
        return $http.get('authenticated.json');
    };
    var getAllPerson = function() {
        return $http.get("users.json");
    };
    return {
        getAuthenticatedPerson: getAuthenticatedPerson,
        getAllPerson: getAllPerson
    }
});

muzimaconsultation.factory('$notification', function ($http) {
    var getNotificationByUuid = function (uuid) {
        return $http.get('notification.json?uuid=' + uuid);
    };
    var saveNotification = function (notification) {
        return $http.post('notification.json', notification);
    };
    return {
        getNotificationByUuid: getNotificationByUuid,
        saveNotification: saveNotification
    }
});


muzimaconsultation.factory('$notifications', function ($http) {
    var getNotifications = function (uuid, mode) {
        return $http.get('notifications.json?uuid=' + uuid + '&sender=' + mode);
    };
    return {
        getNotifications: getNotifications
    }
});

