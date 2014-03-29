var userModule = angular.module("user");

userModule.factory("userService", function($rootScope, usersResource) {
	var user;
	
	var currentUser = usersResource.current({});
	currentUser.$promise.then(function() {
		if (currentUser.publicKey) {
			_set(currentUser);
		} else {
			_reset();
		}
	}, function() {
		_reset();
	});
	
	function get() {
		return user;
	}
	
	function _set(currentUser) {
		if (currentUser && currentUser.publicKey) {
			user = currentUser;
			$rootScope.user = currentUser;
			$rootScope.$broadcast("login", currentUser);
		} else {
			_reset();
		}
	}
	function _reset() {
		var oldUser = user;
		user = null;
		$rootScope.user = null;
		$rootScope.$broadcast("logout", oldUser);
	}
	
	return {
		get: get
	};
});