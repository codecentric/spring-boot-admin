/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import sbaConfig from '@/sba-config'
import ViewRegistry from './viewRegistry';

describe('viewRegistry', () => {
    describe('given view already in the registry', function () {


        it('it should replace the existing one', async () => {
            const viewRegistry = new ViewRegistry();

            viewRegistry.addView(...[
                {name: 'view', group: 'group'},
                {name: 'duplicateView', group: 'group'},
                {name: 'duplicateView', group: 'group'}
            ])

            expect(viewRegistry.views).toHaveLength(2);
        });

    });

    it('hide or show views depending on their settings', () => {
        sbaConfig.uiSettings.viewSettings = [
          {name: 'disabledView', enabled: false},
          {name: 'explicitlyEnabledView', enabled: true}
        ];

        const viewRegistry = new ViewRegistry();
        viewRegistry.addView(...[
            {name: 'disabledView', group: 'group'},
            {name: 'explicitlyEnabledView', group: 'group'},
            {name: 'implicitlyEnabledView', group: 'group'}
        ])

        let disabledView = viewRegistry.getViewByName('disabledView');
        expect(disabledView).toBeDefined();
        expect(disabledView.isEnabled()).toBeFalsy();

        let implicitlyEnabledView = viewRegistry.getViewByName('implicitlyEnabledView');
        expect(implicitlyEnabledView).toBeDefined();
        expect(implicitlyEnabledView.isEnabled()).toBeTruthy();

        let explicitlyEnabledView = viewRegistry.getViewByName('explicitlyEnabledView');
        expect(explicitlyEnabledView).toBeDefined();
        expect(explicitlyEnabledView.isEnabled()).toBeTruthy();
    });
});
