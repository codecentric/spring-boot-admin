/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

module.exports = function () {
    return function (input, active) {
        if (!active) {
            return input;
        }
        var result = [];
        for (var j in input) {
            var name = input[j].name;
            var i = name.lastIndexOf('.') + 1;
            if (name.charAt(i) === name.charAt(i)
                .toUpperCase()) {
                result.push(input[j]);
            }
        }
        return result;
    };
};
