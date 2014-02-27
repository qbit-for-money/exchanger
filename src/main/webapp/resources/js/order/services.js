var orderModule = angular.module("order");

orderModule.factory("resetOrderInfo", function($rootScope) {
	var emptyOrderInfo = {
		inTransfer: {type: "IN"},
		outTransfer: {type: "OUT"}
	};
	return function() {
		var orderInfo = angular.copy(emptyOrderInfo);
		if ($rootScope.orderInfo) {
			if ($rootScope.orderInfo.inTransfer && $rootScope.orderInfo.inTransfer.currency) {
				orderInfo.inTransfer.currency = $rootScope.orderInfo.inTransfer.currency;
			}
			if ($rootScope.orderInfo.outTransfer && $rootScope.orderInfo.outTransfer.currency) {
				orderInfo.outTransfer.currency = $rootScope.orderInfo.outTransfer.currency;
			}
		}
		$rootScope.orderInfo = orderInfo;
	};
});

orderModule.factory("storeOrderInfoInSession", function($rootScope, sessionStorage) {
	return function() {
		sessionStorage.setItem("orderInfo", JSON.stringify($rootScope.orderInfo));
	};
});
orderModule.factory("restoreOrderInfoFromSession", function($rootScope, sessionStorage) {
	return function() {
		var orderInfoJSON = sessionStorage.getItem("orderInfo");
		if (orderInfoJSON) {
			$rootScope.orderInfo = JSON.parse(orderInfoJSON);
		}
	};
});

orderModule.run(function(resetOrderInfo) {
	resetOrderInfo();
});