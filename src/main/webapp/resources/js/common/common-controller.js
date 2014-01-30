var commonModule = angular.module("common");

commonModule.controller("CommonController", function($scope, env) {
	$scope.env = env;
});
