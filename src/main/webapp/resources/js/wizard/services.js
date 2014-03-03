var wizardModule = angular.module("wizard");

wizardModule.factory("wizardService", function() {
	var steps = [{
			title: "Currency", path: "/steps/currency"
		}, {
			title: "Amount", path: "/steps/amount",
			validator: function(orderInfo) {
				return true;
			}
		}, {
			title: "Result", path: "/steps/result"
		}];
	function getSteps() {
		return steps;
	}
	function getStepIndexByPath(path) {
		for (var i = 0; i < steps.length; i++) {
			if (steps[i].path === path) {
				return i;
			}
		}
	}
	return {
		getSteps: getSteps,
		getStepIndexByPath: getStepIndexByPath
	};
});