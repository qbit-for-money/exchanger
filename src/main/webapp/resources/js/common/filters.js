var commonModule = angular.module("common");

commonModule.filter("delaySeconds", function() {
	return function(totalSeconds) {
		var result = "";
		var minutes = Math.floor(totalSeconds / 60);
		result += minutes;
		result += ":";
		var seconds = totalSeconds % 60;
		if (seconds < 10) {
			result += "0";
		}
		result += seconds;
		return result;
	};
});