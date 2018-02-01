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
    <nav id="navigation" class="navbar is-fixed-top">
        <div class="container">
            <div class="navbar-brand">
                <router-link class="navbar-item logo" to="/">
                    <boot-logo></boot-logo>
                    Spring Boot Admin
                </router-link>

                <div class="navbar-burger burger" @click.stop="showMenu = !showMenu">
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
            </div>
            <div class="navbar-menu" :class="{'is-active' : showMenu}">
                <div class="navbar-start">
                </div>
                <div class="navbar-end">
                    <router-link class="navbar-item" v-for="view in views" :to="{name: view.name}" :key="view.name">
                        <component :is="view.handle"></component>
                    </router-link>

                    <div class="navbar-item">
                        <form action="logout" method="post">
                            <button class="button is-icon" type="submit" value="logout">
                                Logout&nbsp;<font-awesome-icon icon="sign-out-alt" size="lg"></font-awesome-icon>
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </nav>
</template>

<script>
  import bootLogo from '@/assets/img/boot-logo.svg';

  export default {
    components: {
      bootLogo
    },
    data: () => ({
      showMenu: false
    }),
    props: ['views'],
    mounted() {
      document.documentElement.classList.add('has-navbar-fixed-top');
    },
    beforeDestroy() {
      document.documentElement.classList.remove('has-navbar-fixed-top')
    }
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";

    .logo {
        font-size: 1.5rem;
        font-weight: 600;
        color: $white;

        & svg {
            fill: currentColor;
            padding-right: 0.5rem;
        }
    }
</style>