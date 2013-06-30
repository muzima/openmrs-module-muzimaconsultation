angular.module("ui.bootstrap", ["ui.bootstrap.datepicker","ui.bootstrap.position","ui.bootstrap.typeahead"]);
angular.module('ui.bootstrap.datepicker', [])

.constant('datepickerConfig', {
  dayFormat: 'dd',
  monthFormat: 'MMMM',
  yearFormat: 'yyyy',
  dayHeaderFormat: 'EEE',
  dayTitleFormat: 'MMMM yyyy',
  monthTitleFormat: 'yyyy',
  showWeeks: true,
  startingDay: 0,
  yearRange: 20
})

.directive( 'datepicker', ['dateFilter', '$parse', 'datepickerConfig', function (dateFilter, $parse, datepickerConfig) {
  return {
    restrict: 'EA',
    replace: true,
    scope: {
      model: '=ngModel',
      dateDisabled: '&'
    },
    templateUrl: 'template/datepicker/datepicker.html',
    link: function(scope, element, attrs) {
      scope.mode = 'day'; // Initial mode

      // Configuration parameters
      var selected = new Date(), showWeeks, minDate, maxDate, format = {};
      format.day   = angular.isDefined(attrs.dayFormat) ? scope.$eval(attrs.dayFormat) : datepickerConfig.dayFormat;
      format.month = angular.isDefined(attrs.monthFormat) ? scope.$eval(attrs.monthFormat) : datepickerConfig.monthFormat;
      format.year  = angular.isDefined(attrs.yearFormat) ? scope.$eval(attrs.yearFormat) : datepickerConfig.yearFormat;
      format.dayHeader  = angular.isDefined(attrs.dayHeaderFormat) ? scope.$eval(attrs.dayHeaderFormat) : datepickerConfig.dayHeaderFormat;
      format.dayTitle   = angular.isDefined(attrs.dayTitleFormat) ? scope.$eval(attrs.dayTitleFormat) : datepickerConfig.dayTitleFormat;
      format.monthTitle = angular.isDefined(attrs.monthTitleFormat) ? scope.$eval(attrs.monthTitleFormat) : datepickerConfig.monthTitleFormat;
      var startingDay   = angular.isDefined(attrs.startingDay) ? scope.$eval(attrs.startingDay) : datepickerConfig.startingDay;
      var yearRange = angular.isDefined(attrs.yearRange) ? scope.$eval(attrs.yearRange) : datepickerConfig.yearRange;

      if (attrs.showWeeks) {
        scope.$parent.$watch($parse(attrs.showWeeks), function(value) {
          showWeeks = !! value;
          updateShowWeekNumbers();
        });
      } else {
        showWeeks = datepickerConfig.showWeeks;
        updateShowWeekNumbers();
      }

      if (attrs.min) {
        scope.$parent.$watch($parse(attrs.min), function(value) {
          minDate = new Date(value);
          refill();
        });
      }
      if (attrs.max) {
        scope.$parent.$watch($parse(attrs.max), function(value) {
          maxDate = new Date(value);
          refill();
        });
      }

      function updateCalendar (rows, labels, title) {
        scope.rows = rows;
        scope.labels = labels;
        scope.title = title;
      }

      // Define whether the week number are visible
      function updateShowWeekNumbers() {
        scope.showWeekNumbers = ( scope.mode === 'day' && showWeeks );
      }

      function compare( date1, date2 ) {
        if ( scope.mode === 'year') {
          return date2.getFullYear() - date1.getFullYear();
        } else if ( scope.mode === 'month' ) {
          return new Date( date2.getFullYear(), date2.getMonth() ) - new Date( date1.getFullYear(), date1.getMonth() );
        } else if ( scope.mode === 'day' ) {
          return (new Date( date2.getFullYear(), date2.getMonth(), date2.getDate() ) - new Date( date1.getFullYear(), date1.getMonth(), date1.getDate() ) );
        }
      }

      function isDisabled(date) {
        return ((minDate && compare(date, minDate) > 0) || (maxDate && compare(date, maxDate) < 0) || (scope.dateDisabled && scope.dateDisabled({ date: date, mode: scope.mode })));
      }

      // Split array into smaller arrays
      var split = function(a, size) {
        var arrays = [];
        while (a.length > 0) {
          arrays.push(a.splice(0, size));
        }
        return arrays;
      };
      var getDaysInMonth = function( year, month ) {
        return new Date(year, month + 1, 0).getDate();
      };

      var fill = {
        day: function() {
          var days = [], labels = [], lastDate = null;

          function addDays( dt, n, isCurrentMonth ) {
            for (var i =0; i < n; i ++) {
              days.push( {date: new Date(dt), isCurrent: isCurrentMonth, isSelected: isSelected(dt), label: dateFilter(dt, format.day), disabled: isDisabled(dt) } );
              dt.setDate( dt.getDate() + 1 );
            }
            lastDate = dt;
          }

          var d = new Date(selected);
          d.setDate(1);

          var difference = startingDay - d.getDay();
          var numDisplayedFromPreviousMonth = (difference > 0) ? 7 - difference : - difference;

          if ( numDisplayedFromPreviousMonth > 0 ) {
            d.setDate( - numDisplayedFromPreviousMonth + 1 );
            addDays(d, numDisplayedFromPreviousMonth, false);
          }
          addDays(lastDate || d, getDaysInMonth(selected.getFullYear(), selected.getMonth()), true);
          addDays(lastDate, (7 - days.length % 7) % 7, false);

          // Day labels
          for (i = 0; i < 7; i++) {
            labels.push(  dateFilter(days[i].date, format.dayHeader) );
          }
          updateCalendar( split( days, 7 ), labels, dateFilter(selected, format.dayTitle) );
        },
        month: function() {
          var months = [], i = 0, year = selected.getFullYear();
          while ( i < 12 ) {
            var dt = new Date(year, i++, 1);
            months.push( {date: dt, isCurrent: true, isSelected: isSelected(dt), label: dateFilter(dt, format.month), disabled: isDisabled(dt)} );
          }
          updateCalendar( split( months, 3 ), [], dateFilter(selected, format.monthTitle) );
        },
        year: function() {
          var years = [], year = parseInt((selected.getFullYear() - 1) / yearRange, 10) * yearRange + 1;
          for ( var i = 0; i < yearRange; i++ ) {
            var dt = new Date(year + i, 0, 1);
            years.push( {date: dt, isCurrent: true, isSelected: isSelected(dt), label: dateFilter(dt, format.year), disabled: isDisabled(dt)} );
          }
          var title = years[0].label + ' - ' + years[years.length - 1].label;
          updateCalendar( split( years, 5 ), [], title );
        }
      };
      var refill = function() {
        fill[scope.mode]();
      };
      var isSelected = function( dt ) {
        if ( scope.model && scope.model.getFullYear() === dt.getFullYear() ) {
          if ( scope.mode === 'year' ) {
            return true;
          }
          if ( scope.model.getMonth() === dt.getMonth() ) {
            return ( scope.mode === 'month' || (scope.mode === 'day' && scope.model.getDate() === dt.getDate()) );
          }
        }
        return false;
      };

      scope.$watch('model', function ( dt, olddt ) {
        if ( angular.isDate(dt) ) {
          selected = angular.copy(dt);
        }

        if ( ! angular.equals(dt, olddt) ) {
          refill();
        }
      });
      scope.$watch('mode', function() {
        updateShowWeekNumbers();
        refill();
      });

      scope.select = function( dt ) {
        selected = new Date(dt);

        if ( scope.mode === 'year' ) {
          scope.mode = 'month';
          selected.setFullYear( dt.getFullYear() );
        } else if ( scope.mode === 'month' ) {
          scope.mode = 'day';
          selected.setMonth( dt.getMonth() );
        } else if ( scope.mode === 'day' ) {
          scope.model = new Date(selected);
        }
      };
      scope.move = function(step) {
        if (scope.mode === 'day') {
          selected.setMonth( selected.getMonth() + step );
        } else if (scope.mode === 'month') {
          selected.setFullYear( selected.getFullYear() + step );
        } else if (scope.mode === 'year') {
          selected.setFullYear( selected.getFullYear() + step * yearRange );
        }
        refill();
      };
      scope.toggleMode = function() {
        scope.mode = ( scope.mode === 'day' ) ? 'month' : ( scope.mode === 'month' ) ? 'year' : 'day';
      };
      scope.getWeekNumber = function(row) {
        if ( scope.mode !== 'day' || ! scope.showWeekNumbers || row.length !== 7 ) {
          return;
        }

        var index = ( startingDay > 4 ) ? 11 - startingDay : 4 - startingDay; // Thursday
        var d = new Date( row[ index ].date );
        d.setHours(0, 0, 0);
        return Math.ceil((((d - new Date(d.getFullYear(), 0, 1)) / 86400000) + 1) / 7); // 86400000 = 1000*60*60*24;
      };
    }
  };
}]);
angular.module('ui.bootstrap.position', [])

/**
 * A set of utility methods that can be use to retrieve position of DOM elements.
 * It is meant to be used where we need to absolute-position DOM elements in
 * relation to other, existing elements (this is the case for tooltips, popovers,
 * typeahead suggestions etc.).
 */
  .factory('$position', ['$document', '$window', function ($document, $window) {

    var mouseX, mouseY;

    $document.bind('mousemove', function mouseMoved(event) {
      mouseX = event.pageX;
      mouseY = event.pageY;
    });

    function getStyle(el, cssprop) {
      if (el.currentStyle) { //IE
        return el.currentStyle[cssprop];
      } else if ($window.getComputedStyle) {
        return $window.getComputedStyle(el)[cssprop];
      }
      // finally try and get inline style
      return el.style[cssprop];
    }

    /**
     * Checks if a given element is statically positioned
     * @param element - raw DOM element
     */
    function isStaticPositioned(element) {
      return (getStyle(element, "position") || 'static' ) === 'static';
    }

    /**
     * returns the closest, non-statically positioned parentOffset of a given element
     * @param element
     */
    var parentOffsetEl = function (element) {
      var docDomEl = $document[0];
      var offsetParent = element.offsetParent || docDomEl;
      while (offsetParent && offsetParent !== docDomEl && isStaticPositioned(offsetParent) ) {
        offsetParent = offsetParent.offsetParent;
      }
      return offsetParent || docDomEl;
    };

    return {
      /**
       * Provides read-only equivalent of jQuery's position function:
       * http://api.jquery.com/position/
       */
      position: function (element) {
        var elBCR = this.offset(element);
        var offsetParentBCR = { top: 0, left: 0 };
        var offsetParentEl = parentOffsetEl(element[0]);
        if (offsetParentEl != $document[0]) {
          offsetParentBCR = this.offset(angular.element(offsetParentEl));
          offsetParentBCR.top += offsetParentEl.clientTop;
          offsetParentBCR.left += offsetParentEl.clientLeft;
        }

        return {
          width: element.prop('offsetWidth'),
          height: element.prop('offsetHeight'),
          top: elBCR.top - offsetParentBCR.top,
          left: elBCR.left - offsetParentBCR.left
        };
      },

      /**
       * Provides read-only equivalent of jQuery's offset function:
       * http://api.jquery.com/offset/
       */
      offset: function (element) {
        var boundingClientRect = element[0].getBoundingClientRect();
        return {
          width: element.prop('offsetWidth'),
          height: element.prop('offsetHeight'),
          top: boundingClientRect.top + ($window.pageYOffset || $document[0].body.scrollTop),
          left: boundingClientRect.left + ($window.pageXOffset || $document[0].body.scrollLeft)
        };
      },

      /**
       * Provides the coordinates of the mouse
       */
      mouse: function () {
        return {x: mouseX, y: mouseY};
      }
    };
  }]);

angular.module('ui.bootstrap.typeahead', ['ui.bootstrap.position'])

/**
 * A helper service that can parse typeahead's syntax (string provided by users)
 * Extracted to a separate service for ease of unit testing
 */
  .factory('typeaheadParser', ['$parse', function ($parse) {

  //                      00000111000000000000022200000000000000003333333333333330000000000044000
  var TYPEAHEAD_REGEXP = /^\s*(.*?)(?:\s+as\s+(.*?))?\s+for\s+(?:([\$\w][\$\w\d]*))\s+in\s+(.*)$/;

  return {
    parse:function (input) {

      var match = input.match(TYPEAHEAD_REGEXP), modelMapper, viewMapper, source;
      if (!match) {
        throw new Error(
          "Expected typeahead specification in form of '_modelValue_ (as _label_)? for _item_ in _collection_'" +
            " but got '" + input + "'.");
      }

      return {
        itemName:match[3],
        source:$parse(match[4]),
        viewMapper:$parse(match[2] || match[1]),
        modelMapper:$parse(match[1])
      };
    }
  };
}])

  .directive('typeahead', ['$compile', '$parse', '$q', '$timeout', '$document', '$position', 'typeaheadParser', function ($compile, $parse, $q, $timeout, $document, $position, typeaheadParser) {

  var HOT_KEYS = [9, 13, 27, 38, 40];

  return {
    require:'ngModel',
    link:function (originalScope, element, attrs, modelCtrl) {

      var selected;

      //minimal no of characters that needs to be entered before typeahead kicks-in
      var minSearch = originalScope.$eval(attrs.typeaheadMinLength) || 1;

      //minimal wait time after last character typed before typehead kicks-in
      var waitTime = originalScope.$eval(attrs.typeaheadWaitMs) || 0;

      //expressions used by typeahead
      var parserResult = typeaheadParser.parse(attrs.typeahead);

      //should it restrict model values to the ones selected from the popup only?
      var isEditable = originalScope.$eval(attrs.typeaheadEditable) !== false;

      var isLoadingSetter = $parse(attrs.typeaheadLoading).assign || angular.noop;

      var onSelectCallback = $parse(attrs.typeaheadOnSelect);

      //pop-up element used to display matches
      var popUpEl = angular.element('<typeahead-popup></typeahead-popup>');
      popUpEl.attr({
        matches: 'matches',
        active: 'activeIdx',
        select: 'select(activeIdx)',
        query: 'query',
        position: 'position'
      });

      //create a child scope for the typeahead directive so we are not polluting original scope
      //with typeahead-specific data (matches, query etc.)
      var scope = originalScope.$new();
      originalScope.$on('$destroy', function(){
        scope.$destroy();
      });

      var resetMatches = function() {
        scope.matches = [];
        scope.activeIdx = -1;
      };

      var getMatchesAsync = function(inputValue) {

        var locals = {$viewValue: inputValue};
        isLoadingSetter(originalScope, true);
        $q.when(parserResult.source(scope, locals)).then(function(matches) {

          //it might happen that several async queries were in progress if a user were typing fast
          //but we are interested only in responses that correspond to the current view value
          if (inputValue === modelCtrl.$viewValue) {
            if (matches.length > 0) {

              scope.activeIdx = 0;
              scope.matches.length = 0;

              //transform labels
              for(var i=0; i<matches.length; i++) {
                locals[parserResult.itemName] = matches[i];
                scope.matches.push({
                  label: parserResult.viewMapper(scope, locals),
                  model: matches[i]
                });
              }

              scope.query = inputValue;
              //position pop-up with matches - we need to re-calculate its position each time we are opening a window
              //with matches as a pop-up might be absolute-positioned and position of an input might have changed on a page
              //due to other elements being rendered
              scope.position = $position.position(element);
              scope.position.top = scope.position.top + element.prop('offsetHeight');

            } else {
              resetMatches();
            }
            isLoadingSetter(originalScope, false);
          }
        }, function(){
          resetMatches();
          isLoadingSetter(originalScope, false);
        });
      };

      resetMatches();

      //we need to propagate user's query so we can higlight matches
      scope.query = undefined;

      //plug into $parsers pipeline to open a typeahead on view changes initiated from DOM
      //$parsers kick-in on all the changes coming from the view as well as manually triggered by $setViewValue
      modelCtrl.$parsers.push(function (inputValue) {

        var timeoutId;

        resetMatches();
        if (selected) {
          return inputValue;
        } else {
          if (inputValue && inputValue.length >= minSearch) {
            if (waitTime > 0) {
              if (timeoutId) {
                $timeout.cancel(timeoutId);//cancel previous timeout
              }
              timeoutId = $timeout(function () {
                getMatchesAsync(inputValue);
              }, waitTime);
            } else {
              getMatchesAsync(inputValue);
            }
          }
        }

        return isEditable ? inputValue : undefined;
      });

      modelCtrl.$render = function () {
        var locals = {};
        locals[parserResult.itemName] = selected || modelCtrl.$viewValue;
        element.val(parserResult.viewMapper(scope, locals) || modelCtrl.$viewValue);
        selected = undefined;
      };

      scope.select = function (activeIdx) {
        //called from within the $digest() cycle
        var locals = {};
        var model, item;
        locals[parserResult.itemName] = item = selected = scope.matches[activeIdx].model;

        model = parserResult.modelMapper(scope, locals);
        modelCtrl.$setViewValue(model);
        modelCtrl.$render();
        onSelectCallback(scope, {
          $item: item,
          $model: model,
          $label: parserResult.viewMapper(scope, locals)
        });

        element[0].focus();
      };

      //bind keyboard events: arrows up(38) / down(40), enter(13) and tab(9), esc(27)
      element.bind('keydown', function (evt) {

        //typeahead is open and an "interesting" key was pressed
        if (scope.matches.length === 0 || HOT_KEYS.indexOf(evt.which) === -1) {
          return;
        }

        evt.preventDefault();

        if (evt.which === 40) {
          scope.activeIdx = (scope.activeIdx + 1) % scope.matches.length;
          scope.$digest();

        } else if (evt.which === 38) {
          scope.activeIdx = (scope.activeIdx ? scope.activeIdx : scope.matches.length) - 1;
          scope.$digest();

        } else if (evt.which === 13 || evt.which === 9) {
          scope.$apply(function () {
            scope.select(scope.activeIdx);
          });

        } else if (evt.which === 27) {
          evt.stopPropagation();

          resetMatches();
          scope.$digest();
        }
      });

      $document.bind('click', function(){
        resetMatches();
        scope.$digest();
      });

      element.after($compile(popUpEl)(scope));
    }
  };

}])

  .directive('typeaheadPopup', function () {
    return {
      restrict:'E',
      scope:{
        matches:'=',
        query:'=',
        active:'=',
        position:'=',
        select:'&'
      },
      replace:true,
      templateUrl:'template/typeahead/typeahead.html',
      link:function (scope, element, attrs) {

        scope.isOpen = function () {
          return scope.matches.length > 0;
        };

        scope.isActive = function (matchIdx) {
          return scope.active == matchIdx;
        };

        scope.selectActive = function (matchIdx) {
          scope.active = matchIdx;
        };

        scope.selectMatch = function (activeIdx) {
          scope.select({activeIdx:activeIdx});
        };
      }
    };
  })

  .filter('typeaheadHighlight', function() {

    function escapeRegexp(queryToEscape) {
      return queryToEscape.replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
    }

    return function(matchItem, query) {
      return query ? matchItem.replace(new RegExp(escapeRegexp(query), 'gi'), '<strong>$&</strong>') : query;
    };
  });
