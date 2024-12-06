"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[459],{7399:(e,t,n)=>{n.d(t,{In:()=>_e});var o=n(6540);const r=Object.freeze({left:0,top:0,width:16,height:16}),i=Object.freeze({rotate:0,vFlip:!1,hFlip:!1}),c=Object.freeze({...r,...i}),s=Object.freeze({...c,body:"",hidden:!1});function a(e,t){const n=function(e,t){const n={};!e.hFlip!=!t.hFlip&&(n.hFlip=!0),!e.vFlip!=!t.vFlip&&(n.vFlip=!0);const o=((e.rotate||0)+(t.rotate||0))%4;return o&&(n.rotate=o),n}(e,t);for(const o in s)o in i?o in e&&!(o in n)&&(n[o]=i[o]):o in t?n[o]=t[o]:o in e&&(n[o]=e[o]);return n}function f(e,t,n){const o=e.icons,r=e.aliases||Object.create(null);let i={};function c(e){i=a(o[e]||r[e],i)}return c(t),n.forEach(c),a(e,i)}function l(e,t){const n=[];if("object"!=typeof e||"object"!=typeof e.icons)return n;e.not_found instanceof Array&&e.not_found.forEach((e=>{t(e,null),n.push(e)}));const o=function(e,t){const n=e.icons,o=e.aliases||Object.create(null),r=Object.create(null);return(t||Object.keys(n).concat(Object.keys(o))).forEach((function e(t){if(n[t])return r[t]=[];if(!(t in r)){r[t]=null;const n=o[t]&&o[t].parent,i=n&&e(n);i&&(r[t]=[n].concat(i))}return r[t]})),r}(e);for(const r in o){const i=o[r];i&&(t(r,f(e,r,i)),n.push(r))}return n}const u=/^[a-z0-9]+(-[a-z0-9]+)*$/,d=(e,t,n,o="")=>{const r=e.split(":");if("@"===e.slice(0,1)){if(r.length<2||r.length>3)return null;o=r.shift().slice(1)}if(r.length>3||!r.length)return null;if(r.length>1){const e=r.pop(),n=r.pop(),i={provider:r.length>0?r[0]:o,prefix:n,name:e};return t&&!p(i)?null:i}const i=r[0],c=i.split("-");if(c.length>1){const e={provider:o,prefix:c.shift(),name:c.join("-")};return t&&!p(e)?null:e}if(n&&""===o){const e={provider:o,prefix:"",name:i};return t&&!p(e,n)?null:e}return null},p=(e,t)=>!!e&&!(""!==e.provider&&!e.provider.match(u)||!(t&&""===e.prefix||e.prefix.match(u))||!e.name.match(u)),h={provider:"",aliases:{},not_found:{},...r};function g(e,t){for(const n in t)if(n in e&&typeof e[n]!=typeof t[n])return!1;return!0}function m(e){if("object"!=typeof e||null===e)return null;const t=e;if("string"!=typeof t.prefix||!e.icons||"object"!=typeof e.icons)return null;if(!g(e,h))return null;const n=t.icons;for(const r in n){const e=n[r];if(!r.match(u)||"string"!=typeof e.body||!g(e,s))return null}const o=t.aliases||Object.create(null);for(const r in o){const e=o[r],t=e.parent;if(!r.match(u)||"string"!=typeof t||!n[t]&&!o[t]||!g(e,s))return null}return t}const b=Object.create(null);function y(e,t){const n=b[e]||(b[e]=Object.create(null));return n[t]||(n[t]=function(e,t){return{provider:e,prefix:t,icons:Object.create(null),missing:new Set}}(e,t))}function v(e,t){return m(t)?l(t,((t,n)=>{n?e.icons[t]=n:e.missing.add(t)})):[]}let x=!1;function w(e){return"boolean"==typeof e&&(x=e),x}function k(e){const t="string"==typeof e?d(e,!0,x):e;if(t){const e=y(t.provider,t.prefix),n=t.name;return e.icons[n]||(e.missing.has(n)?null:void 0)}}function j(e,t){if("object"!=typeof e)return!1;if("string"!=typeof t&&(t=e.provider||""),x&&!t&&!e.prefix){let t=!1;return m(e)&&(e.prefix="",l(e,((e,n)=>{n&&function(e,t){const n=d(e,!0,x);return!!n&&function(e,t,n){try{if("string"==typeof n.body)return e.icons[t]={...n},!0}catch(o){}return!1}(y(n.provider,n.prefix),n.name,t)}(e,n)&&(t=!0)}))),t}const n=e.prefix;if(!p({provider:t,prefix:n,name:"a"}))return!1;return!!v(y(t,n),e)}const S=Object.freeze({width:null,height:null}),O=Object.freeze({...S,...i}),E=/(-?[0-9.]*[0-9]+[0-9.]*)/g,M=/^-?[0-9.]*[0-9]+[0-9.]*$/g;function C(e,t,n){if(1===t)return e;if(n=n||100,"number"==typeof e)return Math.ceil(e*t*n)/n;if("string"!=typeof e)return e;const o=e.split(E);if(null===o||!o.length)return e;const r=[];let i=o.shift(),c=M.test(i);for(;;){if(c){const e=parseFloat(i);isNaN(e)?r.push(i):r.push(Math.ceil(e*t*n)/n)}else r.push(i);if(i=o.shift(),void 0===i)return r.join("");c=!c}}const T=/\sid="(\S+)"/g,F="IconifyId"+Date.now().toString(16)+(16777216*Math.random()|0).toString(16);let I=0;function L(e,t=F){const n=[];let o;for(;o=T.exec(e);)n.push(o[1]);if(!n.length)return e;const r="suffix"+(16777216*Math.random()|Date.now()).toString(16);return n.forEach((n=>{const o="function"==typeof t?t(n):t+(I++).toString(),i=n.replace(/[.*+?^${}()|[\]\\]/g,"\\$&");e=e.replace(new RegExp('([#;"])('+i+')([")]|\\.[a-z])',"g"),"$1"+o+r+"$3")})),e=e.replace(new RegExp(r,"g"),"")}const z=Object.create(null);function N(e,t){z[e]=t}function R(e){return z[e]||z[""]}function P(e){let t;if("string"==typeof e.resources)t=[e.resources];else if(t=e.resources,!(t instanceof Array&&t.length))return null;return{resources:t,path:e.path||"/",maxURL:e.maxURL||500,rotate:e.rotate||750,timeout:e.timeout||5e3,random:!0===e.random,index:e.index||0,dataAfterTimeout:!1!==e.dataAfterTimeout}}const _=Object.create(null),A=["https://api.simplesvg.com","https://api.unisvg.com"],$=[];for(;A.length>0;)1===A.length||Math.random()>.5?$.push(A.shift()):$.push(A.pop());function D(e,t){const n=P(t);return null!==n&&(_[e]=n,!0)}function U(e){return _[e]}_[""]=P({resources:["https://api.iconify.design"].concat($)});let q=(()=>{let e;try{if(e=fetch,"function"==typeof e)return e}catch(t){}})();const H={prepare:(e,t,n)=>{const o=[],r=function(e,t){const n=U(e);if(!n)return 0;let o;if(n.maxURL){let e=0;n.resources.forEach((t=>{const n=t;e=Math.max(e,n.length)}));const r=t+".json?icons=";o=n.maxURL-e-n.path.length-r.length}else o=0;return o}(e,t),i="icons";let c={type:i,provider:e,prefix:t,icons:[]},s=0;return n.forEach(((n,a)=>{s+=n.length+1,s>=r&&a>0&&(o.push(c),c={type:i,provider:e,prefix:t,icons:[]},s=n.length),c.icons.push(n)})),o.push(c),o},send:(e,t,n)=>{if(!q)return void n("abort",424);let o=function(e){if("string"==typeof e){const t=U(e);if(t)return t.path}return"/"}(t.provider);switch(t.type){case"icons":{const e=t.prefix,n=t.icons.join(",");o+=e+".json?"+new URLSearchParams({icons:n}).toString();break}case"custom":{const e=t.uri;o+="/"===e.slice(0,1)?e.slice(1):e;break}default:return void n("abort",400)}let r=503;q(e+o).then((e=>{const t=e.status;if(200===t)return r=501,e.json();setTimeout((()=>{n(function(e){return 404===e}(t)?"abort":"next",t)}))})).then((e=>{"object"==typeof e&&null!==e?setTimeout((()=>{n("success",e)})):setTimeout((()=>{404===e?n("abort",e):n("next",r)}))})).catch((()=>{n("next",r)}))}};function J(e,t){e.forEach((e=>{const n=e.loaderCallbacks;n&&(e.loaderCallbacks=n.filter((e=>e.id!==t)))}))}let Q=0;var B={resources:[],index:0,timeout:2e3,rotate:750,random:!1,dataAfterTimeout:!1};function W(e,t,n,o){const r=e.resources.length,i=e.random?Math.floor(Math.random()*r):e.index;let c;if(e.random){let t=e.resources.slice(0);for(c=[];t.length>1;){const e=Math.floor(Math.random()*t.length);c.push(t[e]),t=t.slice(0,e).concat(t.slice(e+1))}c=c.concat(t)}else c=e.resources.slice(i).concat(e.resources.slice(0,i));const s=Date.now();let a,f="pending",l=0,u=null,d=[],p=[];function h(){u&&(clearTimeout(u),u=null)}function g(){"pending"===f&&(f="aborted"),h(),d.forEach((e=>{"pending"===e.status&&(e.status="aborted")})),d=[]}function m(e,t){t&&(p=[]),"function"==typeof e&&p.push(e)}function b(){f="failed",p.forEach((e=>{e(void 0,a)}))}function y(){d.forEach((e=>{"pending"===e.status&&(e.status="aborted")})),d=[]}function v(){if("pending"!==f)return;h();const o=c.shift();if(void 0===o)return d.length?void(u=setTimeout((()=>{h(),"pending"===f&&(y(),b())}),e.timeout)):void b();const r={status:"pending",resource:o,callback:(t,n)=>{!function(t,n,o){const r="success"!==n;switch(d=d.filter((e=>e!==t)),f){case"pending":break;case"failed":if(r||!e.dataAfterTimeout)return;break;default:return}if("abort"===n)return a=o,void b();if(r)return a=o,void(d.length||(c.length?v():b()));if(h(),y(),!e.random){const n=e.resources.indexOf(t.resource);-1!==n&&n!==e.index&&(e.index=n)}f="completed",p.forEach((e=>{e(o)}))}(r,t,n)}};d.push(r),l++,u=setTimeout(v,e.rotate),n(o,t,r.callback)}return"function"==typeof o&&p.push(o),setTimeout(v),function(){return{startTime:s,payload:t,status:f,queriesSent:l,queriesPending:d.length,subscribe:m,abort:g}}}function X(e){const t={...B,...e};let n=[];function o(){n=n.filter((e=>"pending"===e().status))}return{query:function(e,r,i){const c=W(t,e,r,((e,t)=>{o(),i&&i(e,t)}));return n.push(c),c},find:function(e){return n.find((t=>e(t)))||null},setIndex:e=>{t.index=e},getIndex:()=>t.index,cleanup:o}}function G(){}const K=Object.create(null);function V(e,t,n){let o,r;if("string"==typeof e){const t=R(e);if(!t)return n(void 0,424),G;r=t.send;const i=function(e){if(!K[e]){const t=U(e);if(!t)return;const n={config:t,redundancy:X(t)};K[e]=n}return K[e]}(e);i&&(o=i.redundancy)}else{const t=P(e);if(t){o=X(t);const n=R(e.resources?e.resources[0]:"");n&&(r=n.send)}}return o&&r?o.query(t,r,n)().abort:(n(void 0,424),G)}const Y="iconify2",Z="iconify",ee=Z+"-count",te=Z+"-version",ne=36e5,oe=168,re=50;function ie(e,t){try{return e.getItem(t)}catch(n){}}function ce(e,t,n){try{return e.setItem(t,n),!0}catch(o){}}function se(e,t){try{e.removeItem(t)}catch(n){}}function ae(e,t){return ce(e,ee,t.toString())}function fe(e){return parseInt(ie(e,ee))||0}const le={local:!0,session:!0},ue={local:new Set,session:new Set};let de=!1;let pe="undefined"==typeof window?{}:window;function he(e){const t=e+"Storage";try{if(pe&&pe[t]&&"number"==typeof pe[t].length)return pe[t]}catch(n){}le[e]=!1}function ge(e,t){const n=he(e);if(!n)return;const o=ie(n,te);if(o!==Y){if(o){const e=fe(n);for(let t=0;t<e;t++)se(n,Z+t.toString())}return ce(n,te,Y),void ae(n,0)}const r=Math.floor(Date.now()/ne)-oe,i=e=>{const o=Z+e.toString(),i=ie(n,o);if("string"==typeof i){try{const n=JSON.parse(i);if("object"==typeof n&&"number"==typeof n.cached&&n.cached>r&&"string"==typeof n.provider&&"object"==typeof n.data&&"string"==typeof n.data.prefix&&t(n,e))return!0}catch(c){}se(n,o)}};let c=fe(n);for(let s=c-1;s>=0;s--)i(s)||(s===c-1?(c--,ae(n,c)):ue[e].add(s))}function me(){if(!de){de=!0;for(const e in le)ge(e,(e=>{const t=e.data,n=y(e.provider,t.prefix);if(!v(n,t).length)return!1;const o=t.lastModified||-1;return n.lastModifiedCached=n.lastModifiedCached?Math.min(n.lastModifiedCached,o):o,!0}))}}function be(e,t){function n(n){let o;if(!le[n]||!(o=he(n)))return;const r=ue[n];let i;if(r.size)r.delete(i=Array.from(r).shift());else if(i=fe(o),i>=re||!ae(o,i+1))return;const c={cached:Math.floor(Date.now()/ne),provider:e.provider,data:t};return ce(o,Z+i.toString(),JSON.stringify(c))}de||me(),t.lastModified&&!function(e,t){const n=e.lastModifiedCached;if(n&&n>=t)return n===t;if(e.lastModifiedCached=t,n)for(const o in le)ge(o,(n=>{const o=n.data;return n.provider!==e.provider||o.prefix!==e.prefix||o.lastModified===t}));return!0}(e,t.lastModified)||Object.keys(t.icons).length&&(t.not_found&&delete(t=Object.assign({},t)).not_found,n("local")||n("session"))}function ye(){}function ve(e){e.iconsLoaderFlag||(e.iconsLoaderFlag=!0,setTimeout((()=>{e.iconsLoaderFlag=!1,function(e){e.pendingCallbacksFlag||(e.pendingCallbacksFlag=!0,setTimeout((()=>{e.pendingCallbacksFlag=!1;const t=e.loaderCallbacks?e.loaderCallbacks.slice(0):[];if(!t.length)return;let n=!1;const o=e.provider,r=e.prefix;t.forEach((t=>{const i=t.icons,c=i.pending.length;i.pending=i.pending.filter((t=>{if(t.prefix!==r)return!0;const c=t.name;if(e.icons[c])i.loaded.push({provider:o,prefix:r,name:c});else{if(!e.missing.has(c))return n=!0,!0;i.missing.push({provider:o,prefix:r,name:c})}return!1})),i.pending.length!==c&&(n||J([e],t.id),t.callback(i.loaded.slice(0),i.missing.slice(0),i.pending.slice(0),t.abort))}))})))}(e)})))}const xe=(e,t)=>{const n=function(e,t=!0,n=!1){const o=[];return e.forEach((e=>{const r="string"==typeof e?d(e,t,n):e;r&&o.push(r)})),o}(e,!0,w()),o=function(e){const t={loaded:[],missing:[],pending:[]},n=Object.create(null);e.sort(((e,t)=>e.provider!==t.provider?e.provider.localeCompare(t.provider):e.prefix!==t.prefix?e.prefix.localeCompare(t.prefix):e.name.localeCompare(t.name)));let o={provider:"",prefix:"",name:""};return e.forEach((e=>{if(o.name===e.name&&o.prefix===e.prefix&&o.provider===e.provider)return;o=e;const r=e.provider,i=e.prefix,c=e.name,s=n[r]||(n[r]=Object.create(null)),a=s[i]||(s[i]=y(r,i));let f;f=c in a.icons?t.loaded:""===i||a.missing.has(c)?t.missing:t.pending;const l={provider:r,prefix:i,name:c};f.push(l)})),t}(n);if(!o.pending.length){let e=!0;return t&&setTimeout((()=>{e&&t(o.loaded,o.missing,o.pending,ye)})),()=>{e=!1}}const r=Object.create(null),i=[];let c,s;return o.pending.forEach((e=>{const{provider:t,prefix:n}=e;if(n===s&&t===c)return;c=t,s=n,i.push(y(t,n));const o=r[t]||(r[t]=Object.create(null));o[n]||(o[n]=[])})),o.pending.forEach((e=>{const{provider:t,prefix:n,name:o}=e,i=y(t,n),c=i.pendingIcons||(i.pendingIcons=new Set);c.has(o)||(c.add(o),r[t][n].push(o))})),i.forEach((e=>{const{provider:t,prefix:n}=e;r[t][n].length&&function(e,t){e.iconsToLoad?e.iconsToLoad=e.iconsToLoad.concat(t).sort():e.iconsToLoad=t,e.iconsQueueFlag||(e.iconsQueueFlag=!0,setTimeout((()=>{e.iconsQueueFlag=!1;const{provider:t,prefix:n}=e,o=e.iconsToLoad;let r;delete e.iconsToLoad,o&&(r=R(t))&&r.prepare(t,n,o).forEach((n=>{V(t,n,(t=>{if("object"!=typeof t)n.icons.forEach((t=>{e.missing.add(t)}));else try{const n=v(e,t);if(!n.length)return;const o=e.pendingIcons;o&&n.forEach((e=>{o.delete(e)})),be(e,t)}catch(o){console.error(o)}ve(e)}))}))})))}(e,r[t][n])})),t?function(e,t,n){const o=Q++,r=J.bind(null,n,o);if(!t.pending.length)return r;const i={id:o,icons:t,callback:e,abort:r};return n.forEach((e=>{(e.loaderCallbacks||(e.loaderCallbacks=[])).push(i)})),r}(t,o,i):ye};const we=/[\s,]+/;function ke(e,t){t.split(we).forEach((t=>{switch(t.trim()){case"horizontal":e.hFlip=!0;break;case"vertical":e.vFlip=!0}}))}function je(e,t=0){const n=e.replace(/^-?[0-9.]*/,"");function o(e){for(;e<0;)e+=4;return e%4}if(""===n){const t=parseInt(e);return isNaN(t)?0:o(t)}if(n!==e){let t=0;switch(n){case"%":t=25;break;case"deg":t=90}if(t){let r=parseFloat(e.slice(0,e.length-n.length));return isNaN(r)?0:(r/=t,r%1==0?o(r):0)}}return t}let Se;function Oe(e){return void 0===Se&&function(){try{Se=window.trustedTypes.createPolicy("iconify",{createHTML:e=>e})}catch(e){Se=null}}(),Se?Se.createHTML(e):e}const Ee={...O,inline:!1},Me={xmlns:"http://www.w3.org/2000/svg",xmlnsXlink:"http://www.w3.org/1999/xlink","aria-hidden":!0,role:"img"},Ce={display:"inline-block"},Te={backgroundColor:"currentColor"},Fe={backgroundColor:"transparent"},Ie={Image:"var(--svg)",Repeat:"no-repeat",Size:"100% 100%"},Le={WebkitMask:Te,mask:Te,background:Fe};for(const $e in Le){const e=Le[$e];for(const t in Ie)e[$e+t]=Ie[t]}const ze={...Ee,inline:!0};function Ne(e){return e+(e.match(/^[-0-9.]+$/)?"px":"")}const Re=(e,t,n)=>{const r=t.inline?ze:Ee,i=function(e,t){const n={...e};for(const o in t){const e=t[o],r=typeof e;o in S?(null===e||e&&("string"===r||"number"===r))&&(n[o]=e):r===typeof n[o]&&(n[o]="rotate"===o?e%4:e)}return n}(r,t),s=t.mode||"svg",a={},f=t.style||{},l={..."svg"===s?Me:{}};if(n){const e=d(n,!1,!0);if(e){const t=["iconify"],n=["provider","prefix"];for(const o of n)e[o]&&t.push("iconify--"+e[o]);l.className=t.join(" ")}}for(let o in t){const e=t[o];if(void 0!==e)switch(o){case"icon":case"style":case"children":case"onLoad":case"mode":case"ssr":break;case"_ref":l.ref=e;break;case"className":l[o]=(l[o]?l[o]+" ":"")+e;break;case"inline":case"hFlip":case"vFlip":i[o]=!0===e||"true"===e||1===e;break;case"flip":"string"==typeof e&&ke(i,e);break;case"color":a.color=e;break;case"rotate":"string"==typeof e?i[o]=je(e):"number"==typeof e&&(i[o]=e);break;case"ariaHidden":case"aria-hidden":!0!==e&&"true"!==e&&delete l["aria-hidden"];break;default:void 0===r[o]&&(l[o]=e)}}const u=function(e,t){const n={...c,...e},o={...O,...t},r={left:n.left,top:n.top,width:n.width,height:n.height};let i=n.body;[n,o].forEach((e=>{const t=[],n=e.hFlip,o=e.vFlip;let c,s=e.rotate;switch(n?o?s+=2:(t.push("translate("+(r.width+r.left).toString()+" "+(0-r.top).toString()+")"),t.push("scale(-1 1)"),r.top=r.left=0):o&&(t.push("translate("+(0-r.left).toString()+" "+(r.height+r.top).toString()+")"),t.push("scale(1 -1)"),r.top=r.left=0),s<0&&(s-=4*Math.floor(s/4)),s%=4,s){case 1:c=r.height/2+r.top,t.unshift("rotate(90 "+c.toString()+" "+c.toString()+")");break;case 2:t.unshift("rotate(180 "+(r.width/2+r.left).toString()+" "+(r.height/2+r.top).toString()+")");break;case 3:c=r.width/2+r.left,t.unshift("rotate(-90 "+c.toString()+" "+c.toString()+")")}s%2==1&&(r.left!==r.top&&(c=r.left,r.left=r.top,r.top=c),r.width!==r.height&&(c=r.width,r.width=r.height,r.height=c)),t.length&&(i=function(e,t,n){const o=function(e,t="defs"){let n="";const o=e.indexOf("<"+t);for(;o>=0;){const r=e.indexOf(">",o),i=e.indexOf("</"+t);if(-1===r||-1===i)break;const c=e.indexOf(">",i);if(-1===c)break;n+=e.slice(r+1,i).trim(),e=e.slice(0,o).trim()+e.slice(c+1)}return{defs:n,content:e}}(e);return r=o.defs,i=t+o.content+n,r?"<defs>"+r+"</defs>"+i:i;var r,i}(i,'<g transform="'+t.join(" ")+'">',"</g>"))}));const s=o.width,a=o.height,f=r.width,l=r.height;let u,d;null===s?(d=null===a?"1em":"auto"===a?l:a,u=C(d,f/l)):(u="auto"===s?f:s,d=null===a?C(u,l/f):"auto"===a?l:a);const p={},h=(e,t)=>{(e=>"unset"===e||"undefined"===e||"none"===e)(t)||(p[e]=t.toString())};h("width",u),h("height",d);const g=[r.left,r.top,f,l];return p.viewBox=g.join(" "),{attributes:p,viewBox:g,body:i}}(e,i),p=u.attributes;if(i.inline&&(a.verticalAlign="-0.125em"),"svg"===s){l.style={...a,...f},Object.assign(l,p);let e=0,n=t.id;return"string"==typeof n&&(n=n.replace(/-/g,"_")),l.dangerouslySetInnerHTML={__html:Oe(L(u.body,n?()=>n+"ID"+e++:"iconifyReact"))},(0,o.createElement)("svg",l)}const{body:h,width:g,height:m}=e,b="mask"===s||"bg"!==s&&-1!==h.indexOf("currentColor"),y=function(e,t){let n=-1===e.indexOf("xlink:")?"":' xmlns:xlink="http://www.w3.org/1999/xlink"';for(const o in t)n+=" "+o+'="'+t[o]+'"';return'<svg xmlns="http://www.w3.org/2000/svg"'+n+">"+e+"</svg>"}(h,{...p,width:g+"",height:m+""});var v;return l.style={...a,"--svg":(v=y,'url("'+function(e){return"data:image/svg+xml,"+function(e){return e.replace(/"/g,"'").replace(/%/g,"%25").replace(/#/g,"%23").replace(/</g,"%3C").replace(/>/g,"%3E").replace(/\s+/g," ")}(e)}(v)+'")'),width:Ne(p.width),height:Ne(p.height),...Ce,...b?Te:Fe,...f},(0,o.createElement)("span",l)};if(w(!0),N("",H),"undefined"!=typeof document&&"undefined"!=typeof window){me();const e=window;if(void 0!==e.IconifyPreload){const t=e.IconifyPreload,n="Invalid IconifyPreload syntax.";"object"==typeof t&&null!==t&&(t instanceof Array?t:[t]).forEach((e=>{try{("object"!=typeof e||null===e||e instanceof Array||"object"!=typeof e.icons||"string"!=typeof e.prefix||!j(e))&&console.error(n)}catch(t){console.error(n)}}))}if(void 0!==e.IconifyProviders){const t=e.IconifyProviders;if("object"==typeof t&&null!==t)for(let e in t){const n="IconifyProviders["+e+"] is invalid.";try{const o=t[e];if("object"!=typeof o||!o||void 0===o.resources)continue;D(e,o)||console.error(n)}catch(Ae){console.error(n)}}}}function Pe(e){const[t,n]=(0,o.useState)(!!e.ssr),[r,i]=(0,o.useState)({});const[s,a]=(0,o.useState)(function(t){if(t){const t=e.icon;if("object"==typeof t)return{name:"",data:t};const n=k(t);if(n)return{name:t,data:n}}return{name:""}}(!!e.ssr));function f(){const e=r.callback;e&&(e(),i({}))}function l(e){if(JSON.stringify(s)!==JSON.stringify(e))return f(),a(e),!0}function u(){var t;const n=e.icon;if("object"==typeof n)return void l({name:"",data:n});const o=k(n);if(l({name:n,data:o}))if(void 0===o){const e=xe([n],u);i({callback:e})}else o&&(null===(t=e.onLoad)||void 0===t||t.call(e,n))}(0,o.useEffect)((()=>(n(!0),f)),[]),(0,o.useEffect)((()=>{t&&u()}),[e.icon,t]);const{name:d,data:p}=s;return p?Re({...c,...p},e,d):e.children?e.children:(0,o.createElement)("span",{})}const _e=(0,o.forwardRef)(((e,t)=>Pe({...e,_ref:t})));(0,o.forwardRef)(((e,t)=>Pe({inline:!0,...e,_ref:t})))},8453:(e,t,n)=>{n.d(t,{R:()=>c,x:()=>s});var o=n(6540);const r={},i=o.createContext(r);function c(e){const t=o.useContext(i);return o.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function s(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:c(e.components),o.createElement(i.Provider,{value:t},e.children)}}}]);