var moneyModule = angular.module("money");

moneyModule.factory("amountToNumber", function() {
	return function(amount) {
		if (amount) {
			return (amount.coins + (amount.cents / amount.centsInCoin));
		}
	};
});
moneyModule.factory("rateToNumber", function(amountToNumber) {
	return function(rate) {
		if (rate) {
			return (amountToNumber(rate.denominator) / amountToNumber(rate.numerator));
		}
	};
});
moneyModule.factory("convertAmount", function() {
	return function(amount, rate) {
		if (!amount || !rate) {
			return amount;
		}
		
	};
});