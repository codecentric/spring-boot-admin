'use strict';

var yaml = require('../../../../modules/applications/filters/yaml.js')();

describe('Filter: yaml', function () {
    it('test yaml', function () {
        expect(yaml({
            id: 1,
            obj: {
                name: 'foo'
            }
        })).toEqual('id: 1\nobj:\n  name: foo\n');
    });
});
