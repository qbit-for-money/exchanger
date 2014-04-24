var userModule = angular.module("user");

userModule.controller("UserController", function($scope, usersResource, captchaAuthResource, userService) {
	$scope.keyType = "user";
	$scope.logoutButton = "";
	var currentUser = usersResource.current({});
	//userService._reset();
	currentUser.$promise.then(function() {
		if (currentUser.publicKey) {
			if (currentUser.publicKey.indexOf("@") !== -1) {
				$scope.keyType = "envelope";
			} else {
				$scope.keyType = "user";
			}
			$scope.logoutButton = "glyphicon-log-out";
		}
	});

	$scope.logout = function() {
		captchaAuthResource.logout({});
		location.reload();
	};
});