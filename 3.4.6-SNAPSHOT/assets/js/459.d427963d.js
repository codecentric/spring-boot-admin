"use strict";(self.webpackChunksite=self.webpackChunksite||[]).push([[459],{7399:(e,t,n)=>{n.d(t,{In:()=>xe});var o=n(6540);const r=Object.freeze({left:0,top:0,width:16,height:16}),i=Object.freeze({rotate:0,vFlip:!1,hFlip:!1}),c=Object.freeze({...r,...i}),s=Object.freeze({...c,body:"",hidden:!1});function a(e,t){const n=function(e,t){const n={};!e.hFlip!=!t.hFlip&&(n.hFlip=!0),!e.vFlip!=!t.vFlip&&(n.vFlip=!0);const o=((e.rotate||0)+(t.rotate||0))%4;return o&&(n.rotate=o),n}(e,t);for(const o in s)o in i?o in e&&!(o in n)&&(n[o]=i[o]):o in t?n[o]=t[o]:o in e&&(n[o]=e[o]);return n}function l(e,t,n){const o=e.icons,r=e.aliases||Object.create(null);let i={};function c(e){i=a(o[e]||r[e],i)}return c(t),n.forEach(c),a(e,i)}function f(e,t){const n=[];if("object"!=typeof e||"object"!=typeof e.icons)return n;e.not_found instanceof Array&&e.not_found.forEach((e=>{t(e,null),n.push(e)}));const o=function(e,t){const n=e.icons,o=e.aliases||Object.create(null),r=Object.create(null);return Object.keys(n).concat(Object.keys(o)).forEach((function e(t){if(n[t])return r[t]=[];if(!(t in r)){r[t]=null;const n=o[t]&&o[t].parent,i=n&&e(n);i&&(r[t]=[n].concat(i))}return r[t]})),r}(e);for(const r in o){const i=o[r];i&&(t(r,l(e,r,i)),n.push(r))}return n}const u={provider:"",aliases:{},not_found:{},...r};function d(e,t){for(const n in t)if(n in e&&typeof e[n]!=typeof t[n])return!1;return!0}function p(e){if("object"!=typeof e||null===e)return null;const t=e;if("string"!=typeof t.prefix||!e.icons||"object"!=typeof e.icons)return null;if(!d(e,u))return null;const n=t.icons;for(const r in n){const e=n[r];if(!r||"string"!=typeof e.body||!d(e,s))return null}const o=t.aliases||Object.create(null);for(const r in o){const e=o[r],t=e.parent;if(!r||"string"!=typeof t||!n[t]&&!o[t]||!d(e,s))return null}return t}const h=/^[a-z0-9]+(-[a-z0-9]+)*$/,g=(e,t,n,o="")=>{const r=e.split(":");if("@"===e.slice(0,1)){if(r.length<2||r.length>3)return null;o=r.shift().slice(1)}if(r.length>3||!r.length)return null;if(r.length>1){const e=r.pop(),n=r.pop(),i={provider:r.length>0?r[0]:o,prefix:n,name:e};return t&&!m(i)?null:i}const i=r[0],c=i.split("-");if(c.length>1){const e={provider:o,prefix:c.shift(),name:c.join("-")};return t&&!m(e)?null:e}if(n&&""===o){const e={provider:o,prefix:"",name:i};return t&&!m(e,n)?null:e}return null},m=(e,t)=>!!e&&!(!(t&&""===e.prefix||e.prefix)||!e.name),b=Object.create(null);function v(e,t){const n=b[e]||(b[e]=Object.create(null));return n[t]||(n[t]=function(e,t){return{provider:e,prefix:t,icons:Object.create(null),missing:new Set}}(e,t))}function y(e,t){return p(t)?f(t,((t,n)=>{n?e.icons[t]=n:e.missing.add(t)})):[]}let x=!1;function w(e){return"boolean"==typeof e&&(x=e),x}function k(e){const t="string"==typeof e?g(e,!0,x):e;if(t){const e=v(t.provider,t.prefix),n=t.name;return e.icons[n]||(e.missing.has(n)?null:void 0)}}function j(e,t){if("object"!=typeof e)return!1;if("string"!=typeof t&&(t=e.provider||""),x&&!t&&!e.prefix){let t=!1;return p(e)&&(e.prefix="",f(e,((e,n)=>{(function(e,t){const n=g(e,!0,x);if(!n)return!1;const o=v(n.provider,n.prefix);return t?function(e,t,n){try{if("string"==typeof n.body)return e.icons[t]={...n},!0}catch(o){}return!1}(o,n.name,t):(o.missing.add(n.name),!0)})(e,n)&&(t=!0)}))),t}const n=e.prefix;if(!m({prefix:n,name:"a"}))return!1;return!!y(v(t,n),e)}const E=Object.freeze({width:null,height:null}),O=Object.freeze({...E,...i}),S=/(-?[0-9.]*[0-9]+[0-9.]*)/g,T=/^-?[0-9.]*[0-9]+[0-9.]*$/g;function F(e,t,n){if(1===t)return e;if(n=n||100,"number"==typeof e)return Math.ceil(e*t*n)/n;if("string"!=typeof e)return e;const o=e.split(S);if(null===o||!o.length)return e;const r=[];let i=o.shift(),c=T.test(i);for(;;){if(c){const e=parseFloat(i);isNaN(e)?r.push(i):r.push(Math.ceil(e*t*n)/n)}else r.push(i);if(i=o.shift(),void 0===i)return r.join("");c=!c}}const C=/\sid="(\S+)"/g,I="IconifyId"+Date.now().toString(16)+(16777216*Math.random()|0).toString(16);let L=0;function M(e,t=I){const n=[];let o;for(;o=C.exec(e);)n.push(o[1]);if(!n.length)return e;const r="suffix"+(16777216*Math.random()|Date.now()).toString(16);return n.forEach((n=>{const o="function"==typeof t?t(n):t+(L++).toString(),i=n.replace(/[.*+?^${}()|[\]\\]/g,"\\$&");e=e.replace(new RegExp('([#;"])('+i+')([")]|\\.[a-z])',"g"),"$1"+o+r+"$3")})),e=e.replace(new RegExp(r,"g"),"")}const P=Object.create(null);function R(e,t){P[e]=t}function z(e){return P[e]||P[""]}function N(e){let t;if("string"==typeof e.resources)t=[e.resources];else if(t=e.resources,!(t instanceof Array&&t.length))return null;return{resources:t,path:e.path||"/",maxURL:e.maxURL||500,rotate:e.rotate||750,timeout:e.timeout||5e3,random:!0===e.random,index:e.index||0,dataAfterTimeout:!1!==e.dataAfterTimeout}}const A=Object.create(null),_=["https://api.simplesvg.com","https://api.unisvg.com"],$=[];for(;_.length>0;)1===_.length||Math.random()>.5?$.push(_.shift()):$.push(_.pop());function U(e,t){const n=N(t);return null!==n&&(A[e]=n,!0)}function q(e){return A[e]}A[""]=N({resources:["https://api.iconify.design"].concat($)});let D=(()=>{let e;try{if(e=fetch,"function"==typeof e)return e}catch(t){}})();const H={prepare:(e,t,n)=>{const o=[],r=function(e,t){const n=q(e);if(!n)return 0;let o;if(n.maxURL){let e=0;n.resources.forEach((t=>{const n=t;e=Math.max(e,n.length)}));const r=t+".json?icons=";o=n.maxURL-e-n.path.length-r.length}else o=0;return o}(e,t),i="icons";let c={type:i,provider:e,prefix:t,icons:[]},s=0;return n.forEach(((n,a)=>{s+=n.length+1,s>=r&&a>0&&(o.push(c),c={type:i,provider:e,prefix:t,icons:[]},s=n.length),c.icons.push(n)})),o.push(c),o},send:(e,t,n)=>{if(!D)return void n("abort",424);let o=function(e){if("string"==typeof e){const t=q(e);if(t)return t.path}return"/"}(t.provider);switch(t.type){case"icons":{const e=t.prefix,n=t.icons.join(",");o+=e+".json?"+new URLSearchParams({icons:n}).toString();break}case"custom":{const e=t.uri;o+="/"===e.slice(0,1)?e.slice(1):e;break}default:return void n("abort",400)}let r=503;D(e+o).then((e=>{const t=e.status;if(200===t)return r=501,e.json();setTimeout((()=>{n(function(e){return 404===e}(t)?"abort":"next",t)}))})).then((e=>{"object"==typeof e&&null!==e?setTimeout((()=>{n("success",e)})):setTimeout((()=>{404===e?n("abort",e):n("next",r)}))})).catch((()=>{n("next",r)}))}};function Q(e,t){e.forEach((e=>{const n=e.loaderCallbacks;n&&(e.loaderCallbacks=n.filter((e=>e.id!==t)))}))}let B=0;var J={resources:[],index:0,timeout:2e3,rotate:750,random:!1,dataAfterTimeout:!1};function W(e,t,n,o){const r=e.resources.length,i=e.random?Math.floor(Math.random()*r):e.index;let c;if(e.random){let t=e.resources.slice(0);for(c=[];t.length>1;){const e=Math.floor(Math.random()*t.length);c.push(t[e]),t=t.slice(0,e).concat(t.slice(e+1))}c=c.concat(t)}else c=e.resources.slice(i).concat(e.resources.slice(0,i));const s=Date.now();let a,l="pending",f=0,u=null,d=[],p=[];function h(){u&&(clearTimeout(u),u=null)}function g(){"pending"===l&&(l="aborted"),h(),d.forEach((e=>{"pending"===e.status&&(e.status="aborted")})),d=[]}function m(e,t){t&&(p=[]),"function"==typeof e&&p.push(e)}function b(){l="failed",p.forEach((e=>{e(void 0,a)}))}function v(){d.forEach((e=>{"pending"===e.status&&(e.status="aborted")})),d=[]}function y(){if("pending"!==l)return;h();const o=c.shift();if(void 0===o)return d.length?void(u=setTimeout((()=>{h(),"pending"===l&&(v(),b())}),e.timeout)):void b();const r={status:"pending",resource:o,callback:(t,n)=>{!function(t,n,o){const r="success"!==n;switch(d=d.filter((e=>e!==t)),l){case"pending":break;case"failed":if(r||!e.dataAfterTimeout)return;break;default:return}if("abort"===n)return a=o,void b();if(r)return a=o,void(d.length||(c.length?y():b()));if(h(),v(),!e.random){const n=e.resources.indexOf(t.resource);-1!==n&&n!==e.index&&(e.index=n)}l="completed",p.forEach((e=>{e(o)}))}(r,t,n)}};d.push(r),f++,u=setTimeout(y,e.rotate),n(o,t,r.callback)}return"function"==typeof o&&p.push(o),setTimeout(y),function(){return{startTime:s,payload:t,status:l,queriesSent:f,queriesPending:d.length,subscribe:m,abort:g}}}function X(e){const t={...J,...e};let n=[];function o(){n=n.filter((e=>"pending"===e().status))}return{query:function(e,r,i){const c=W(t,e,r,((e,t)=>{o(),i&&i(e,t)}));return n.push(c),c},find:function(e){return n.find((t=>e(t)))||null},setIndex:e=>{t.index=e},getIndex:()=>t.index,cleanup:o}}function G(){}const K=Object.create(null);function V(e,t,n){let o,r;if("string"==typeof e){const t=z(e);if(!t)return n(void 0,424),G;r=t.send;const i=function(e){if(!K[e]){const t=q(e);if(!t)return;const n={config:t,redundancy:X(t)};K[e]=n}return K[e]}(e);i&&(o=i.redundancy)}else{const t=N(e);if(t){o=X(t);const n=z(e.resources?e.resources[0]:"");n&&(r=n.send)}}return o&&r?o.query(t,r,n)().abort:(n(void 0,424),G)}function Y(){}function Z(e){e.iconsLoaderFlag||(e.iconsLoaderFlag=!0,setTimeout((()=>{e.iconsLoaderFlag=!1,function(e){e.pendingCallbacksFlag||(e.pendingCallbacksFlag=!0,setTimeout((()=>{e.pendingCallbacksFlag=!1;const t=e.loaderCallbacks?e.loaderCallbacks.slice(0):[];if(!t.length)return;let n=!1;const o=e.provider,r=e.prefix;t.forEach((t=>{const i=t.icons,c=i.pending.length;i.pending=i.pending.filter((t=>{if(t.prefix!==r)return!0;const c=t.name;if(e.icons[c])i.loaded.push({provider:o,prefix:r,name:c});else{if(!e.missing.has(c))return n=!0,!0;i.missing.push({provider:o,prefix:r,name:c})}return!1})),i.pending.length!==c&&(n||Q([e],t.id),t.callback(i.loaded.slice(0),i.missing.slice(0),i.pending.slice(0),t.abort))}))})))}(e)})))}function ee(e,t,n){function o(){const n=e.pendingIcons;t.forEach((t=>{n&&n.delete(t),e.icons[t]||e.missing.add(t)}))}if(n&&"object"==typeof n)try{if(!y(e,n).length)return void o()}catch(r){console.error(r)}o(),Z(e)}function te(e,t){e instanceof Promise?e.then((e=>{t(e)})).catch((()=>{t(null)})):t(e)}function ne(e,t){e.iconsToLoad?e.iconsToLoad=e.iconsToLoad.concat(t).sort():e.iconsToLoad=t,e.iconsQueueFlag||(e.iconsQueueFlag=!0,setTimeout((()=>{e.iconsQueueFlag=!1;const{provider:t,prefix:n}=e,o=e.iconsToLoad;if(delete e.iconsToLoad,!o||!o.length)return;const r=e.loadIcon;if(e.loadIcons&&(o.length>1||!r))return void te(e.loadIcons(o,n,t),(t=>{ee(e,o,t)}));if(r)return void o.forEach((o=>{te(r(o,n,t),(t=>{ee(e,[o],t?{prefix:n,icons:{[o]:t}}:null)}))}));const{valid:i,invalid:c}=function(e){const t=[],n=[];return e.forEach((e=>{(e.match(h)?t:n).push(e)})),{valid:t,invalid:n}}(o);if(c.length&&ee(e,c,null),!i.length)return;const s=n.match(h)?z(t):null;if(!s)return void ee(e,i,null);s.prepare(t,n,i).forEach((n=>{V(t,n,(t=>{ee(e,n.icons,t)}))}))})))}const oe=(e,t)=>{const n=function(e,t=!0,n=!1){const o=[];return e.forEach((e=>{const r="string"==typeof e?g(e,t,n):e;r&&o.push(r)})),o}(e,!0,w()),o=function(e){const t={loaded:[],missing:[],pending:[]},n=Object.create(null);e.sort(((e,t)=>e.provider!==t.provider?e.provider.localeCompare(t.provider):e.prefix!==t.prefix?e.prefix.localeCompare(t.prefix):e.name.localeCompare(t.name)));let o={provider:"",prefix:"",name:""};return e.forEach((e=>{if(o.name===e.name&&o.prefix===e.prefix&&o.provider===e.provider)return;o=e;const r=e.provider,i=e.prefix,c=e.name,s=n[r]||(n[r]=Object.create(null)),a=s[i]||(s[i]=v(r,i));let l;l=c in a.icons?t.loaded:""===i||a.missing.has(c)?t.missing:t.pending;const f={provider:r,prefix:i,name:c};l.push(f)})),t}(n);if(!o.pending.length){let e=!0;return t&&setTimeout((()=>{e&&t(o.loaded,o.missing,o.pending,Y)})),()=>{e=!1}}const r=Object.create(null),i=[];let c,s;return o.pending.forEach((e=>{const{provider:t,prefix:n}=e;if(n===s&&t===c)return;c=t,s=n,i.push(v(t,n));const o=r[t]||(r[t]=Object.create(null));o[n]||(o[n]=[])})),o.pending.forEach((e=>{const{provider:t,prefix:n,name:o}=e,i=v(t,n),c=i.pendingIcons||(i.pendingIcons=new Set);c.has(o)||(c.add(o),r[t][n].push(o))})),i.forEach((e=>{const t=r[e.provider][e.prefix];t.length&&ne(e,t)})),t?function(e,t,n){const o=B++,r=Q.bind(null,n,o);if(!t.pending.length)return r;const i={id:o,icons:t,callback:e,abort:r};return n.forEach((e=>{(e.loaderCallbacks||(e.loaderCallbacks=[])).push(i)})),r}(t,o,i):Y};const re=/[\s,]+/;function ie(e,t){t.split(re).forEach((t=>{switch(t.trim()){case"horizontal":e.hFlip=!0;break;case"vertical":e.vFlip=!0}}))}function ce(e,t=0){const n=e.replace(/^-?[0-9.]*/,"");function o(e){for(;e<0;)e+=4;return e%4}if(""===n){const t=parseInt(e);return isNaN(t)?0:o(t)}if(n!==e){let t=0;switch(n){case"%":t=25;break;case"deg":t=90}if(t){let r=parseFloat(e.slice(0,e.length-n.length));return isNaN(r)?0:(r/=t,r%1==0?o(r):0)}}return t}let se;function ae(e){return void 0===se&&function(){try{se=window.trustedTypes.createPolicy("iconify",{createHTML:e=>e})}catch(e){se=null}}(),se?se.createHTML(e):e}const le={...O,inline:!1},fe={xmlns:"http://www.w3.org/2000/svg",xmlnsXlink:"http://www.w3.org/1999/xlink","aria-hidden":!0,role:"img"},ue={display:"inline-block"},de={backgroundColor:"currentColor"},pe={backgroundColor:"transparent"},he={Image:"var(--svg)",Repeat:"no-repeat",Size:"100% 100%"},ge={WebkitMask:de,mask:de,background:pe};for(const ke in ge){const e=ge[ke];for(const t in he)e[ke+t]=he[t]}const me={...le,inline:!0};function be(e){return e+(e.match(/^[-0-9.]+$/)?"px":"")}const ve=(e,t,n)=>{const r=t.inline?me:le,i=function(e,t){const n={...e};for(const o in t){const e=t[o],r=typeof e;o in E?(null===e||e&&("string"===r||"number"===r))&&(n[o]=e):r===typeof n[o]&&(n[o]="rotate"===o?e%4:e)}return n}(r,t),s=t.mode||"svg",a={},l=t.style||{},f={..."svg"===s?fe:{}};if(n){const e=g(n,!1,!0);if(e){const t=["iconify"],n=["provider","prefix"];for(const o of n)e[o]&&t.push("iconify--"+e[o]);f.className=t.join(" ")}}for(let o in t){const e=t[o];if(void 0!==e)switch(o){case"icon":case"style":case"children":case"onLoad":case"mode":case"ssr":break;case"_ref":f.ref=e;break;case"className":f[o]=(f[o]?f[o]+" ":"")+e;break;case"inline":case"hFlip":case"vFlip":i[o]=!0===e||"true"===e||1===e;break;case"flip":"string"==typeof e&&ie(i,e);break;case"color":a.color=e;break;case"rotate":"string"==typeof e?i[o]=ce(e):"number"==typeof e&&(i[o]=e);break;case"ariaHidden":case"aria-hidden":!0!==e&&"true"!==e&&delete f["aria-hidden"];break;default:void 0===r[o]&&(f[o]=e)}}const u=function(e,t){const n={...c,...e},o={...O,...t},r={left:n.left,top:n.top,width:n.width,height:n.height};let i=n.body;[n,o].forEach((e=>{const t=[],n=e.hFlip,o=e.vFlip;let c,s=e.rotate;switch(n?o?s+=2:(t.push("translate("+(r.width+r.left).toString()+" "+(0-r.top).toString()+")"),t.push("scale(-1 1)"),r.top=r.left=0):o&&(t.push("translate("+(0-r.left).toString()+" "+(r.height+r.top).toString()+")"),t.push("scale(1 -1)"),r.top=r.left=0),s<0&&(s-=4*Math.floor(s/4)),s%=4,s){case 1:c=r.height/2+r.top,t.unshift("rotate(90 "+c.toString()+" "+c.toString()+")");break;case 2:t.unshift("rotate(180 "+(r.width/2+r.left).toString()+" "+(r.height/2+r.top).toString()+")");break;case 3:c=r.width/2+r.left,t.unshift("rotate(-90 "+c.toString()+" "+c.toString()+")")}s%2==1&&(r.left!==r.top&&(c=r.left,r.left=r.top,r.top=c),r.width!==r.height&&(c=r.width,r.width=r.height,r.height=c)),t.length&&(i=function(e,t,n){const o=function(e,t="defs"){let n="";const o=e.indexOf("<"+t);for(;o>=0;){const r=e.indexOf(">",o),i=e.indexOf("</"+t);if(-1===r||-1===i)break;const c=e.indexOf(">",i);if(-1===c)break;n+=e.slice(r+1,i).trim(),e=e.slice(0,o).trim()+e.slice(c+1)}return{defs:n,content:e}}(e);return r=o.defs,i=t+o.content+n,r?"<defs>"+r+"</defs>"+i:i;var r,i}(i,'<g transform="'+t.join(" ")+'">',"</g>"))}));const s=o.width,a=o.height,l=r.width,f=r.height;let u,d;null===s?(d=null===a?"1em":"auto"===a?f:a,u=F(d,l/f)):(u="auto"===s?l:s,d=null===a?F(u,f/l):"auto"===a?f:a);const p={},h=(e,t)=>{(e=>"unset"===e||"undefined"===e||"none"===e)(t)||(p[e]=t.toString())};h("width",u),h("height",d);const g=[r.left,r.top,l,f];return p.viewBox=g.join(" "),{attributes:p,viewBox:g,body:i}}(e,i),d=u.attributes;if(i.inline&&(a.verticalAlign="-0.125em"),"svg"===s){f.style={...a,...l},Object.assign(f,d);let e=0,n=t.id;return"string"==typeof n&&(n=n.replace(/-/g,"_")),f.dangerouslySetInnerHTML={__html:ae(M(u.body,n?()=>n+"ID"+e++:"iconifyReact"))},(0,o.createElement)("svg",f)}const{body:p,width:h,height:m}=e,b="mask"===s||"bg"!==s&&-1!==p.indexOf("currentColor"),v=function(e,t){let n=-1===e.indexOf("xlink:")?"":' xmlns:xlink="http://www.w3.org/1999/xlink"';for(const o in t)n+=" "+o+'="'+t[o]+'"';return'<svg xmlns="http://www.w3.org/2000/svg"'+n+">"+e+"</svg>"}(p,{...d,width:h+"",height:m+""});var y;return f.style={...a,"--svg":(y=v,'url("'+function(e){return"data:image/svg+xml,"+function(e){return e.replace(/"/g,"'").replace(/%/g,"%25").replace(/#/g,"%23").replace(/</g,"%3C").replace(/>/g,"%3E").replace(/\s+/g," ")}(e)}(y)+'")'),width:be(d.width),height:be(d.height),...ue,...b?de:pe,...l},(0,o.createElement)("span",f)};if(w(!0),R("",H),"undefined"!=typeof document&&"undefined"!=typeof window){const e=window;if(void 0!==e.IconifyPreload){const t=e.IconifyPreload,n="Invalid IconifyPreload syntax.";"object"==typeof t&&null!==t&&(t instanceof Array?t:[t]).forEach((e=>{try{("object"!=typeof e||null===e||e instanceof Array||"object"!=typeof e.icons||"string"!=typeof e.prefix||!j(e))&&console.error(n)}catch(t){console.error(n)}}))}if(void 0!==e.IconifyProviders){const t=e.IconifyProviders;if("object"==typeof t&&null!==t)for(let e in t){const n="IconifyProviders["+e+"] is invalid.";try{const o=t[e];if("object"!=typeof o||!o||void 0===o.resources)continue;U(e,o)||console.error(n)}catch(we){console.error(n)}}}}function ye(e){const[t,n]=(0,o.useState)(!!e.ssr),[r,i]=(0,o.useState)({});const[s,a]=(0,o.useState)(function(t){if(t){const t=e.icon;if("object"==typeof t)return{name:"",data:t};const n=k(t);if(n)return{name:t,data:n}}return{name:""}}(!!e.ssr));function l(){const e=r.callback;e&&(e(),i({}))}function f(e){if(JSON.stringify(s)!==JSON.stringify(e))return l(),a(e),!0}function u(){var t;const n=e.icon;if("object"==typeof n)return void f({name:"",data:n});const o=k(n);if(f({name:n,data:o}))if(void 0===o){const e=oe([n],u);i({callback:e})}else o&&(null===(t=e.onLoad)||void 0===t||t.call(e,n))}(0,o.useEffect)((()=>(n(!0),l)),[]),(0,o.useEffect)((()=>{t&&u()}),[e.icon,t]);const{name:d,data:p}=s;return p?ve({...c,...p},e,d):e.children?e.children:e.fallback?e.fallback:(0,o.createElement)("span",{})}const xe=(0,o.forwardRef)(((e,t)=>ye({...e,_ref:t})));(0,o.forwardRef)(((e,t)=>ye({inline:!0,...e,_ref:t})))},8453:(e,t,n)=>{n.d(t,{R:()=>c,x:()=>s});var o=n(6540);const r={},i=o.createContext(r);function c(e){const t=o.useContext(i);return o.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function s(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:c(e.components),o.createElement(i.Provider,{value:t},e.children)}}}]);