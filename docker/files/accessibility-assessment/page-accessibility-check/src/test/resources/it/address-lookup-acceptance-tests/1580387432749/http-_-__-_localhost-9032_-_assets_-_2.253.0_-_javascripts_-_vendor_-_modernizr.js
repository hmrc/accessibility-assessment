!function(e,t,n){function r(e,t){return typeof e===t}function o(e,t){return!!~(""+e).indexOf(t)}function a(){return"function"!=typeof t.createElement?t.createElement(arguments[0]):y?t.createElementNS.call(t,"http://www.w3.org/2000/svg",arguments[0]):t.createElement.apply(t,arguments)}function i(){var e=t.body;return e||((e=a(y?"svg":"body")).fake=!0),e}function s(e,n,r,o){var s,l,c,u,d="modernizr",f=a("div"),p=i();if(parseInt(r,10))for(;r--;)(c=a("div")).id=o?o[r]:d+(r+1),f.appendChild(c);return s=a("style"),s.type="text/css",s.id="s"+d,(p.fake?p:f).appendChild(s),p.appendChild(f),s.styleSheet?s.styleSheet.cssText=e:s.appendChild(t.createTextNode(e)),f.id=d,p.fake&&(p.style.background="",p.style.overflow="hidden",u=g.style.overflow,g.style.overflow="hidden",g.appendChild(p)),l=n(f,e),p.fake?(p.parentNode.removeChild(p),g.style.overflow=u,g.offsetHeight):f.parentNode.removeChild(f),!!l}function l(e){return e.replace(/([A-Z])/g,function(e,t){return"-"+t.toLowerCase()}).replace(/^ms-/,"-ms-")}function c(t,n,r){var o;if("getComputedStyle"in e){o=getComputedStyle.call(e,t,n);var a=e.console;null!==o?r&&(o=o.getPropertyValue(r)):a&&a[a.error?"error":"log"].call(a,"getComputedStyle returning null, its possible modernizr test results are inaccurate")}else o=!n&&t.currentStyle&&t.currentStyle[r];return o}function u(t,r){var o=t.length;if("CSS"in e&&"supports"in e.CSS){for(;o--;)if(e.CSS.supports(l(t[o]),r))return!0;return!1}if("CSSSupportsRule"in e){for(var a=[];o--;)a.push("("+l(t[o])+":"+r+")");return a=a.join(" or "),s("@supports ("+a+") { #modernizr { position: absolute; } }",function(e){return"absolute"==c(e,null,"position")})}return n}function d(e){return e.replace(/([a-z])-([a-z])/g,function(e,t,n){return t+n.toUpperCase()}).replace(/^-/,"")}function f(e,t,i,s){function l(){f&&(delete b.style,delete b.modElem)}if(s=!r(s,"undefined")&&s,!r(i,"undefined")){var c=u(e,i);if(!r(c,"undefined"))return c}for(var f,p,m,h,v,g=["modernizr","tspan","samp"];!b.style&&g.length;)f=!0,b.modElem=a(g.shift()),b.style=b.modElem.style;for(m=e.length,p=0;p<m;p++)if(h=e[p],v=b.style[h],o(h,"-")&&(h=d(h)),b.style[h]!==n){if(s||r(i,"undefined"))return l(),"pfx"!=t||h;try{b.style[h]=i}catch(e){}if(b.style[h]!=v)return l(),"pfx"!=t||h}return l(),!1}var p=[],m={_version:"3.5.0",_config:{classPrefix:"",enableClasses:!0,enableJSClass:!0,usePrefixes:!0},_q:[],on:function(e,t){var n=this;setTimeout(function(){t(n[e])},0)},addTest:function(e,t,n){p.push({name:e,fn:t,options:n})},addAsyncTest:function(e){p.push({name:null,fn:e})}},h=function(){};h.prototype=m,h=new h;var v=[],g=t.documentElement,y="svg"===g.nodeName.toLowerCase();y||function(e,t){function n(e,t){var n=e.createElement("p"),r=e.getElementsByTagName("head")[0]||e.documentElement;return n.innerHTML="x<style>"+t+"</style>",r.insertBefore(n.lastChild,r.firstChild)}function r(){var e=S.elements;return"string"==typeof e?e.split(" "):e}function o(e){var t=b[e[y]];return t||(t={},E++,e[y]=E,b[E]=t),t}function a(e,n,r){if(n||(n=t),m)return n.createElement(e);r||(r=o(n));var a;return!(a=r.cache[e]?r.cache[e].cloneNode():g.test(e)?(r.cache[e]=r.createElem(e)).cloneNode():r.createElem(e)).canHaveChildren||v.test(e)||a.tagUrn?a:r.frag.appendChild(a)}function i(e,t){t.cache||(t.cache={},t.createElem=e.createElement,t.createFrag=e.createDocumentFragment,t.frag=t.createFrag()),e.createElement=function(n){return S.shivMethods?a(n,e,t):t.createElem(n)},e.createDocumentFragment=Function("h,f","return function(){var n=f.cloneNode(),c=n.createElement;h.shivMethods&&("+r().join().replace(/[\w\-:]+/g,function(e){return t.createElem(e),t.frag.createElement(e),'c("'+e+'")'})+");return n}")(S,t.frag)}function s(e){e||(e=t);var r=o(e);return!S.shivCSS||p||r.hasCSS||(r.hasCSS=!!n(e,"article,aside,dialog,figcaption,figure,footer,header,hgroup,main,nav,section{display:block}mark{background:#FF0;color:#000}template{display:none}")),m||i(e,r),e}function l(e){for(var t,n=e.getElementsByTagName("*"),o=n.length,a=RegExp("^(?:"+r().join("|")+")$","i"),i=[];o--;)t=n[o],a.test(t.nodeName)&&i.push(t.applyElement(c(t)));return i}function c(e){for(var t,n=e.attributes,r=n.length,o=e.ownerDocument.createElement(C+":"+e.nodeName);r--;)(t=n[r]).specified&&o.setAttribute(t.nodeName,t.nodeValue);return o.style.cssText=e.style.cssText,o}function u(e){for(var t,n=e.split("{"),o=n.length,a=RegExp("(^|[\\s,>+~])("+r().join("|")+")(?=[[\\s,>+~#.:]|$)","gi"),i="$1"+C+"\\:$2";o--;)(t=n[o]=n[o].split("}"))[t.length-1]=t[t.length-1].replace(a,i),n[o]=t.join("}");return n.join("{")}function d(e){for(var t=e.length;t--;)e[t].removeNode()}function f(e){function t(){clearTimeout(i._removeSheetTimer),r&&r.removeNode(!0),r=null}var r,a,i=o(e),s=e.namespaces,c=e.parentWindow;return!w||e.printShived?e:(void 0===s[C]&&s.add(C),c.attachEvent("onbeforeprint",function(){t();for(var o,i,s,c=e.styleSheets,d=[],f=c.length,p=Array(f);f--;)p[f]=c[f];for(;s=p.pop();)if(!s.disabled&&T.test(s.media)){try{o=s.imports,i=o.length}catch(e){i=0}for(f=0;f<i;f++)p.push(o[f]);try{d.push(s.cssText)}catch(e){}}d=u(d.reverse().join("")),a=l(e),r=n(e,d)}),c.attachEvent("onafterprint",function(){d(a),clearTimeout(i._removeSheetTimer),i._removeSheetTimer=setTimeout(t,500)}),e.printShived=!0,e)}var p,m,h=e.html5||{},v=/^<|^(?:button|map|select|textarea|object|iframe|option|optgroup)$/i,g=/^(?:a|b|code|div|fieldset|h1|h2|h3|h4|h5|h6|i|label|li|ol|p|q|span|strong|style|table|tbody|td|th|tr|ul)$/i,y="_html5shiv",E=0,b={};!function(){try{var e=t.createElement("a");e.innerHTML="<xyz></xyz>",p="hidden"in e,m=1==e.childNodes.length||function(){t.createElement("a");var e=t.createDocumentFragment();return void 0===e.cloneNode||void 0===e.createDocumentFragment||void 0===e.createElement}()}catch(e){p=!0,m=!0}}();var S={elements:h.elements||"abbr article aside audio bdi canvas data datalist details dialog figcaption figure footer header hgroup main mark meter nav output picture progress section summary template time video",version:"3.7.3",shivCSS:!1!==h.shivCSS,supportsUnknownElements:m,shivMethods:!1!==h.shivMethods,type:"default",shivDocument:s,createElement:a,createDocumentFragment:function(e,n){if(e||(e=t),m)return e.createDocumentFragment();for(var a=(n=n||o(e)).frag.cloneNode(),i=0,s=r(),l=s.length;i<l;i++)a.createElement(s[i]);return a},addElements:function(e,t){var n=S.elements;"string"!=typeof n&&(n=n.join(" ")),"string"!=typeof e&&(e=e.join(" ")),S.elements=n+" "+e,s(t)}};e.html5=S,s(t);var T=/^$|\b(?:all|print)\b/,C="html5shiv",w=!m&&function(){var n=t.documentElement;return!(void 0===t.namespaces||void 0===t.parentWindow||void 0===n.applyElement||void 0===n.removeNode||void 0===e.attachEvent)}();S.type+=" print",S.shivPrint=f,f(t),"object"==typeof module&&module.exports&&(module.exports=S)}(void 0!==e?e:this,t);var E={elem:a("modernizr")};h._q.push(function(){delete E.elem});var b={style:E.elem.style};h._q.unshift(function(){delete b.style});m.testProp=function(e,t,r){return f([e],n,t,r)};var S=m._config.usePrefixes?" -webkit- -moz- -o- -ms- ".split(" "):["",""];m._prefixes=S;var T=m.testStyles=s;h.addTest("touchevents",function(){var n;if("ontouchstart"in e||e.DocumentTouch&&t instanceof DocumentTouch)n=!0;else{var r=["@media (",S.join("touch-enabled),("),"heartz",")","{#modernizr{top:9px;position:absolute}}"].join("");T(r,function(e){n=9===e.offsetTop})}return n}),h.addTest("details",function(){var e,t=a("details");return"open"in t&&(T("#modernizr details{display:block}",function(n){n.appendChild(t),t.innerHTML="<summary>a</summary>b",e=t.offsetHeight,t.open=!0,e=e!=t.offsetHeight}),e)}),h.addTest("svg",!!t.createElementNS&&!!t.createElementNS("http://www.w3.org/2000/svg","svg").createSVGRect),h.addTest("video",function(){var e=a("video"),t=!1;try{(t=!!e.canPlayType)&&((t=new Boolean(t)).ogg=e.canPlayType('video/ogg; codecs="theora"').replace(/^no$/,""),t.h264=e.canPlayType('video/mp4; codecs="avc1.42E01E"').replace(/^no$/,""),t.webm=e.canPlayType('video/webm; codecs="vp8, vorbis"').replace(/^no$/,""),t.vp9=e.canPlayType('video/webm; codecs="vp9"').replace(/^no$/,""),t.hls=e.canPlayType('application/x-mpegURL; codecs="avc1.42E01E"').replace(/^no$/,""))}catch(e){}return t}),function(){var e,t,n,o,a,i;for(var s in p)if(p.hasOwnProperty(s)){if(e=[],(t=p[s]).name&&(e.push(t.name.toLowerCase()),t.options&&t.options.aliases&&t.options.aliases.length))for(n=0;n<t.options.aliases.length;n++)e.push(t.options.aliases[n].toLowerCase());for(o=r(t.fn,"function")?t.fn():t.fn,a=0;a<e.length;a++)1===(i=e[a].split(".")).length?h[i[0]]=o:(!h[i[0]]||h[i[0]]instanceof Boolean||(h[i[0]]=new Boolean(h[i[0]])),h[i[0]][i[1]]=o),v.push((o?"":"no-")+i.join("-"))}}(),function(e){var t=g.className,n=h._config.classPrefix||"";if(y&&(t=t.baseVal),h._config.enableJSClass){var r=new RegExp("(^|\\s)"+n+"no-js(\\s|$)");t=t.replace(r,"$1"+n+"js$2")}h._config.enableClasses&&(t+=" "+n+e.join(" "+n),y?g.className.baseVal=t:g.className=t)}(v),delete m.addTest,delete m.addAsyncTest;for(var C=0;C<h._q.length;C++)h._q[C]();e.Modernizr=h}(window,document);