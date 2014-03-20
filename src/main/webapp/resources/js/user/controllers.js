var userModule = angular.module("user");

userModule.controller("UserController", function($scope, delayedProxy, userService) {
	function createUser() {
		userService.create();
	}
	$scope.createUser = createUser;
	
	function changeUser() {
		$scope.publicKeyUnderCheck = true;
		var user = userService.change($scope.publicKey);
		user.$promise.finally(function() {
			$scope.publicKeyUnderCheck = false;
		});
	}
	$scope.changeUser = delayedProxy(changeUser, 1000);

	function editUser() {
		userService.edit();
	}
	$scope.editUser = delayedProxy(editUser, 1000);

	$scope.$on("login", function() {
		$scope.publicKey = userService.get().publicKey;
	});
	$scope.$on("logout", function() {
		// do nothing
	});
});