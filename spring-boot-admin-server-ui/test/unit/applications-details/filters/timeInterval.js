'use strict';

var chai = require('chai')
  , expect = chai.expect;

var timeInterval = require('../../../../modules/applications-details/filters/timeInterval')();

describe('Filter: timeInterval', function() {
    it('test timeInterval', function() {
        expect(timeInterval(0)).to.equal('00:00:00:00');
        expect(timeInterval(113556270)).to.equal('01:07:32:36');
    });
});
