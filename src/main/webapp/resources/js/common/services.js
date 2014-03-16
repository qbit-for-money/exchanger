var commonModule = angular.module("common");

commonModule.factory("storage", function() {
	return {
		getItem: function(key) {
			return this[key];
		},
		setItem: function(key, value) {
			this[key] = value;
		},
		removeItem: function(key) {
			delete this[key];
		}
	};
});
commonModule.factory("localStorage", function(storage) {
	return window.localStorage || storage;
});
commonModule.factory("sessionStorage", function(storage) {
	return window.sessionStorage || storage;
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
