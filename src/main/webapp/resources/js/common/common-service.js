var commonModule = angular.module("common");

commonModule.factory("localStorage", function() {
	return window.localStorage || {
		getItem: function(key) {
			return this[key];
		},
		setItem: function(key, value) {
			this[key] = value;
		}
	};
});

commonModule.factory("delayedProxy", function($timeout) {
	return function(f, delay) {
			var proxy = function() {
				if (proxy.promise) {
					$timeout.cancel(proxy.promise);
				}
				var self = this;
				var args = arguments;
				proxy.promise = $timeout(function() {
						f.apply(self, args);
					}, delay);
			};
			return proxy;
		};
});

commonModule.factory("envResource", function($resource) {
	return $resource(window.context + "webapi/env");
});

commonModule.factory("usersResource", function($resource) {
	return $resource(window.context + "webapi/users/:publicKey", {publicKey: ""}, {
			create: {method: "POST"},
			edit: {method: "PUT"}
		});
});

commonModule.factory("userService", function(localStorage, usersResource) {
	function getPublicKey() {
		return localStorage.getItem("publicKey");
	}
	function get() {
		return usersResource.get({publicKey: getPublicKey()});
	}
	function change(publicKey) {
		var user = usersResource.get({publicKey: publicKey}, function() {
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