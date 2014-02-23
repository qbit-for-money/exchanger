var userModule = angular.module("user");

userModule.controller("UserController", function($rootScope, $scope, delayedProxy, userService) {
	function setUser(user) {
		if (user && user.publicKey) {
			$rootScope.user = user;
			$scope.publicKey = user.publicKey;
		}
	}
	
	var user = userService.get();
	user.$promise.then(function() {
			if (user.publicKey) {
				setUser(user);
			} else {
				user = userService.create();
				user.$promise.then(function() {
						setUser(user);
					});
			}
		});
		
	$scope.changeUser = function() {
		$scope.publicKeyUnderCheck = true;
		var user = userService.change($scope.publicKey);
		user.$promise.then(function() {
				setUser(user);
			}).finally(function() {
				$scope.publicKeyUnderCheck = false;
			});
	};
	
	$scope.createUser = function() {
		var user = userService.create();
		user.$promise.then(function() {
				setUser(user);
			});
	};
	
	function editUser() {
		if (!$rootScope.user || !$rootScope.user.publicKey) {
			return;
		}
		$rootScope.user.$edit({publicKey: $rootScope.user.publicKey});
	}
	$scope.editUser = delayedProxy(editUser, 2000);
	
	function isPublicKeyValid() {
		return ($rootScope.user && $rootScope.user.publicKey && ($rootScope.user.publicKey === $scope.publicKey));
	}
	$scope.isPublicKeyValid = isPublicKeyValid;
	
	function updateUserProfileButton() {
		if (isPublicKeyValid()) {
			angular.element("#userProfileButton").removeAttr("disabled");
		} else {
			angular.element("#userProfileButton").attr("disabled", "true");
		}
	}
	$scope.$watch("publicKey", updateUserProfileButton);
	$rootScope.$watch("user", updateUserProfileButton);
});