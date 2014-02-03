var commonModule = angular.module("common");

commonModule.controller("EnvController", function($scope, envResource) {
	$scope.env = {};
	var env = envResource.get(function() {
		$scope.env = env;
	});
});

commonModule.controller("UserController", function($scope, delayedProxy, usersService) {
	function setUser(user) {
		if (user && user.publicKey) {
			$scope.user = user;
			$scope.publicKey = user.publicKey;
		}
	}
	
	var user = usersService.get();
	user.$promise.then(function() {
			if (user.publicKey) {
				setUser(user);
			} else {
				user = usersService.create();
				user.$promise.then(function() {
						setUser(user);
					});
			}
		});
		
	$scope.changeUser = function() {
		$scope.publicKeyUnderCheck = true;
		var user = usersService.change($scope.publicKey);
		user.$promise.then(function() {
				setUser(user);
			}).finally(function() {
				$scope.publicKeyUnderCheck = false;
			});
	};
	$scope.createUser = function() {
		var user = usersService.create();
		user.$promise.then(function() {
				setUser(user);
			});
	};
	function editUser() {
		if (!$scope.user || !$scope.user.publicKey) {
			return;
		}
		$scope.user.$edit({publicKey: $scope.user.publicKey});
	}
	$scope.editUser = delayedProxy(editUser, 1000);
	
	function isPublicKeyValid() {
		return ($scope.user && $scope.user.publicKey && ($scope.user.publicKey === $scope.publicKey));
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
	$scope.$watch("user", updateUserProfileButton);
});