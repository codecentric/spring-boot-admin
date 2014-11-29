'use strict';

var chai = require('chai')
  , expect = chai.expect
  , sinon = require('sinon')
  , sinonChai = require('sinon-chai');

var humanBytesFilter = require('../../../app/js/filter/humanBytes')();

describe('Filter: humanBytes', function() {

    it('test byte thresholds', function() {
        expect(humanBytesFilter(0)).to.equal('0B');
        expect(humanBytesFilter(2.5 * 1024)).to.equal('2.5K');
        expect(humanBytesFilter(1023)).to.equal('1023B');
        expect(humanBytesFilter(0.5, 'K')).to.equal('512B');
    });

    it('test kbyte thresholds', function() {
        expect(humanBytesFilter(0, 'K')).to.equal('0B');
        expect(humanBytesFilter(2.5 * 1024, 'K')).to.equal('2.5M');
        expect(humanBytesFilter(1023, 'K')).to.equal('1023K');
        expect(humanBytesFilter(0.5, 'M')).to.equal('512K');
    });

    it('test mbyte thresholds', function() {
        expect(humanBytesFilter(0, 'M')).to.equal('0B');
        expect(humanBytesFilter(1024, 'M')).to.equal('1G');
        expect(humanBytesFilter(1023, 'M')).to.equal('1023M');
        expect(humanBytesFilter(0.5, 'G')).to.equal('512M');
    });

});
