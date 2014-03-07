var moneyModule = angular.module("money");

moneyModule.factory("isAmountValid", function() {
	return function(amount) {
		return (amount && ((amount.coins > 0) || (amount.cents > 0)));
	};
});

moneyModule.factory("isTransferValid", function(isAmountValid) {
	return function(transfer) {
		return (transfer && transfer.currency && isAmountValid(transfer.amount));
	};
});