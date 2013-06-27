var muzimaconsultation = angular.module('muzimaconsultation', []);

muzimaconsultation.
    config(['$routeProvider', '$compileProvider', function ($routeProvider, $compileProvider) {
        $compileProvider.urlSanitizationWhitelist(/^\s*(https?|ftp|mailto|file):/);
        $routeProvider.when('/consults', {templateUrl: '../../moduleResources/muzimaconsultation/partials/consults.html'});
        $routeProvider.when('/consult/:uuid', {templateUrl: '../../moduleResources/muzimaconsultation/partials/consult.html'});
        $routeProvider.otherwise({redirectTo: '/consults'});
    }]);

