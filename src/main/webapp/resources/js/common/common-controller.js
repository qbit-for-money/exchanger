var commonModule = angular.module("common");

commonModule.controller("EnvController", function($rootScope, envResource) {
	$rootScope.env = {};
	var env = envResource.get(function() {
		$rootScope.env = env;
	});
});