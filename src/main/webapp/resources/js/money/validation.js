var moneyModule = angular.module("money");

moneyModule.factory("isAmountValid", function() {
	return function(amount) {
		return (amount && angular.isNumber(amount.coins) && angular.isNumber(amount.cents)
				&& (amount.coins >= 0) && (amount.cents >= 0)
				&& angular.isNumber(amount.centsInCoin) && (amount.centsInCoin > 0));
	};
});
moneyModule.factory("isAmountPositive", function(isAmountValid) {
	return function(amount) {
		var minCents = Math.floor(amount.centsInCoin / 100);
		return (isAmountValid(amount) && ((amount.coins > 0) || (amount.cents >= minCents)));
	};
});

moneyModule.factory("isRateValid", function(isAmountValid) {
	return function(rate) {
		return (rate && isAmountValid(rate.numerator) && isAmountValid(rate.denominator));
	};
});

moneyModule.factory("isTransferValid", function(isAmountPositive) {
	return function(transfer) {
		return (transfer && transfer.currency && transfer.address
				&& isAmountPositive(transfer.amount));
	};
});