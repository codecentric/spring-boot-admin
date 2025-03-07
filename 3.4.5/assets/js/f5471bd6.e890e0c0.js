(self.webpackChunksite=self.webpackChunksite||[]).push([[732],{4150:(t,e,i)=>{var n={"./index-expanded.png":2399,"./index.png":6969,"./instance-dependencies.png":2340,"./instance-dependency-tree.png":7263,"./instance-details.png":1025,"./instance-envmanager.png":8561,"./instance-jmx.png":69,"./instance-logger.png":5863,"./instance-logs.png":346,"./instance-startup.png":8952,"./instance-threaddump.png":3721};function o(t){var e=s(t);return i(e)}function s(t){if(!i.o(n,t)){var e=new Error("Cannot find module '"+t+"'");throw e.code="MODULE_NOT_FOUND",e}return n[t]}o.keys=function(){return Object.keys(n)},o.resolve=s,t.exports=o,o.id=4150},6946:(t,e,i)=>{"use strict";i.r(e),i.d(e,{assets:()=>c,contentTitle:()=>u,default:()=>f,frontMatter:()=>d,metadata:()=>n,toc:()=>h});const n=JSON.parse('{"id":"index","title":"Overview","description":"<Screenshot","source":"@site/docs/01-index.md","sourceDirName":".","slug":"/index","permalink":"/3.4.5/docs/index","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":1,"frontMatter":{},"sidebar":"tutorialSidebar","next":{"title":"Installation and Setup","permalink":"/3.4.5/docs/installation-and-setup/"}}');var o=i(4848),s=i(8453),r=i(4145);const a={screenshot:"screenshot_tVSX",screenshot__description:"screenshot__description_nuig"};function l(t){let{images:e}=t;return(0,o.jsx)(r.default,{additionalTransfrom:0,arrows:!0,autoPlaySpeed:3e3,draggable:!0,infinite:!0,keyBoardControl:!0,minimumTouchDrag:80,pauseOnHover:!0,responsive:{desktop:{breakpoint:{max:3e3,min:1024},items:1},mobile:{breakpoint:{max:464,min:0},items:1},tablet:{breakpoint:{max:1024,min:464},items:1}},shouldResetAutoplay:!0,sliderClass:"",slidesToSlide:1,swipeable:!0,children:e.map(((t,e)=>{const{default:n}=i(4150)(`./${t.src}`);return(0,o.jsxs)("div",{className:a.screenshot,children:[(0,o.jsx)("div",{className:a.screenshot__description,children:t.description}),(0,o.jsx)("img",{src:n})]})}))})}const d={},u="Overview",c={},h=[];function p(t){const e={h1:"h1",header:"header",...(0,s.R)(),...t.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(e.header,{children:(0,o.jsx)(e.h1,{id:"overview",children:"Overview"})}),"\n",(0,o.jsx)(l,{images:[{src:"index.png",description:"Landing page showing connected services"},{src:"instance-details.png",description:"Details: Overview page showing some metrics of a service"},{src:"instance-dependencies.png",description:"Dependency view: Show all dependencies of a service"},{src:"instance-dependency-tree.png",description:"Dependency tree: Show a tree of dependencies"},{src:"instance-envmanager.png",description:"Env manager: View and manage environment variables"},{src:"instance-jmx.png",description:"JMX: Access and trigger JMX beans"},{src:"instance-logs.png",description:"Logs: View current logs of a service"},{src:"instance-logger.png",description:"Logger: Check current state of log levels and set new level without restarting the service"},{src:"instance-startup.png",description:"Startup: View startup times of a service"},{src:"instance-threaddump.png",description:"Threaddump: Insights into running threads and option to download a threaddump"}]})]})}function f(t={}){const{wrapper:e}={...(0,s.R)(),...t.components};return e?(0,o.jsx)(e,{...t,children:(0,o.jsx)(p,{...t})}):p(t)}},4145:(t,e,i)=>{t.exports=i(5961)},8371:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(6540);e.LeftArrow=function(t){var e=t.customLeftArrow,i=t.getState,o=t.previous,s=t.disabled,r=t.rtl;if(e)return n.cloneElement(e,{onClick:function(){return o()},carouselState:i(),disabled:s,rtl:r});var a=r?"rtl":"";return n.createElement("button",{"aria-label":"Go to previous slide",className:"react-multiple-carousel__arrow react-multiple-carousel__arrow--left "+a,onClick:function(){return o()},type:"button",disabled:s})};e.RightArrow=function(t){var e=t.customRightArrow,i=t.getState,o=t.next,s=t.disabled,r=t.rtl;if(e)return n.cloneElement(e,{onClick:function(){return o()},carouselState:i(),disabled:s,rtl:r});var a=r?"rtl":"";return n.createElement("button",{"aria-label":"Go to next slide",className:"react-multiple-carousel__arrow react-multiple-carousel__arrow--right "+a,onClick:function(){return o()},type:"button",disabled:s})}},6163:function(t,e,i){"use strict";var n,o=this&&this.__extends||(n=function(t,e){return(n=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(t,e){t.__proto__=e}||function(t,e){for(var i in e)e.hasOwnProperty(i)&&(t[i]=e[i])})(t,e)},function(t,e){function i(){this.constructor=t}n(t,e),t.prototype=null===e?Object.create(e):(i.prototype=e.prototype,new i)});Object.defineProperty(e,"__esModule",{value:!0});var s=i(6540),r=i(7035),a=i(2424),l=i(4169),d=i(8371),u=i(1519),c=i(6802),h=400,p="transform 400ms ease-in-out",f=function(t){function e(e){var i=t.call(this,e)||this;return i.containerRef=s.createRef(),i.listRef=s.createRef(),i.state={itemWidth:0,slidesToShow:0,currentSlide:0,totalItems:s.Children.count(e.children),deviceType:"",domLoaded:!1,transform:0,containerWidth:0},i.onResize=i.onResize.bind(i),i.handleDown=i.handleDown.bind(i),i.handleMove=i.handleMove.bind(i),i.handleOut=i.handleOut.bind(i),i.onKeyUp=i.onKeyUp.bind(i),i.handleEnter=i.handleEnter.bind(i),i.setIsInThrottle=i.setIsInThrottle.bind(i),i.next=r.throttle(i.next.bind(i),e.transitionDuration||h,i.setIsInThrottle),i.previous=r.throttle(i.previous.bind(i),e.transitionDuration||h,i.setIsInThrottle),i.goToSlide=r.throttle(i.goToSlide.bind(i),e.transitionDuration||h,i.setIsInThrottle),i.onMove=!1,i.initialX=0,i.lastX=0,i.isAnimationAllowed=!1,i.direction="",i.initialY=0,i.isInThrottle=!1,i.transformPlaceHolder=0,i}return o(e,t),e.prototype.resetTotalItems=function(){var t=this,e=s.Children.count(this.props.children),i=r.notEnoughChildren(this.state)?0:Math.max(0,Math.min(this.state.currentSlide,e));this.setState({totalItems:e,currentSlide:i},(function(){t.setContainerAndItemWidth(t.state.slidesToShow,!0)}))},e.prototype.setIsInThrottle=function(t){void 0===t&&(t=!1),this.isInThrottle=t},e.prototype.setTransformDirectly=function(t,e){var i=this.props.additionalTransfrom;this.transformPlaceHolder=t;var n=c.getTransform(this.state,this.props,this.transformPlaceHolder);this.listRef&&this.listRef.current&&(this.setAnimationDirectly(e),this.listRef.current.style.transform="translate3d("+(n+i)+"px,0,0)")},e.prototype.setAnimationDirectly=function(t){this.listRef&&this.listRef.current&&(this.listRef.current.style.transition=t?this.props.customTransition||p:"none")},e.prototype.componentDidMount=function(){this.setState({domLoaded:!0}),this.setItemsToShow(),window.addEventListener("resize",this.onResize),this.onResize(!0),this.props.keyBoardControl&&window.addEventListener("keyup",this.onKeyUp),this.props.autoPlay&&(this.autoPlay=setInterval(this.next,this.props.autoPlaySpeed))},e.prototype.setClones=function(t,e,i,n){var o=this;void 0===n&&(n=!1),this.isAnimationAllowed=!1;var a=s.Children.toArray(this.props.children),l=r.getInitialSlideInInfiniteMode(t||this.state.slidesToShow,a),d=r.getClones(this.state.slidesToShow,a),u=a.length<this.state.slidesToShow?0:this.state.currentSlide;this.setState({totalItems:d.length,currentSlide:i&&!n?u:l},(function(){o.correctItemsPosition(e||o.state.itemWidth)}))},e.prototype.setItemsToShow=function(t,e){var i=this,n=this.props.responsive;Object.keys(n).forEach((function(o){var s=n[o],r=s.breakpoint,a=s.items,l=r.max,d=r.min,u=[window.innerWidth];window.screen&&window.screen.width&&u.push(window.screen.width);var c=Math.min.apply(Math,u);d<=c&&c<=l&&(i.setState({slidesToShow:a,deviceType:o}),i.setContainerAndItemWidth(a,t,e))}))},e.prototype.setContainerAndItemWidth=function(t,e,i){var n=this;if(this.containerRef&&this.containerRef.current){var o=this.containerRef.current.offsetWidth,s=r.getItemClientSideWidth(this.props,t,o);this.setState({containerWidth:o,itemWidth:s},(function(){n.props.infinite&&n.setClones(t,s,e,i)})),e&&this.correctItemsPosition(s)}},e.prototype.correctItemsPosition=function(t,e,i){e&&(this.isAnimationAllowed=!0),!e&&this.isAnimationAllowed&&(this.isAnimationAllowed=!1);var n=this.state.totalItems<this.state.slidesToShow?0:-t*this.state.currentSlide;i&&this.setTransformDirectly(n,!0),this.setState({transform:n})},e.prototype.onResize=function(t){var e;e=!(!this.props.infinite||"boolean"==typeof t&&t),this.setItemsToShow(e)},e.prototype.componentDidUpdate=function(t,i){var n=this,o=t.keyBoardControl,s=t.autoPlay,a=t.children,l=i.containerWidth,d=i.domLoaded,u=i.currentSlide;if(this.containerRef&&this.containerRef.current&&this.containerRef.current.offsetWidth!==l&&(this.itemsToShowTimeout&&clearTimeout(this.itemsToShowTimeout),this.itemsToShowTimeout=setTimeout((function(){n.setItemsToShow(!0)}),this.props.transitionDuration||h)),o&&!this.props.keyBoardControl&&window.removeEventListener("keyup",this.onKeyUp),!o&&this.props.keyBoardControl&&window.addEventListener("keyup",this.onKeyUp),s&&!this.props.autoPlay&&this.autoPlay&&(clearInterval(this.autoPlay),this.autoPlay=void 0),s||!this.props.autoPlay||this.autoPlay||(this.autoPlay=setInterval(this.next,this.props.autoPlaySpeed)),a.length!==this.props.children.length?e.clonesTimeout=setTimeout((function(){n.props.infinite?n.setClones(n.state.slidesToShow,n.state.itemWidth,!0,!0):n.resetTotalItems()}),this.props.transitionDuration||h):this.props.infinite&&this.state.currentSlide!==u&&this.correctClonesPosition({domLoaded:d}),this.transformPlaceHolder!==this.state.transform&&(this.transformPlaceHolder=this.state.transform),this.props.autoPlay&&this.props.rewind&&!this.props.infinite&&r.isInRightEnd(this.state)){var c=this.props.transitionDuration||h;e.isInThrottleTimeout=setTimeout((function(){n.setIsInThrottle(!1),n.resetAutoplayInterval(),n.goToSlide(0,void 0,!!n.props.rewindWithAnimation)}),c+this.props.autoPlaySpeed)}},e.prototype.correctClonesPosition=function(t){var i=this,n=t.domLoaded,o=s.Children.toArray(this.props.children),a=r.checkClonesPosition(this.state,o,this.props),l=a.isReachingTheEnd,d=a.isReachingTheStart,u=a.nextSlide,c=a.nextPosition;this.state.domLoaded&&n&&(l||d)&&(this.isAnimationAllowed=!1,e.transformTimeout=setTimeout((function(){i.setState({transform:c,currentSlide:u})}),this.props.transitionDuration||h))},e.prototype.next=function(t){var i=this;void 0===t&&(t=0);var n=this.props,o=n.afterChange,s=n.beforeChange;if(!r.notEnoughChildren(this.state)){var a=r.populateNextSlides(this.state,this.props,t),l=a.nextSlides,d=a.nextPosition,u=this.state.currentSlide;void 0!==l&&void 0!==d&&("function"==typeof s&&s(l,this.getState()),this.isAnimationAllowed=!0,this.props.shouldResetAutoplay&&this.resetAutoplayInterval(),this.setState({transform:d,currentSlide:l},(function(){"function"==typeof o&&(e.afterChangeTimeout=setTimeout((function(){o(u,i.getState())}),i.props.transitionDuration||h))})))}},e.prototype.previous=function(t){var i=this;void 0===t&&(t=0);var n=this.props,o=n.afterChange,s=n.beforeChange;if(!r.notEnoughChildren(this.state)){var a=r.populatePreviousSlides(this.state,this.props,t),l=a.nextSlides,d=a.nextPosition;if(void 0!==l&&void 0!==d){var u=this.state.currentSlide;"function"==typeof s&&s(l,this.getState()),this.isAnimationAllowed=!0,this.props.shouldResetAutoplay&&this.resetAutoplayInterval(),this.setState({transform:d,currentSlide:l},(function(){"function"==typeof o&&(e.afterChangeTimeout2=setTimeout((function(){o(u,i.getState())}),i.props.transitionDuration||h))}))}}},e.prototype.resetAutoplayInterval=function(){this.props.autoPlay&&(clearInterval(this.autoPlay),this.autoPlay=setInterval(this.next,this.props.autoPlaySpeed))},e.prototype.componentWillUnmount=function(){window.removeEventListener("resize",this.onResize),this.props.keyBoardControl&&window.removeEventListener("keyup",this.onKeyUp),this.props.autoPlay&&this.autoPlay&&(clearInterval(this.autoPlay),this.autoPlay=void 0),this.itemsToShowTimeout&&clearTimeout(this.itemsToShowTimeout),e.clonesTimeout&&clearTimeout(e.clonesTimeout),e.isInThrottleTimeout&&clearTimeout(e.isInThrottleTimeout),e.transformTimeout&&clearTimeout(e.transformTimeout),e.afterChangeTimeout&&clearTimeout(e.afterChangeTimeout),e.afterChangeTimeout2&&clearTimeout(e.afterChangeTimeout2),e.afterChangeTimeout3&&clearTimeout(e.afterChangeTimeout3)},e.prototype.resetMoveStatus=function(){this.onMove=!1,this.initialX=0,this.lastX=0,this.direction="",this.initialY=0},e.prototype.getCords=function(t){var e=t.clientX,i=t.clientY;return{clientX:c.parsePosition(this.props,e),clientY:c.parsePosition(this.props,i)}},e.prototype.handleDown=function(t){if(!(!a.isMouseMoveEvent(t)&&!this.props.swipeable||a.isMouseMoveEvent(t)&&!this.props.draggable||this.isInThrottle)){var e=this.getCords(a.isMouseMoveEvent(t)?t:t.touches[0]),i=e.clientX,n=e.clientY;this.onMove=!0,this.initialX=i,this.initialY=n,this.lastX=i,this.isAnimationAllowed=!1}},e.prototype.handleMove=function(t){if(!(!a.isMouseMoveEvent(t)&&!this.props.swipeable||a.isMouseMoveEvent(t)&&!this.props.draggable||r.notEnoughChildren(this.state))){var e=this.getCords(a.isMouseMoveEvent(t)?t:t.touches[0]),i=e.clientX,n=e.clientY,o=this.initialX-i,s=this.initialY-n;if(this.onMove){if(!(Math.abs(o)>Math.abs(s)))return;var l=r.populateSlidesOnMouseTouchMove(this.state,this.props,this.initialX,this.lastX,i,this.transformPlaceHolder),d=l.direction,u=l.nextPosition,c=l.canContinue;d&&(this.direction=d,c&&void 0!==u&&this.setTransformDirectly(u)),this.lastX=i}}},e.prototype.handleOut=function(t){this.props.autoPlay&&!this.autoPlay&&(this.autoPlay=setInterval(this.next,this.props.autoPlaySpeed));var e="touchend"===t.type&&!this.props.swipeable,i=("mouseleave"===t.type||"mouseup"===t.type)&&!this.props.draggable;if(!e&&!i&&this.onMove){if(this.setAnimationDirectly(!0),"right"===this.direction)if(this.initialX-this.lastX>=this.props.minimumTouchDrag){var n=Math.round((this.initialX-this.lastX)/this.state.itemWidth);this.next(n)}else this.correctItemsPosition(this.state.itemWidth,!0,!0);"left"===this.direction&&(this.lastX-this.initialX>this.props.minimumTouchDrag?(n=Math.round((this.lastX-this.initialX)/this.state.itemWidth),this.previous(n)):this.correctItemsPosition(this.state.itemWidth,!0,!0)),this.resetMoveStatus()}},e.prototype.isInViewport=function(t){var e=t.getBoundingClientRect(),i=e.top,n=void 0===i?0:i,o=e.left,s=void 0===o?0:o,r=e.bottom,a=void 0===r?0:r,l=e.right,d=void 0===l?0:l;return 0<=n&&0<=s&&a<=(window.innerHeight||document.documentElement.clientHeight)&&d<=(window.innerWidth||document.documentElement.clientWidth)},e.prototype.isChildOfCarousel=function(t){return!!(t instanceof Element&&this.listRef&&this.listRef.current)&&this.listRef.current.contains(t)},e.prototype.onKeyUp=function(t){var e=t.target;switch(t.keyCode){case 37:if(this.isChildOfCarousel(e))return this.previous();break;case 39:if(this.isChildOfCarousel(e))return this.next();break;case 9:if(this.isChildOfCarousel(e)&&e instanceof HTMLInputElement&&this.isInViewport(e))return this.next()}},e.prototype.handleEnter=function(t){a.isMouseMoveEvent(t)&&this.autoPlay&&this.props.autoPlay&&this.props.pauseOnHover&&(clearInterval(this.autoPlay),this.autoPlay=void 0)},e.prototype.goToSlide=function(t,i,n){var o=this;if(void 0===n&&(n=!0),!this.isInThrottle){var s=this.state.itemWidth,r=this.props,a=r.afterChange,l=r.beforeChange,d=this.state.currentSlide;"function"!=typeof l||i&&("object"!=typeof i||i.skipBeforeChange)||l(t,this.getState()),this.isAnimationAllowed=n,this.props.shouldResetAutoplay&&this.resetAutoplayInterval(),this.setState({currentSlide:t,transform:-s*t},(function(){o.props.infinite&&o.correctClonesPosition({domLoaded:!0}),"function"!=typeof a||i&&("object"!=typeof i||i.skipAfterChange)||(e.afterChangeTimeout3=setTimeout((function(){a(d,o.getState())}),o.props.transitionDuration||h))}))}},e.prototype.getState=function(){return this.state},e.prototype.renderLeftArrow=function(t){var e=this,i=this.props,n=i.customLeftArrow,o=i.rtl;return s.createElement(d.LeftArrow,{customLeftArrow:n,getState:function(){return e.getState()},previous:this.previous,disabled:t,rtl:o})},e.prototype.renderRightArrow=function(t){var e=this,i=this.props,n=i.customRightArrow,o=i.rtl;return s.createElement(d.RightArrow,{customRightArrow:n,getState:function(){return e.getState()},next:this.next,disabled:t,rtl:o})},e.prototype.renderButtonGroups=function(){var t=this,e=this.props.customButtonGroup;return e?s.cloneElement(e,{previous:function(){return t.previous()},next:function(){return t.next()},goToSlide:function(e,i){return t.goToSlide(e,i)},carouselState:this.getState()}):null},e.prototype.renderDotsList=function(){var t=this;return s.createElement(l.default,{state:this.state,props:this.props,goToSlide:this.goToSlide,getState:function(){return t.getState()}})},e.prototype.renderCarouselItems=function(){var t=[];if(this.props.infinite){var e=s.Children.toArray(this.props.children);t=r.getClones(this.state.slidesToShow,e)}return s.createElement(u.default,{clones:t,goToSlide:this.goToSlide,state:this.state,notEnoughChildren:r.notEnoughChildren(this.state),props:this.props})},e.prototype.render=function(){var t=this.props,e=t.deviceType,i=t.arrows,n=t.renderArrowsWhenDisabled,o=t.removeArrowOnDeviceType,a=t.infinite,l=t.containerClass,d=t.sliderClass,u=t.customTransition,h=t.additionalTransfrom,f=t.renderDotsOutside,m=t.renderButtonGroupOutside,v=t.className,g=t.rtl,y=r.getInitialState(this.state,this.props),S=y.shouldRenderOnSSR,T=y.shouldRenderAtAll,w=r.isInLeftEnd(this.state),b=r.isInRightEnd(this.state),C=i&&!(o&&(e&&-1<o.indexOf(e)||this.state.deviceType&&-1<o.indexOf(this.state.deviceType)))&&!r.notEnoughChildren(this.state)&&T,I=!a&&w,P=!a&&b,M=c.getTransform(this.state,this.props);return s.createElement(s.Fragment,null,s.createElement("div",{className:"react-multi-carousel-list "+l+" "+v,dir:g?"rtl":"ltr",ref:this.containerRef},s.createElement("ul",{ref:this.listRef,className:"react-multi-carousel-track "+d,style:{transition:this.isAnimationAllowed?u||p:"none",overflow:S?"hidden":"unset",transform:"translate3d("+(M+h)+"px,0,0)"},onMouseMove:this.handleMove,onMouseDown:this.handleDown,onMouseUp:this.handleOut,onMouseEnter:this.handleEnter,onMouseLeave:this.handleOut,onTouchStart:this.handleDown,onTouchMove:this.handleMove,onTouchEnd:this.handleOut},this.renderCarouselItems()),C&&(!I||n)&&this.renderLeftArrow(I),C&&(!P||n)&&this.renderRightArrow(P),T&&!m&&this.renderButtonGroups(),T&&!f&&this.renderDotsList()),T&&f&&this.renderDotsList(),T&&m&&this.renderButtonGroups())},e.defaultProps={slidesToSlide:1,infinite:!1,draggable:!0,swipeable:!0,arrows:!0,renderArrowsWhenDisabled:!1,containerClass:"",sliderClass:"",itemClass:"",keyBoardControl:!0,autoPlaySpeed:3e3,showDots:!1,renderDotsOutside:!1,renderButtonGroupOutside:!1,minimumTouchDrag:80,className:"",dotListClass:"",focusOnSelect:!1,centerMode:!1,additionalTransfrom:0,pauseOnHover:!0,shouldResetAutoplay:!0,rewind:!1,rtl:!1,rewindWithAnimation:!1},e}(s.Component);e.default=f},1519:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(6540),o=i(7035);e.default=function(t){var e=t.props,i=t.state,s=t.goToSlide,r=t.clones,a=t.notEnoughChildren,l=i.itemWidth,d=e.children,u=e.infinite,c=e.itemClass,h=e.itemAriaLabel,p=e.partialVisbile,f=e.partialVisible,m=o.getInitialState(i,e),v=m.flexBisis,g=m.shouldRenderOnSSR,y=m.domFullyLoaded,S=m.partialVisibilityGutter;return m.shouldRenderAtAll?(p&&console.warn('WARNING: Please correct props name: "partialVisible" as old typo will be removed in future versions!'),n.createElement(n.Fragment,null,(u?r:n.Children.toArray(d)).map((function(t,r){return n.createElement("li",{key:r,"data-index":r,onClick:function(){e.focusOnSelect&&s(r)},"aria-hidden":o.getIfSlideIsVisbile(r,i)?"false":"true","aria-label":h||(t.props.ariaLabel?t.props.ariaLabel:null),style:{flex:g?"1 0 "+v+"%":"auto",position:"relative",width:y?((p||f)&&S&&!a?l-S:l)+"px":"auto"},className:"react-multi-carousel-item "+(o.getIfSlideIsVisbile(r,i)?"react-multi-carousel-item--active":"")+" "+c},t)})))):null}},4169:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(6540),o=i(9189),s=i(1811),r=i(6802);e.default=function(t){var e=t.props,i=t.state,a=t.goToSlide,l=t.getState,d=e.showDots,u=e.customDot,c=e.dotListClass,h=e.infinite,p=e.children;if(!d||r.notEnoughChildren(i))return null;var f,m=i.currentSlide,v=i.slidesToShow,g=r.getSlidesToSlide(i,e),y=n.Children.toArray(p);f=h?Math.ceil(y.length/g):Math.ceil((y.length-v)/g)+1;var S=s.getLookupTableForNextSlides(f,i,e,y),T=o.getOriginalIndexLookupTableByClones(v,y),w=T[m];return n.createElement("ul",{className:"react-multi-carousel-dot-list "+c},Array(f).fill(0).map((function(t,e){var i,o;if(h){o=S[e];var s=T[o];i=w===s||s<=w&&w<s+g}else{var r=y.length-v,d=e*g;i=(o=r<d?r:d)===m||o<m&&m<o+g&&m<y.length-v}return u?n.cloneElement(u,{index:e,active:i,key:e,onClick:function(){return a(o)},carouselState:l()}):n.createElement("li",{"data-index":e,key:e,className:"react-multi-carousel-dot "+(i?"react-multi-carousel-dot--active":"")},n.createElement("button",{"aria-label":"Go to slide "+(e+1),onClick:function(){return a(o)}}))})))}},5961:(t,e,i)=>{"use strict";var n=i(6163);e.default=n.default},2424:function(t,e,i){"use strict";var n,o=this&&this.__extends||(n=function(t,e){return(n=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(t,e){t.__proto__=e}||function(t,e){for(var i in e)e.hasOwnProperty(i)&&(t[i]=e[i])})(t,e)},function(t,e){function i(){this.constructor=t}n(t,e),t.prototype=null===e?Object.create(e):(i.prototype=e.prototype,new i)});Object.defineProperty(e,"__esModule",{value:!0});var s=i(6540);e.isMouseMoveEvent=function(t){return"clientY"in t};var r=function(t){function e(){return null!==t&&t.apply(this,arguments)||this}return o(e,t),e}(s.Component);e.default=r},9189:(t,e)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0}),e.getOriginalCounterPart=function(t,e,i){var n=e.slidesToShow,o=e.currentSlide;return i.length>2*n?t+2*n:o>=i.length?i.length+t:t},e.getOriginalIndexLookupTableByClones=function(t,e){if(e.length>2*t){for(var i={},n=e.length-2*t,o=e.length-n,s=n,r=0;r<o;r++)i[r]=s,s++;var a=e.length+o,l=a+e.slice(0,2*t).length,d=0;for(r=a;r<=l;r++)i[r]=d,d++;var u=a,c=0;for(r=o;r<u;r++)i[r]=c,c++;return i}i={};var h=3*e.length,p=0;for(r=0;r<h;r++)i[r]=p,++p===e.length&&(p=0);return i},e.getClones=function(t,e){return e.length<t?e:e.length>2*t?e.slice(e.length-2*t,e.length).concat(e,e.slice(0,2*t)):e.concat(e,e)},e.getInitialSlideInInfiniteMode=function(t,e){return e.length>2*t?2*t:e.length},e.checkClonesPosition=function(t,e,i){var n,o=t.currentSlide,s=t.slidesToShow,r=t.itemWidth,a=t.totalItems,l=0,d=0,u=0===o,c=e.length-(e.length-2*s);return e.length<s?(d=l=0,u=n=!1):e.length>2*s?((n=o>=c+e.length)&&(d=-r*(l=o-e.length)),u&&(d=-r*(l=c+(e.length-2*s)))):((n=o>=2*e.length)&&(d=-r*(l=o-e.length)),u&&(d=i.showDots?-r*(l=e.length):-r*(l=a/3))),{isReachingTheEnd:n,isReachingTheStart:u,nextSlide:l,nextPosition:d}}},6802:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(4867);function o(t){var e=t.slidesToShow;return t.totalItems<e}function s(t,e,i){var n=i||t.transform;return!e.infinite&&0===t.currentSlide||o(t)?n:n+t.itemWidth/2}function r(t){var e=t.currentSlide,i=t.totalItems;return!(e+t.slidesToShow<i)}function a(t,e,i,n){void 0===e&&(e=0);var s=t.currentSlide,a=t.slidesToShow,l=r(t),d=!i.infinite&&l,u=n||t.transform;if(o(t))return u;var c=u+s*e;return d?c+(t.containerWidth-(t.itemWidth-e)*a):c}function l(t,e){return t.rtl?-1*e:e}e.notEnoughChildren=o,e.getInitialState=function(t,e){var i,o=t.domLoaded,s=t.slidesToShow,r=t.containerWidth,a=t.itemWidth,l=e.deviceType,d=e.responsive,u=e.ssr,c=e.partialVisbile,h=e.partialVisible,p=Boolean(o&&s&&r&&a);u&&l&&!p&&(i=n.getWidthFromDeviceType(l,d));var f=Boolean(u&&l&&!p&&i);return{shouldRenderOnSSR:f,flexBisis:i,domFullyLoaded:p,partialVisibilityGutter:n.getPartialVisibilityGutter(d,c||h,l,t.deviceType),shouldRenderAtAll:f||p}},e.getIfSlideIsVisbile=function(t,e){var i=e.currentSlide,n=e.slidesToShow;return i<=t&&t<i+n},e.getTransformForCenterMode=s,e.isInLeftEnd=function(t){return!(0<t.currentSlide)},e.isInRightEnd=r,e.getTransformForPartialVsibile=a,e.parsePosition=l,e.getTransform=function(t,e,i){var o=e.partialVisbile,r=e.partialVisible,d=e.responsive,u=e.deviceType,c=e.centerMode,h=i||t.transform,p=n.getPartialVisibilityGutter(d,o||r,u,t.deviceType);return l(e,r||o?a(t,p,e,i):c?s(t,e,i):h)},e.getSlidesToSlide=function(t,e){var i=t.domLoaded,n=t.slidesToShow,o=t.containerWidth,s=t.itemWidth,r=e.deviceType,a=e.responsive,l=e.slidesToSlide||1,d=Boolean(i&&n&&o&&s);return e.ssr&&e.deviceType&&!d&&Object.keys(a).forEach((function(t){var e=a[t].slidesToSlide;r===t&&e&&(l=e)})),d&&Object.keys(a).forEach((function(t){var e=a[t],i=e.breakpoint,n=e.slidesToSlide,o=i.max,s=i.min;n&&window.innerWidth>=s&&window.innerWidth<=o&&(l=n)})),l}},1811:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(9189),o=i(6802);e.getLookupTableForNextSlides=function(t,e,i,s){var r={},a=o.getSlidesToSlide(e,i);return Array(t).fill(0).forEach((function(t,i){var o=n.getOriginalCounterPart(i,e,s);if(0===i)r[0]=o;else{var l=r[i-1]+a;r[i]=l}})),r}},4867:(t,e)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});e.getPartialVisibilityGutter=function(t,e,i,n){var o=0,s=n||i;return e&&s&&(o=t[s].partialVisibilityGutter||t[s].paritialVisibilityGutter),o},e.getWidthFromDeviceType=function(t,e){var i;return e[t]&&(i=(100/e[t].items).toFixed(1)),i},e.getItemClientSideWidth=function(t,e,i){return Math.round(i/(e+(t.centerMode?1:0)))}},7035:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(9189);e.getOriginalCounterPart=n.getOriginalCounterPart,e.getClones=n.getClones,e.checkClonesPosition=n.checkClonesPosition,e.getInitialSlideInInfiniteMode=n.getInitialSlideInInfiniteMode;var o=i(4867);e.getWidthFromDeviceType=o.getWidthFromDeviceType,e.getPartialVisibilityGutter=o.getPartialVisibilityGutter,e.getItemClientSideWidth=o.getItemClientSideWidth;var s=i(6802);e.getInitialState=s.getInitialState,e.getIfSlideIsVisbile=s.getIfSlideIsVisbile,e.getTransformForCenterMode=s.getTransformForCenterMode,e.getTransformForPartialVsibile=s.getTransformForPartialVsibile,e.isInLeftEnd=s.isInLeftEnd,e.isInRightEnd=s.isInRightEnd,e.notEnoughChildren=s.notEnoughChildren,e.getSlidesToSlide=s.getSlidesToSlide;var r=i(2259);e.throttle=r.default;var a=i(4713);e.throwError=a.default;var l=i(6650);e.populateNextSlides=l.populateNextSlides;var d=i(6862);e.populatePreviousSlides=d.populatePreviousSlides;var u=i(251);e.populateSlidesOnMouseTouchMove=u.populateSlidesOnMouseTouchMove},251:(t,e)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0}),e.populateSlidesOnMouseTouchMove=function(t,e,i,n,o,s){var r,a,l=t.itemWidth,d=t.slidesToShow,u=t.totalItems,c=t.currentSlide,h=e.infinite,p=!1,f=Math.round((i-n)/l),m=Math.round((n-i)/l),v=i<o;if(o<i&&f<=d){r="right";var g=Math.abs(-l*(u-d)),y=s-(n-o),S=c===u-d;(Math.abs(y)<=g||S&&h)&&(a=y,p=!0)}return v&&m<=d&&(r="left",((y=s+(o-n))<=0||0===c&&h)&&(p=!0,a=y)),{direction:r,nextPosition:a,canContinue:p}}},6650:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(6802);e.populateNextSlides=function(t,e,i){void 0===i&&(i=0);var o,s,r=t.slidesToShow,a=t.currentSlide,l=t.itemWidth,d=t.totalItems,u=n.getSlidesToSlide(t,e),c=a+1+i+r+(0<i?0:u);return s=c<=d?-l*(o=a+i+(0<i?0:u)):d<c&&a!==d-r?-l*(o=d-r):o=void 0,{nextSlides:o,nextPosition:s}}},6862:(t,e,i)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=i(6540),o=i(6802),s=i(6802);e.populatePreviousSlides=function(t,e,i){void 0===i&&(i=0);var r,a,l=t.currentSlide,d=t.itemWidth,u=t.slidesToShow,c=e.children,h=e.showDots,p=e.infinite,f=o.getSlidesToSlide(t,e),m=l-i-(0<i?0:f),v=(n.Children.toArray(c).length-u)%f;return a=0<=m?(r=m,h&&!p&&0<v&&s.isInRightEnd(t)&&(r=l-v),-d*r):r=m<0&&0!==l?0:void 0,{nextSlides:r,nextPosition:a}}},2259:(t,e)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0});e.default=function(t,e,i){var n;return function(){var o=arguments;n||(t.apply(this,o),n=!0,"function"==typeof i&&i(!0),setTimeout((function(){n=!1,"function"==typeof i&&i(!1)}),e))}}},4713:(t,e)=>{"use strict";Object.defineProperty(e,"__esModule",{value:!0}),e.default=function(t,e){var i=e.partialVisbile,n=e.partialVisible,o=e.centerMode,s=e.ssr,r=e.responsive;if((i||n)&&o)throw new Error("center mode can not be used at the same time with partialVisible");if(!r)throw s?new Error("ssr mode need to be used in conjunction with responsive prop"):new Error("Responsive prop is needed for deciding the amount of items to show on the screen");if(r&&"object"!=typeof r)throw new Error("responsive prop must be an object")}},2399:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/index-expanded-22cac83322e4017ed3c39259f7b0cca4.png"},6969:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/index-c809724cd7c9a1ed5db1b9717443b5bf.png"},2340:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-dependencies-2ea3528f707855f25cac80bcaa66c4db.png"},7263:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-dependency-tree-f4c44b06828ceeacaa13b4154dd25922.png"},1025:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-details-5bea1876e2ff9b97dc4b4d0a4312d96e.png"},8561:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-envmanager-896c060fc1a445640c8d8d1d0b9ba0c1.png"},69:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-jmx-5c4627a22662d4b6cbc87903d7d8d289.png"},5863:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-logger-603d0f47b71f09aafdc189f979e1ff89.png"},346:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-logs-37abfb667e18c0bc0ed52b51463d53e0.png"},8952:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-startup-19968c9435decc930d250a9dc35c1b39.png"},3721:(t,e,i)=>{"use strict";i.r(e),i.d(e,{default:()=>n});const n=i.p+"assets/images/instance-threaddump-861da51186d3bf2cc704df7efe560ead.png"},8453:(t,e,i)=>{"use strict";i.d(e,{R:()=>r,x:()=>a});var n=i(6540);const o={},s=n.createContext(o);function r(t){const e=n.useContext(s);return n.useMemo((function(){return"function"==typeof t?t(e):{...e,...t}}),[e,t])}function a(t){let e;return e=t.disableParentContext?"function"==typeof t.components?t.components(o):t.components||o:r(t.components),n.createElement(s.Provider,{value:e},t.children)}}}]);