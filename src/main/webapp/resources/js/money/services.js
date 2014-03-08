var moneyModule = angular.module("money");

moneyModule.factory("amountToNumber", function(isAmountValid) {
	return function(amount) {
		if (isAmountValid(amount)) {
			return (amount.coins + (amount.cents / amount.centsInCoin));
		} else {
			return Number.NaN;
		}
	};
});
moneyModule.factory("numberToAmount", function() {
	return function(num, centsInCoin) {
		if (!angular.isNumber(centsInCoin) || (centsInCoin <= 0)) {
			return;
		}
		var result = {coins: 0, cents: 0, centsInCoin: centsInCoin};
		if (angular.isNumber(num)) {
			result.coins = Math.floor(num);
			result.cents = Math.round((num - Math.floor(num)) * centsInCoin);
		}
		return result;
	};
});

moneyModule.factory("rateToNumber", function(isRateValid, amountToNumber) {
	return function(rate) {
		if (isRateValid(rate)) {
			return (amountToNumber(rate.numerator) / amountToNumber(rate.denominator));
		} else {
			return Number.NaN;
		}
	};
});
moneyModule.factory("invRate", function(isRateValid) {
	return function(rate) {
		if (isRateValid(rate)) {
			return {numerator: rate.denominator, denominator: rate.numerator};
		}
	};
});

moneyModule.factory("convertAmount", function(isAmountValid, isRateValid, amountToNumber, numberToAmount) {
	return function(amount, rate) {
		if (isAmountValid(amount) && isRateValid(rate)) {
			var resultNum = amountToNumber(amount) * amountToNumber(
					rate.numerator) / amountToNumber(rate.denominator);
			return numberToAmount(resultNum, rate.numerator.centsInCoin);
		}
	};
});