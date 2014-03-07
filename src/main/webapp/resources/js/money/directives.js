var moneyModule = angular.module("money");

moneyModule.directive("amountInput", function() {
	return {
		restrict: "E",
		scope: { form: "=", amount: "=" },
		templateUrl: "resources/html/money/amount-input.html",
		controller: function($scope, formatAmount) {
			$scope.formatAmount = function() {
				return formatAmount($scope.amount);
			};
		},
		link: function(scope, element) {
			var input = element.find("input");
			
			var centsLimit;
			if (scope.amount && scope.amount.centsInCoin) {
				centsLimit = Math.min(3, Math.round(Math.log(scope.amount.centsInCoin) / Math.LN10));
			} else {
				centsLimit = 2;
			}
			input.priceFormat({
				prefix: "", centsLimit: centsLimit, thousandsSeparator: ""
			});
			
			function updateAmount() {
				var amountText = input.val();
				if (amountText) {
					var amountParts = amountText.split(".");
					var coins = parseInt(amountParts[0]);
					scope.amount.coins = coins;
					if (amountParts.length > 1) {
						var centsNumertaor = parseInt(amountParts[1]);
						var centsDenumerator = Math.pow(10, amountParts[1].length);
						scope.amount.cents = Math.round(centsNumertaor * scope.amount.centsInCoin / centsDenumerator);
					} else {
						scope.amount.cents = 0;
					}
				} else {
					scope.amount.coins = 0;
					scope.amount.cents = 0;
				}
			}
			input.bind("change keyup paste", function() {
				scope.$apply(updateAmount);
			});
			
			function updateInput() {
				input.val(scope.formatAmount());
			}
			scope.$watch("amount", updateInput);
			updateInput();
		}
	};
});
