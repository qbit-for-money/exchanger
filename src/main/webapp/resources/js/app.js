angular.module("common", ["ngResource"]);

angular.module("order", ["ngResource"]);
angular.module("money", ["ngResource", "order", "common"], function($locationProvider) {
      $locationProvider.html5Mode(true);
    });
angular.module("main-menu", ["ngRoute", "angular-carousel", "ngResource", "common", "order", "money"]);

angular.module("main", ["common", "ngRoute", "ui.bootstrap", "main-menu", "order", "money"]);
