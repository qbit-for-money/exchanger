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

commonModule.factory("sessionStorage", function() {
	return window.sessionStorage || {
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