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
    <div class="applications-list">
        <template v-for="application in applications">
            <div v-if="selected !== application.name" :key="application.name"
                 @click.stop="select(application.name)"
                 class="applications-list-item applications-list-item--collapsed">
                <sba-status :status="application.status"
                            :date="application.statusTimestamp"
                            class="applications-list-item__status"></sba-status>
                <span class="applications-list-item__text">
                    <span v-text="application.name"></span>
                    <span class="applications-list-item__secondary">
                        <a v-if="application.instances.length === 1"
                           v-text="application.instances[0].registration.serviceUrl || application.instances[0].registration.healthUrl"
                           :href="application.instances[0].registration.serviceUrl || application.instances[0].registration.healthUrl"></a>
                        <span v-else
                              v-text="`${application.instances.length} instances`"></span>
                    </span>
                </span>
                <div class="applications-list-item__text"
                     v-text="application.version"></div>
                <div class="applications-list-item__actions">
                    <sba-icon-button icon="trash"
                                     v-if="application.isUnregisterable"
                                     @click.native.stop="unregister(application)">
                    </sba-icon-button>
                </div>
            </div>
            <div v-else :key="application.name"
                 v-on-clickaway="deselect"
                 class="applications-list-item applications-list-item--detailed">
                <div class="applications-list-item__header"
                     @click.stop="deselect()">
                    <div class="applications-list-item__header-text" v-text="application.name"></div>
                    <div class="applications-list-item__header-actions">
                        <sba-icon-button icon="trash"
                                         v-if="application.isUnregisterable"
                                         @click.native.stop="unregister(application)">
                        </sba-icon-button>
                    </div>
                </div>
                <ul class="applications-list-item__instance-list">
                    <li v-for="instance in application.instances" :key="instance.id"
                        class="applications-list-item__instance"
                        @click.stop="showDetails(instance)">
                        <sba-status :status="instance.statusInfo.status"
                                    :date="instance.statusTimestamp"
                                    class="applications-list-item__status"></sba-status>
                        <span class="applications-list-item__text">
                            <a v-text="instance.registration.serviceUrl || instance.registration.healthUrl"
                               :href="instance.registration.serviceUrl || instance.registration.healthUrl"
                               @click.stop=""></a>
                            <span v-text="instance.id"
                                  class="applications-list-item__secondary"></span>
                        </span>
                        <span v-text="instance.info.version"
                              class="applications-list-item__text"></span>
                        <div class="applications-list-item__actions">
                            <sba-icon-button icon="trash"
                                             v-if="instance.isUnregisterable"
                                             @click.native.stop="unregister(instance)">
                            </sba-icon-button>
                        </div>
                    </li>
                </ul>
            </div>
        </template>
    </div>
</template>

<script>
  import {directive as onClickaway} from 'vue-clickaway';

  export default {
    directives: {
      onClickaway
    },

    data: () => ({
      selected: null,
      errors: []
    }),

    methods: {
      select(name) {
        this.selected = name;
      },
      deselect() {
        this.selected = null;
      },
      showDetails(instance) {
        this.$router.push(`/instances/${instance.id}`)
      },
      async unregister(item) {
        try {
          item.unregister();
        } catch (e) {
          this.errors.push(e);
        }
      }
    },

    props: {
      'applications': Array
    }
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";

    $list-background-color: $white !default;
    $list-shadow: 0 2px 3px rgba($black, 0.1), 0 0 0 1px rgba($black, 0.1) !default;
    $list-color: $text !default;

    .applications-list-item {
        background-color: $list-background-color;
        box-shadow: $list-shadow;
        color: $list-color;
        max-width: 100%;

        &--collapsed {
            display: flex;
            align-items: center;
            overflow: hidden;
            cursor: pointer;
            &:hover {
                background-color: $white-bis;
            }
        }

        &--detailed {
            display: flex;
            flex-direction: column;
            margin: ($gap / 2) 0;
        }

        &__status {
            width: 40px;
            padding-left: ($gap / 2);
        }

        &__text {
            padding-left: ($gap / 2);
            display: inline-flex;
            align-items: flex-start;
            flex-direction: column;
            flex-grow: 1;
            flex-basis: 0;
        }

        &__secondary {
            color: $grey-light;
            font-size: $size-6;
        }

        &__actions {
            justify-self: end;
            padding-right: ($gap / 2);
            opacity: 0;
            transition: all $easing $speed;
            will-change: opacity;
        }

        *:hover > &__actions {
            opacity: 1;
        }

        &__header {
            display: flex;
            height: 48px;
            align-items: center;
            overflow: hidden;
            cursor: pointer;

            &-text {
                color: $grey-darker;
                font-weight: $weight-semibold;
                padding-left: $gap;
                flex-grow: 1;
            }

            &-actions {
                justify-self: end;
                padding-right: $gap;
            }
        }

        &__instance-list {
            list-style: none;
            margin: 0 ($gap / 2) ($gap / 2) ($gap / 2);
            padding: 0;
        }

        &__instance {
            display: flex;
            height: 48px;
            overflow: hidden;
            background-color: #fff;
            align-items: center;
            cursor: pointer;
            &:hover {
                background-color: $white-bis;
            }

            & + & {
                border-top: 1px solid rgba(0, 0, 0, .12);
                margin-top: 0;
            }
        }
    }
</style>
