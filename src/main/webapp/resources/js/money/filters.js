var moneyModule = angular.module("money");

moneyModule.factory("formatAmount", function(isAmountValid, amountToNumber) {
	return function(amount) {
		if (isAmountValid(amount)) {
			var fractionDigits = Math.round(Math.log(amount.centsInCoin) / Math.LN10);
			return amountToNumber(amount).toFixed(fractionDigits);
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
		var fractionDigits = Math.round(Math.log(Math.max(rate.numerator.centsInCoin,
				rate.denominator.centsInCoin)) / Math.LN10);
		if ((rateNum <= 0.0) || (rateNum > 0.5)) {
			return ("1 / " + rateNum.toFixed(fractionDigits));
		} else {
			return ((1.0 / rateNum).toFixed(fractionDigits) + " / 1");
		}
	};
});
