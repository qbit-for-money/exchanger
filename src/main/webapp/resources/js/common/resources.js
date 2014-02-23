var commonModule = angular.module("common");

commonModule.factory("envResource", function($resource) {
	return $resource(window.context + "webapi/env");
});

commonModule.run(function($rootScope, envResource) {
	$rootScope.env = {};
	var env = envResource.get(function() {
		$rootScope.env = env;
	});
});
