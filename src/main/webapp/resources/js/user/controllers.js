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
	$scope.changeUser = changeUser;

	function editUser() {
		userService.edit();
	}
	$scope.editUser = delayedProxy(editUser, 2000);

	$scope.$watch("login", function() {
		$scope.publicKey = userService.get().publicKey;
	});
	$scope.$watch("logout", function() {
		$scope.publicKey = "";
	});
});