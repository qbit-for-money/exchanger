var moneyModule = angular.module("money");

moneyModule.filter("amount", function(amountToNumber) {
	return function(amount) {
		if (!amount) {
			return "";
		}
		var fractionDigits = Math.round(Math.log(amount.centsInCoin) / Math.LN10);
		return amountToNumber(amount).toFixed(fractionDigits);
	};
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
