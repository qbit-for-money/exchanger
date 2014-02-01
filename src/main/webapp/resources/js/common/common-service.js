var commonModule = angular.module("common");

commonModule.factory("localStorage", function() {
	return window.localStorage || {
		getItem: function(key) {
			return null;
		},
		setItem: function(key, value) {
		}
	};
});

commonModule.factory("envResource", function($resource) {
	return $resource(window.context + "webapi/env");
});

commonModule.factory("userResource", function($resource) {
	return $resource(window.context + "webapi/user/:publicKey", {publicKey: ""}, {
			create: {method: "POST"},
			edit: {method: "PUT"}
		});
});

commonModule.factory("userService", function(localStorage, userResource) {
	function get() {
		var publicKey = localStorage.getItem("publicKey");
		return userResource.get({publicKey: publicKey});
	}
	function change(publicKey) {
		if (!publicKey) {
			return;
		}
		var user = userResource.get({publicKey: publicKey}, function() {
				if (publicKey === user.publicKey) {
					localStorage.setItem("publicKey", user.publicKey);
				}
			});
		return user;
	}
	function create() {
		var user = userResource.create({}, function() {
				localStorage.setItem("publicKey", user.publicKey);
			});
		return user;
	}
	return {
		get: get,
		change: change,
		create: create
	};
});