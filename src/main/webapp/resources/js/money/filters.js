var moneyModule = angular.module("money");

moneyModule.factory("formatAmount", function(isAmountValid, amountToNumber) {
	return function(amount) {
		if (isAmountValid(amount)) {
			return amountToNumber(amount).toFixed(2);
		} else {
			return "";
		}
	};
});

moneyModule.filter("amount", function(formatAmount) {
	return formatAmount;
});
moneyModule.filter("rate", function(rateToNumber) {
	return function(rate) {
		if (!rate) {
			return "";
		}
		var rateNum = rateToNumber(rate);
		var fractionDigits;
		if (rateNum < 1) {
			fractionDigits = Math.round(Math.log(Math.max(
					rate.numerator.centsInCoin, rate.denominator.centsInCoin)) / Math.LN10);
		} else {
			fractionDigits = 2;
		}
		return rateNum.toFixed(fractionDigits);
	};
});
