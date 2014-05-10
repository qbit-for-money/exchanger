var moneyModule = angular.module("money");

moneyModule.directive("amountInput", function() {
	return {
		restrict: "E",
		scope: {amount: "=", readonly: "=", type: "="},
		templateUrl: "resources/html/money/amount-input.html",
		controller: function($scope, formatAmount, stringToAmount, isAmountPositive) {
			$scope.formatAmount = formatAmount;
			$scope.stringToAmount = stringToAmount;
			$scope.isAmountPositive = isAmountPositive;
		},
		link: function(scope, element) {
			var input = element.find("input");
			var centsInCoin = scope.amount.centsInCoin.toString();
			// centsInCoin.substring(1, centsInCoin.length) WAT?
			input.mask("099999999999999999999999999999999." + centsInCoin.substring(1, centsInCoin.length),
					{reverse: false, maxlength: false});

			function updateAmount() {
				scope.amount = scope.stringToAmount(input.val(), scope.amount.centsInCoin);
				validate();
			}
			input.bind("change keyup paste", function() {
				scope.$apply(updateAmount);
			});

			function updateInput() {
				var amount = scope.amount;	
				if (amount.coins === 0 && amount.cents === 0) {
					input.val("");
				} else if(amount.cents === 0) {
					input.val(amount.coins);  
				} else {
					var cents = (amount.cents / amount.centsInCoin).toString();
					var fraction = cents.substring(2, cents.length);
					input.val(amount.coins + "." + fraction); 
				}
				validate();
			}
			scope.$watch("amount", updateInput, true);

			function validate() {
				if (scope.isAmountPositive(scope.amount)) {
					input.parent().removeClass("has-error");
				} else {
					input.parent().addClass("has-error");
				}
			}
		}
	};
});
