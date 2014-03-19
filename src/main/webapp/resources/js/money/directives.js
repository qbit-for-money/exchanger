var moneyModule = angular.module("money");

moneyModule.directive("amountInput", function() {
	return {
		restrict: "E",
		scope: {amount: "=", readonly: "="},
		templateUrl: "resources/html/money/amount-input.html",
		controller: function($scope, formatAmount, stringToAmount, isAmountPositive) {
			$scope.formatAmount = formatAmount;
			$scope.stringToAmount = stringToAmount;
			$scope.isAmountPositive = isAmountPositive;
		},
		link: function(scope, element) {
			var input = element.find("input");
			
			var fractionDigits;
			if (scope.amount && scope.amount.centsInCoin) {
				fractionDigits = Math.round(Math.log(scope.amount.centsInCoin) / Math.LN10);
			} else {
				fractionDigits = 2;
			}
			input.priceFormat({
				prefix: "", centsLimit: fractionDigits, thousandsSeparator: ""
			});

			function updateAmount() {
				scope.amount = scope.stringToAmount(input.val(), scope.amount.centsInCoin);
				validate();
			}
			input.bind("change keyup paste", function() {
				scope.$apply(updateAmount);
			});

			function updateInput() {
				input.val(scope.formatAmount(scope.amount));
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
