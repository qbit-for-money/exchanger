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

commonModule.factory("usersService", function(localStorage, usersResource) {
	function get() {
		var publicKey = localStorage.getItem("publicKey");
		return usersResource.get({publicKey: publicKey});
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
		get: get,
		change: change,
		create: create
	};
});

commonModule.factory("currencyResource", function($resource) {
	return $resource(window.context + "webapi/currency/:id", {id: ""}, {
			get: {method: "GET"},
			findAll: {method: "GET", isArray: true}
		});
});

commonModule.factory("currencyService", function(currencyResource) {
	function get(id) {
		return currencyResource.get({id: id});
	}
	function findAll() {
		return currencyResource.query();
	}
	return {
		get: get,
		findAll: findAll
	};
});