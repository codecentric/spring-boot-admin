<!--
  - Copyright 2014-2019 the original author or authors.
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
  <aside class="sidebar">
    <router-link
      :to="{name: 'instances/details', params: {instanceId: instance.id}}"
      class="instance-summary"
      :class="`instance-summary--${instance.statusInfo.status}`"
    >
      <div
        class="instance-summary__name"
        v-text="instance.registration.name"
      />
      <div
        class="instance-summary__id"
        v-text="instance.id"
      />
    </router-link>
    <ul>
      <li
        v-for="group in enabledGroupedViews"
        :key="group.name"
        class="sidebar-group"
        :class="{'is-active' : isActiveGroup(group)}"
        @mouseenter="hasMultipleViews(group) && !isActiveGroup(group) && showFlyout($event)"
        @mouseleave="hasMultipleViews(group) && !isActiveGroup(group) && hideFlyout($event)"
      >
        <router-link
          :to="{ name: group.views[0].name, params: { 'instanceId' : instance.id } }"
          v-text="hasMultipleViews(group) ? $t('sidebar.' + group.id + '.title') : ($t(group.views[0].label))"
          active-class=""
          exact-active-class=""
          :class="{'is-active' : isActiveGroup(group) }"
        />
        <ul
          v-if="hasMultipleViews(group)"
          class="sidebar-group-items"
        >
          <li
            v-for="view in group.views"
            :key="view.name"
          >
            <router-link :to="{ name: view.name, params: { 'instanceId' : instance.id } }">
              <component :is="view.handle" />
            </router-link>
          </li>
        </ul>
      </li>
    </ul>
  </aside>
</template>

<script>
  import sticksBelow from '@/directives/sticks-below';
  import Application from '@/services/application';
  import Instance from '@/services/instance';
  import {compareBy} from '@/utils/collections';

  export default {
    props: {
      views: {
        type: Array,
        default: () => []
      },
      instance: {
        type: Instance,
        default: null
      },
      application: {
        type: Application,
        default: null
      }
    },
    directives: {sticksBelow},
    data: () => ({
      isStuck: false
    }),
    computed: {
      enabledViews() {
        if (!this.instance) {
          return [];
        }

        return [...this.views].filter(
          view => typeof view.isEnabled === 'undefined' || view.isEnabled({instance: this.instance})
        ).sort(compareBy(v => v.order));
      },
      enabledGroupedViews() {
        const groups = new Map();
        this.enabledViews.forEach(view => {
            const groupName = view.group;
            const group = groups.get(groupName) || {
              id: groupName,
              order: Number.MAX_SAFE_INTEGER,
              views: []
            };
            groups.set(groupName, {
              ...group,
              order: Math.min(group.order, view.order),
              views: [...group.views, view]
            })
          }
        );
        return Array.from(groups.values());
      }
    },
    methods: {
      isActiveGroup(group) {
        return group.views.includes(this.$route.meta.view);
      },
      hasMultipleViews(group) {
        return group.views.length > 1;
      },
      onScroll() {
        this.isStuck = this.$el.getBoundingClientRect().top <= 52;
      },
      showFlyout(event) {
        const groupEl = event.target;
        groupEl.classList.add('is-showing-flyout');
        const boundingRect = groupEl.getBoundingClientRect();

        const itemsEl = event.target.querySelector('.sidebar-group-items');
        itemsEl.style.top = `${boundingRect.top}px`;
        itemsEl.style.left = `${boundingRect.right + 1}px`;
      },
      hideFlyout(event) {
        const groupEl = event.target;
        groupEl.classList.remove('is-showing-flyout');
        const itemsEl = event.target.querySelector('.sidebar-group-items');
        itemsEl.style = undefined;
      }
    },
    mounted() {
      window.addEventListener('scroll', this.onScroll);
    },
    beforeDestroy() {
      window.removeEventListener('scroll', this.onScroll);
    },
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .sidebar {
    height: 100%;
    width: 100%;
    background-color: $white-bis;
    border-right: 1px solid $grey-lighter;
    overflow-x: auto;

    .instance-summary {
      padding: 1rem 0.5rem;
      color: $text;
      background-color: $grey;
      text-align: center;

      &:hover {
        background-color: rgba($grey, 0.90);
      }

      &__name {
        max-width: 175px;
        margin: 0 auto;
        font-weight: $weight-semibold;
        font-size: $size-5;
      }

      &__id {
        font-size: $size-6;
      }

      &--UP {
        color: $primary-invert;
        background-color: $primary;
        &:hover {
          background-color: rgba($primary, 0.90);
        }
      }

      &--RESTRICTED {
        color: $warning-invert;
        background-color: $warning;
        &:hover {
          background-color: rgba($warning, 0.90);
        }
      }

      &--OUT_OF_SERVICE,
      &--DOWN {
        color: $danger-invert;
        background-color: $danger;
        &:hover {
          background-color: rgba($danger, 0.90);
        }
      }
    }

    a {
      border-radius: $radius-small;
      color: $text;
      overflow: hidden;
      text-overflow: ellipsis;
      display: block;
      padding: 0.5em 0.75em;

      &:hover {
        background-color: rgba(0, 0, 0, 0.04);
      }
    }

    .sidebar-group {
      .sidebar-group-items {
        display: none;
      }

      &.is-active {
        box-shadow: inset 4px 0 0 $primary;
        background-color: rgba(0, 0, 0, 0.04);

        > a {
          color: $text;
          font-weight: $weight-semibold;
        }

        .sidebar-group-items {
          display: block;
          padding-bottom: 0.25em;

          a {
            padding-left: 2em;

            &.is-active {
              background-color: $primary;
              color: $link-invert;
            }
          }
        }
      }

      &.is-showing-flyout {
        .sidebar-group-items {
          position: fixed;
          display: block;
          z-index: 999;
          background-color: $white;
          min-width: 150px;
          box-shadow: 0 2px 3px rgba($black, 0.1), 0 0 0 1px rgba($black, 0.1);
        }
      }
    }
  }
</style>
