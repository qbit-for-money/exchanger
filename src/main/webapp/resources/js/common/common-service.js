var commonModule = angular.module("common");

commonModule.factory("env", function($http) {
	return $http({method: "GET", url: window.context + "webapi/env", cache: true}).then(function(response) {return response.data;});
});