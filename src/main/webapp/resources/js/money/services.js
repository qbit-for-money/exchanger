var moneyModule = angular.module("money");

moneyModule.factory("moneyCustomModules", function() {
	var customModules = {};
	function get(currency) {
		return customModules[currency];
	}
	function has(currency) {
		return (currency in customModules);
	}
	function put(currency, module) {
		if (currency && module) {
			customModules[currency] = module;
		}
	}
	return {get: get, has: has, put: put};
});

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
		if (!angular.isNumber(centsInCoin) || Number.isNaN(centsInCoin) || (centsInCoin <= 0)) {
			return;
		}
		var result = {coins: 0, cents: 0, centsInCoin: centsInCoin};
		if (angular.isNumber(num) && !Number.isNaN(num)) {
			result.coins = Math.floor(num);
			result.cents = Math.round((num - Math.floor(num)) * centsInCoin);
		}
		return result;
	};
});
moneyModule.factory("stringToAmount", function(numberToAmount) {
	return function(amountText, centsInCoin) {
		if (!angular.isNumber(centsInCoin) || Number.isNaN(centsInCoin) || (centsInCoin <= 0)) {
			return;
		}
		var result = {coins: 0, cents: 0, centsInCoin: centsInCoin};
		if (amountText) {
			try {
				result = numberToAmount(parseFloat(amountText), centsInCoin);
			} catch (ex) {
				// do nothing
			}
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