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
		return (isAmountValid(amount) && ((amount.coins > 0) || (amount.cents > 0)));
	};
});

moneyModule.factory("isRateValid", function(isAmountPositive) {
	return function(rate) {
		return (rate && isAmountPositive(rate.numerator) && isAmountPositive(rate.denominator));
	};
});

moneyModule.factory("isTransferValid", function(isAmountPositive) {
	return function(transfer) {
		return (transfer && transfer.currency && transfer.address
				&& isAmountPositive(transfer.amount));
	};
});