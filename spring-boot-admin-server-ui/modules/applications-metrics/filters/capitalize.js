'use strict';

module.exports = function () {
    return function (input) {
        return (input !== null) ? input.substring(0, 1).toUpperCase() + input.substring(1) : null;
    }
};