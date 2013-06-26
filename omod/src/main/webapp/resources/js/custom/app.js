var muzimaconsultation = angular.module('muzimaconsultation', []);

muzimaconsultation.
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/muzimaconsultation', {templateUrl: '../../moduleResources/muzimaconsultation/partials/consults.html'}).
            otherwise({redirectTo: '/forms'});
    }]);

