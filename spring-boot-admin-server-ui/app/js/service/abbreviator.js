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
    function computeDotIndexes(fqName, delimiter, preserveLast) {
        var dotArray = [];

        //iterate over String and find dots
        var lastIndex = -1;
        do {
            lastIndex = fqName.indexOf(delimiter, lastIndex + 1);
            if (lastIndex !== -1) {
                dotArray.push(lastIndex);
            }
        } while (lastIndex !== -1)

        // remove dots to preserve more than the last element
        for (var i = 0; i < preserveLast - 1; i++) {
            dotArray.pop();
        }

        return dotArray;
    }

    function computeLengthArray(fqName, targetLength, dotArray, shortenThreshold) {
        var lengthArray = [];
        var toTrim = fqName.length - targetLength;

        for (var i = 0; i < dotArray.length; i++) {
            var previousDotPosition = -1;
            if (i > 0) {
                previousDotPosition = dotArray[i - 1];
            }

            var len = dotArray[i] - previousDotPosition - 1;
            var newLen = (toTrim > 0 && len > shortenThreshold ? 1 : len);

            toTrim -= (len - newLen);
            lengthArray[i] = newLen + 1;
        }

        var lastDotIndex = dotArray.length - 1;
        lengthArray[dotArray.length] = fqName.length - dotArray[lastDotIndex];

        return lengthArray;
    }

    this.abbreviate = function (fqName, delimiter, targetLength, preserveLast, shortenThreshold) {
        if (fqName.length < targetLength) {
            return fqName;
        }

        var dotIndexesArray = computeDotIndexes(fqName, delimiter, preserveLast);

        if (dotIndexesArray.length === 0) {
            return fqName;
        }

        var lengthArray = computeLengthArray(fqName, targetLength, dotIndexesArray,
            shortenThreshold);

        var result = '';
        for (var i = 0; i <= dotIndexesArray.length; i++) {
            if (i === 0) {
                result += fqName.substr(0, lengthArray[i] - 1);
            } else {
                result += fqName.substr(dotIndexesArray[i - 1], lengthArray[i]);
            }
        }

        return result;
    };
};
