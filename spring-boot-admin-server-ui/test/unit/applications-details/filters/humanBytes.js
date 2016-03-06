'use strict';

var chai = require('chai')
  , expect = chai.expect;

var humanBytes = require('../../../../modules/applications-details/filters/humanBytes')();

describe('Filter: humanBytes', function() {
    it('test byte thresholds', function() {
        expect(humanBytes(0)).to.equal('0B');
        expect(humanBytes(2.5 * 1024)).to.equal('2.5K');
        expect(humanBytes(1023)).to.equal('1023B');
        expect(humanBytes(0.5, 'K')).to.equal('512B');
    });

    it('test kbyte thresholds', function() {
        expect(humanBytes(0, 'K')).to.equal('0B');
        expect(humanBytes(2.5 * 1024, 'K')).to.equal('2.5M');
        expect(humanBytes(1023, 'K')).to.equal('1023K');
        expect(humanBytes(0.5, 'M')).to.equal('512K');
    });

    it('test mbyte thresholds', function() {
        expect(humanBytes(0, 'M')).to.equal('0B');
        expect(humanBytes(1024, 'M')).to.equal('1G');
        expect(humanBytes(1023, 'M')).to.equal('1023M');
        expect(humanBytes(0.5, 'G')).to.equal('512M');
    });
});
