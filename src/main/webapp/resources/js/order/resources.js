var orderModule = angular.module("order");

orderModule.factory("currencyResource", function($resource) {
	return $resource(window.context + "webapi/currency/:id", {}, {
		findAll: {method: "GET"}
	});
});

orderModule.factory("ordersResource", function($resource) {
	return $resource(window.context + "webapi/orders/", {}, {
		getActive: {method: "GET", url: window.context + "webapi/orders/active"},
		getByTimestamp: {method: "GET"},
		create: {method: "PUT"}
	});
});

orderModule.factory("cancellationsResource", function($resource) {
	return $resource(window.context + "webapi/cancellations/", {}, {
		sendCancellationToken: {method: "PUT"},
		cancel: {method: "POST"}
	});
});