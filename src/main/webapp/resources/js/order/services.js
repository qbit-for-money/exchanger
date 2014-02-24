var orderModule = angular.module("order");

orderModule.factory("resetOrderInfo", function($rootScope) {
	var emptyOrderInfo = {
		inTransfer: { type: "IN" },
		outTransfer: { type: "OUT" }
	};
	return function() {
		$rootScope.orderInfo = angular.copy(emptyOrderInfo);
	};
});

orderModule.run(function(resetOrderInfo) {
	resetOrderInfo();
});