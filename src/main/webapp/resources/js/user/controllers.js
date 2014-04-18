var userModule = angular.module("user");

userModule.controller("UserController", function($scope, usersResource) {
	$scope.keyType = "user";
	var currentUser = usersResource.current({});
	currentUser.$promise.then(function() {
		if (currentUser.publicKey) {	
			if (currentUser.publicKey.indexOf("@") !== -1) {
				$scope.keyType = "envelope";
			} else {
				$scope.keyType = "user";
			}
		}
	});

});