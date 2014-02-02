var commonModule = angular.module("common");

commonModule.controller("CommonController", function($scope, $timeout, envResource, userService) {
	$scope.env = {};
	var env = envResource.get(function() {
		$scope.env = env;
	});
	
	function setUser(user) {
		if (user && user.publicKey) {
			$scope.user = user;
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
	function editOrder() {
		if (!$scope.user || !$scope.user.publicKey) {
			return;
		}
		$scope.user.$edit({publicKey: $scope.user.publicKey});
	}
	$scope.editUser = function() {
		if (editOrder.promise) {
			$timeout.cancel(editOrder.promise);
		}
		editOrder.promise = $timeout(editOrder, 1000);
	};
});