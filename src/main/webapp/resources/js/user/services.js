var userModule = angular.module("user");

userModule.factory("userService", function($rootScope, localStorage, usersResource) {
	var user;
	
	var publicKey = localStorage.getItem("publicKey");
	if (publicKey) {
		var lastUser = usersResource.get({publicKey: publicKey});
		lastUser.$promise.then(function() {
			if (lastUser.publicKey) {
				_set(lastUser);
			} else {
				create();
			}
		}, function() {
			_reset();
		});
	} else {
		create();
	}
	
	function get() {
		return user;
	}
	function change(publicKey) {
		var newUser = usersResource.get({publicKey: (publicKey ? publicKey : "null")}, function() {
			if (publicKey === newUser.publicKey) {
				_set(newUser);
			} else {
				_reset();
			}
		}, function() {
			_reset();
		});
		return newUser;
	}
	function create() {
		var newUser = usersResource.create({}, function() {
			if (newUser && newUser.publicKey) {
				_set(newUser);
			} else {
				_reset();
			}
		}, function() {
			_reset();
		});
		return newUser;
	}
	function edit() {
		if (!user || !user.publicKey) {
			return;
		}
		user.$edit({publicKey: user.publicKey});
	}
	
	function _set(newUser) {
		if (newUser && newUser.publicKey) {
			user = newUser;
			$rootScope.user = newUser;
			localStorage.setItem("publicKey", newUser.publicKey);
			$rootScope.$broadcast("login", newUser);
		} else {
			_reset();
		}
	}
	function _reset() {
		var oldUser = user;
		user = null;
		$rootScope.user = null;
		localStorage.removeItem("publicKey");
		$rootScope.$broadcast("logout", oldUser);
	}
	
	return {
		get: get,
		change: change,
		create: create,
		edit: edit
	};
});