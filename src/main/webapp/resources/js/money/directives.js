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
			input.priceFormat({
				prefix: "", centsLimit: 2, thousandsSeparator: ""
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
					input.closest(".form-group").removeClass("has-error");
				} else {
					input.closest(".form-group").addClass("has-error");
				}
			}
		}
	};
});
