var orderModule = angular.module("order");

orderModule.factory("orderService", function($rootScope, sessionStorage, userService, ordersResource) {
	var EMPTY_ORDER_INFO = {
		inTransfer: {
			type: "IN", currency: "BITCOIN",
			amount: {coins: 0, cents: 0, centsInCoin: 100 * 1000 * 1000}
		},
		outTransfer: {
			type: "OUT", currency: "LITECOIN",
			amount: {coins: 0, cents: 0, centsInCoin: 100 * 1000 * 1000}
		}
	};
	
	var orderInfo;
	
	init();
	$rootScope.$on("login", function() {
		init();
	});
	$rootScope.$on("logout", function() {
		_reset();
	});
	
	function init() {
		if (!userService.get()) {
			return;
		}
		if (_restoreFromSession()) {
			return;
		}
		var activeOrder = ordersResource.getActive({}, function() {
			if (activeOrder && activeOrder.status) {
				_set(activeOrder);
			} else {
				empty();
			}
		}, function() {
			empty();
		});
	}
	
	function get() {
		return orderInfo;
	}
	function empty() {
		var newOrderInfo = angular.copy(EMPTY_ORDER_INFO);
		var user = userService.get();
		if (user.publicKey) {
			newOrderInfo.userPublicKey = user.publicKey;
		}
		if (orderInfo) {
			if (orderInfo.inTransfer && orderInfo.inTransfer.currency) {
				newOrderInfo.inTransfer.currency = orderInfo.inTransfer.currency;
			}
			if (orderInfo.outTransfer && orderInfo.outTransfer.currency) {
				newOrderInfo.outTransfer.currency = orderInfo.outTransfer.currency;
				if (orderInfo.outTransfer.address) {
					newOrderInfo.outTransfer.address = orderInfo.outTransfer.address;
				}
			}
		}
		_set(newOrderInfo);
	}
	function create() {
		if (!orderInfo) {
			return;
		}
		var newOrderInfo = ordersResource.create({}, orderInfo, function() {
			_set(newOrderInfo);
		}, function() {
			// do nothing
		});
		return newOrderInfo;
	}
	
	function actualize() {
		if (orderInfo && orderInfo.status && orderInfo.userPublicKey && orderInfo.creationDate) {
			var newOrderInfo = ordersResource.getByTimestamp({
				creationDate: orderInfo.creationDate
			}, function() {
				_set(newOrderInfo);
			}, function() {
				// do nothing
			});
		}
	}
	
	function storeInSession() {
		if (orderInfo) {
			sessionStorage.setItem("orderInfo", JSON.stringify(orderInfo));
		}
	}
	function _restoreFromSession() {
		var orderInfoJSON = sessionStorage.getItem("orderInfo");
		if (orderInfoJSON) {
			sessionStorage.removeItem("orderInfo");
			var newOrderInfo = JSON.parse(orderInfoJSON);
			if (userService.get()) {
				newOrderInfo.userPublicKey = userService.get().publicKey;
			}
			_set(newOrderInfo);
			return true;
		} else {
			return false;
		}
	}
	
	function _set(newOrderInfo) {
		if (newOrderInfo) {
			orderInfo = newOrderInfo;
			$rootScope.orderInfo = newOrderInfo;
			$rootScope.$broadcast("order-loaded", newOrderInfo);
		} else {
			_reset();
		}
	}
	function _reset() {
		var oldOrderInfo = orderInfo;
		orderInfo = null;
		$rootScope.orderInfo = null;
		$rootScope.$broadcast("order-nulled", oldOrderInfo);
	}
	
	return {
		get: get,
		empty: empty,
		create: create,
		storeInSession: storeInSession,
		actualize: actualize
	};
});