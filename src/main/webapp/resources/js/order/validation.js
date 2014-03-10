var orderModule = angular.module("order");

orderModule.factory("isOrderValid", function(isTransferValid) {
	return function(orderInfo) {
		return (orderInfo && orderInfo.userPublicKey
				&& isTransferValid(orderInfo.inTransfer)
				&& isTransferValid(orderInfo.outTransfer)
				&& (orderInfo.inTransfer.type === "IN")
				&& (orderInfo.outTransfer.type === "OUT")
				&& (orderInfo.inTransfer.currency !== orderInfo.outTransfer.currency));
	};
});