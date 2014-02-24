var userModule = angular.module("user");

userModule.factory("usersResource", function($resource) {
	return $resource(window.context + "webapi/users/:publicKey", {publicKey: ""}, {
			create: {method: "POST"},
			edit: {method: "PUT"}
		});
});