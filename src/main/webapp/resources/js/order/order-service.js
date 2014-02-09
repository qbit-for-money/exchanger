var orderModule = angular.module("order");

orderModule.factory("currencyResource", function($resource) {
	return $resource(window.context + "webapi/currency/:id", {}, {
		findAll: {method: "GET"}
	});
});

orderModule.factory("ordersResource", function($resource) {
	return $resource(window.context + "webapi/orders/", {}, {
			getActiveOrder: {url: window.context + "webapi/orders/active", method: "GET"},
			create: {method: "POST"}
		});
});