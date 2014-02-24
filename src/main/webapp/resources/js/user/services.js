var userModule = angular.module("user");

userModule.factory("userService", function(localStorage, usersResource) {
	function getPublicKey() {
		return localStorage.getItem("publicKey");
	}
	function get() {
		var publicKey = getPublicKey();
		return usersResource.get({publicKey: (publicKey ? publicKey : "null")});
	}
	function change(publicKey) {
		var user = usersResource.get({publicKey: (publicKey ? publicKey : "null")}, function() {
				if (publicKey === user.publicKey) {
					localStorage.setItem("publicKey", user.publicKey);
				}
			});
		return user;
	}
	function create() {
		var user = usersResource.create({}, function() {
				localStorage.setItem("publicKey", user.publicKey);
			});
		return user;
	}
	return {
		getPublicKey: getPublicKey,
		get: get,
		change: change,
		create: create
	};
});