var userModule = angular.module("user");

userModule.factory("usersResource", function($resource) {
	return $resource(window.context + "webapi/users", {}, {
		current: {method: "GET", url: window.context + "webapi/users/current"},
		logout: {method: "POST", url: window.context + "webapi/users/logout"}
	});
});