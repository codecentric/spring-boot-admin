<!--
  - Copyright 2014-2018 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <section class="section" :class="{isLoading: isLoading}">
    <div class="container">
      <div
        v-if="error"
        class="message is-danger">
        <div class="message-body">
          <strong>
            <font-awesome-icon
              class="has-text-danger"
              icon="exclamation-triangle"/>
            Fetching beans failed.
          </strong>
          <p v-text="error.message"/>
        </div>
      </div>
      <div class="field">
        <p class="control is-expanded">
          <input
            class="input"
            type="search"
            placeholder="filter"
            v-model="filter">
        </p>
      </div>
      <contexts-list :contexts="contexts" :filter="filter" />
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import _ from 'lodash';
  import ContextsList from '@/views/instances/beans/contexts-list';

  const shortenName = (fullName) => {

    const shortenNextPackage = (className, packageIndex) => {
      const splittedClassName = className.split('.');
      return splittedClassName
        .map((packageName, index) => {
          if(index === packageIndex && !(splittedClassName.length === index+1)) {
            return packageName.charAt(0);
          } else {
            return packageName;
          }
        }).join('.');
    };

    if(!fullName || fullName.length < 60) {
      return fullName;
    } else {
      let shortenedClassname;
      let tmp = fullName;
      let i = 0;
      do  {
        shortenedClassname = tmp;
        tmp =  shortenNextPackage(shortenedClassname, i++);
      } while(shortenedClassname.length > 60 && tmp.length !== shortenedClassname.length);
      return tmp;
    }
  };
  class Bean {
    constructor(name, bean) {
      Object.assign(this, bean);
      this.name = name;
      this.shortName = shortenName(this.name);
      this.shortType = shortenName(this.type);
    }

    filter(filter) {
      if(!filter || filter === '') {
        return false;
      }
      return !this.name.includes(filter);
    }
  }


  const flattenBeans = beans => {
    return Object.keys(beans)
      .map((key) => {
        return new Bean(key, beans[key]);
      });
  };

  const flattenContexts = beanData => {
    if (_.isEmpty(beanData.contexts)) {
      return [];
    }
    const contexts = beanData.contexts;
    return Object.keys(contexts)
      .map((key) => {

        return {
          beans: flattenBeans(contexts[key].beans),
          name: key,
          parent: contexts[key].parentId
        };
      });
  };

  export default {
    components: {ContextsList},
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      isLoading: false,
      error: null,
      contexts: [],
      filter: '',
    }),
    methods: {
      fetchBeans: async function () {
        this.error = null;
        this.isLoading = true;
        try {
          const res = await this.instance.fetchBeans();
          this.contexts = flattenContexts(res.data);
        } catch (error) {
          console.warn('Fetching beans failed:', error);
          this.error = error;
        }
        this.isLoading = false;
      }
    },
    created() {
      this.fetchBeans();
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/beans',
        parent: 'instances',
        path: 'beans',
        group: 'Insights',
        component: this,
        label: 'Beans',
        order: 110,
        isEnabled: ({instance}) => instance.hasEndpoint('beans')
      });
    }
  }
</script>

