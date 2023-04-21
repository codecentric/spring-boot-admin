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

export default {
  title: 'Components/Table',
};

const TemplateWithProps = () => ({
  template: `
    <table class="table table-full">
        <thead>
        <tr>
            <th>Anwendung</th>
            <th>Instanzen</th>
            <th>Zeit</th>
            <th>Ereignis</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 16:03:45.778</td>
            <td><span>INFO_CHANGED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 16:03:45.776</td>
            <td><span>ENDPOINTS_DETECTED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 16:03:45.768</td>
            <td><span>STATUS_CHANGED</span> <span>(UP)</span></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 16:03:45.755</td>
            <td><span>REGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 16:03:44.666</td>
            <td><span>INFO_CHANGED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 16:03:44.662</td>
            <td><span>ENDPOINTS_DETECTED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 16:03:44.657</td>
            <td><span>STATUS_CHANGED</span> <span>(UP)</span></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 16:03:44.639</td>
            <td><span>REGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 16:03:39.801</td>
            <td><span>DEREGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 16:03:39.799</td>
            <td><span>DEREGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 15:51:45.716</td>
            <td><span>INFO_CHANGED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 15:51:45.712</td>
            <td><span>ENDPOINTS_DETECTED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 15:51:45.702</td>
            <td><span>STATUS_CHANGED</span> <span>(UP)</span></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 15:51:45.690</td>
            <td><span>REGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:51:44.591</td>
            <td><span>INFO_CHANGED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:51:44.586</td>
            <td><span>ENDPOINTS_DETECTED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:51:44.578</td>
            <td><span>STATUS_CHANGED</span> <span>(UP)</span></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:51:44.564</td>
            <td><span>REGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>25b07dc98984</td>
            <td>04/29/2022 15:51:40.611</td>
            <td><span>DEREGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:51:39.600</td>
            <td><span>DEREGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:46:54.948</td>
            <td><span>INFO_CHANGED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:46:54.943</td>
            <td><span>ENDPOINTS_DETECTED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:46:54.935</td>
            <td><span>STATUS_CHANGED</span> <span>(UP)</span></td>
        </tr>
        <tr>
            <td>spring-boot-admin-sample-servlet</td>
            <td>af578c480d41</td>
            <td>04/29/2022 15:46:54.606</td>
            <td><span>REGISTERED</span> <!--v-if--></td>
        </tr>
        <tr>
            <td>spring-boot-1.5</td>
            <td>5914e4fcb78b</td>
            <td>04/29/2022 14:14:56.321</td>
            <td><span>ENDPOINTS_DETECTED</span> <!--v-if--></td>
        </tr>
        </tbody>
    </table>
  `,
});

export const Default = {
  render: TemplateWithProps,

  args: {
    startColor: '#ff0000',
    stopColor: '#00fa73',
  },
};
