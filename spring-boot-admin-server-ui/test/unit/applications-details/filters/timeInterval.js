'use strict';

var timeInterval = require('../../../../modules/applications-details/filters/timeInterval.js')();

describe('Filter: timeInterval', function () {
    it('test timeInterval', function () {
        expect(timeInterval(0)).toEqual('00:00:00:00');
        expect(timeInterval(113556270)).toEqual('01:07:32:36');
    });
});
