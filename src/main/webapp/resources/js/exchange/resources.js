var exchangeModule = angular.module("exchange");

exchangeModule.factory("exchangesResource", function($resource) {
	return $resource(window.context + "webapi/exchanges/", {}, {
		rate: {method: "GET", url: window.context + "webapi/exchanges/rate"}
	});
});