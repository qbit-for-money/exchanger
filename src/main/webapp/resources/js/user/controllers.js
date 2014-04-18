var userModule = angular.module("user");

userModule.controller("UserController", function($scope, usersResource) {
	$scope.keyType = "user";
	var currentUser = usersResource.current({});
	currentUser.$promise.then(function() {
		if (currentUser.publicKey) {
			if (/[^\s]*@[^\s]*$/.test(currentUser.publicKey)) {
				$scope.keyType = "envelope";
			} else {
				$scope.keyType = "user";
			}
		}
	});

});