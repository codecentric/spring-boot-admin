'use strict';

var humanBytes = require('../../../../modules/applications-details/filters/humanBytes.js')();

describe('Filter: humanBytes', function () {
    it('test byte thresholds', function () {
        expect(humanBytes(0)).toEqual('0B');
        expect(humanBytes(2.5 * 1024)).toEqual('2.5K');
        expect(humanBytes(1023)).toEqual('1023B');
        expect(humanBytes(0.5, 'K')).toEqual('512B');
    });

    it('test kbyte thresholds', function () {
        expect(humanBytes(0, 'K')).toEqual('0B');
        expect(humanBytes(2.5 * 1024, 'K')).toEqual('2.5M');
        expect(humanBytes(1023, 'K')).toEqual('1023K');
        expect(humanBytes(0.5, 'M')).toEqual('512K');
    });

    it('test mbyte thresholds', function () {
        expect(humanBytes(0, 'M')).toEqual('0B');
        expect(humanBytes(1024, 'M')).toEqual('1G');
        expect(humanBytes(1023, 'M')).toEqual('1023M');
        expect(humanBytes(0.5, 'G')).toEqual('512M');
    });
});
