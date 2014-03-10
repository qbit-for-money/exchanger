var orderModule = angular.module("order");

orderModule.factory("resetOrderInfo", function($rootScope, userService) {
	var emptyOrderInfo = {
		inTransfer: {
			type: "IN", currency: "BITCOIN"
		},
		outTransfer: {
			type: "OUT", currency: "LITECOIN"
		}
	};
	return function() {
		var orderInfo = angular.copy(emptyOrderInfo);
		if ($rootScope.orderInfo) {
			var userPublicKey = userService.getPublicKey();
			if (userPublicKey) {
				orderInfo.userPublicKey = userPublicKey;
			}
			if ($rootScope.orderInfo.inTransfer && $rootScope.orderInfo.inTransfer.currency) {
				orderInfo.inTransfer.currency = $rootScope.orderInfo.inTransfer.currency;
			}
			if ($rootScope.orderInfo.outTransfer && $rootScope.orderInfo.outTransfer.currency) {
				orderInfo.outTransfer.currency = $rootScope.orderInfo.outTransfer.currency;
				if ($rootScope.orderInfo.outTransfer.address) {
					orderInfo.outTransfer.address = $rootScope.orderInfo.outTransfer.address;
				}
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
			sessionStorage.removeItem("orderInfo");
			$rootScope.orderInfo = JSON.parse(orderInfoJSON);
		}
	};
});

orderModule.run(function(resetOrderInfo) {
	resetOrderInfo();
});