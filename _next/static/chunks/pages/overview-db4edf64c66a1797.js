(self.webpackChunk_N_E=self.webpackChunk_N_E||[]).push([[709],{2414:function(e,t,n){(window.__NEXT_P=window.__NEXT_P||[]).push(["/overview",function(){return n(5344)}])},5344:function(e,t,n){"use strict";n.r(t),n.d(t,{__toc:function(){return d}});var i=n(5893),r=n(2673),s=n(3393),a=n(8426);n(9128);var o=n(2643);let d=[{depth:2,value:"What's Yaci Store?",id:"whats-yaci-store"},{depth:2,value:"Yaci Store application",id:"yaci-store-application"},{depth:2,value:"Yaci Store Modules",id:"yaci-store-modules"},{depth:3,value:"1. Core Modules",id:"1-core-modules"},{depth:3,value:"2. Stores",id:"2-stores"},{depth:3,value:"3. Aggregates",id:"3-aggregates"},{depth:2,value:"Yaci Store Spring Boot Starters",id:"yaci-store-spring-boot-starters"}];function _createMdxContent(e){let t=Object.assign({h2:"h2",p:"p",ol:"ol",li:"li",h3:"h3",strong:"strong",ul:"ul",code:"code"},(0,o.a)(),e.components);return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)(t.h2,{id:"whats-yaci-store",children:"What's Yaci Store?"}),"\n",(0,i.jsx)(t.p,{children:"Yaci Store is a modular Java library for Java developers who are keen on constructing their custom indexer solutions.\nIts architecture ensures that every component within Yaci Store is accessible both as a standalone Java library and a\ncorresponding Spring Boot starter."}),"\n",(0,i.jsx)(t.p,{children:"Developers have the flexibility to select the specific modules that align with their project requirements."}),"\n",(0,i.jsx)(t.h2,{id:"yaci-store-application",children:"Yaci Store application"}),"\n",(0,i.jsx)(t.p,{children:"Yaci Store also offers an out-of-box application that indexes commonly-used data sets using the available modules."}),"\n",(0,i.jsx)(t.h2,{id:"yaci-store-modules",children:"Yaci Store Modules"}),"\n",(0,i.jsx)(t.p,{children:"The modules in Yaci Store are divided into three main categories:"}),"\n",(0,i.jsxs)(t.ol,{children:["\n",(0,i.jsx)(t.li,{children:"Core Modules"}),"\n",(0,i.jsx)(t.li,{children:"Stores"}),"\n",(0,i.jsx)(t.li,{children:"Aggregates"}),"\n"]}),"\n",(0,i.jsx)(t.h3,{id:"1-core-modules",children:"1. Core Modules"}),"\n",(0,i.jsx)(t.p,{children:"Core modules serve a critical purpose. They read data directly from Cardano blockchain and then publish various domain-specific events.\nSince these events are essentially Spring events, developers have the freedom to write their own Spring event listeners.\nThis allows them to tap into these events and process them accordingly. In addition to this, the core module monitors and records\nthe current point in the database."}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Major core modules include:"})}),"\n",(0,i.jsxs)(t.ol,{children:["\n",(0,i.jsx)(t.li,{children:"core"}),"\n",(0,i.jsx)(t.li,{children:"common"}),"\n",(0,i.jsx)(t.li,{children:"events"}),"\n"]}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Events published by core modules:"})}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsx)(t.li,{children:"BlockEvent"}),"\n",(0,i.jsx)(t.li,{children:"BlockHeaderEvent"}),"\n",(0,i.jsx)(t.li,{children:"ByronEbBlockEvent"}),"\n",(0,i.jsx)(t.li,{children:"ByronMainBlockEvent"}),"\n",(0,i.jsx)(t.li,{children:"RollbackEvent"}),"\n",(0,i.jsx)(t.li,{children:"TransactionEvent"}),"\n",(0,i.jsx)(t.li,{children:"MintBurnEvent"}),"\n",(0,i.jsx)(t.li,{children:"ScriptEvent"}),"\n",(0,i.jsx)(t.li,{children:"CertificateEvent"}),"\n",(0,i.jsx)(t.li,{children:"GenesisBlockEvent"}),"\n"]}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Derived Events: Events published by stores"})}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsx)(t.li,{children:"AddressUtxoEvent"}),"\n",(0,i.jsx)(t.li,{children:"TxAssetEvent"}),"\n",(0,i.jsx)(t.li,{children:"TxMetadataEvent"}),"\n",(0,i.jsx)(t.li,{children:"DatumEvent"}),"\n",(0,i.jsx)(t.li,{children:"TxScriptEvent"}),"\n"]}),"\n",(0,i.jsx)(t.h3,{id:"2-stores",children:"2. Stores"}),"\n",(0,i.jsxs)(t.p,{children:['A "',(0,i.jsx)(t.strong,{children:"store"}),'" in Yaci Store is a specialized module designed for a specific data type or use case. Each store has a set of capabilities:']}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsx)(t.li,{children:"Event Listening: Listen to events published by the core module."}),"\n",(0,i.jsx)(t.li,{children:"Data Processing: Processes event data."}),"\n",(0,i.jsx)(t.li,{children:"Data Persistence: Saves processed data to a dedicated persistence store."}),"\n",(0,i.jsx)(t.li,{children:"REST Endpoints: Optionally provides REST endpoints for data retrieval."}),"\n"]}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Available Store Implementations:"})}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"utxo:"})," Focuses on UTxOs, extracting them from transaction data."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"block:"})," Dedicated to handling and storing block data."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"transaction:"})," Takes care of transaction data."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"assets:"})," Manages data related to asset minting and burning."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"metadata:"})," Retrieves and processes metadata events."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"script:"})," Deals with the ScriptEvent, get datums and redeemers."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"staking:"})," Handles from stake address registration to pool registration and more."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"mir:"})," All about Mir data."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"Protocol Params:"})," Fetches protocol parameters from nodes via n2c. There are plans to expand its capabilities to store the history of protocol parameter updates."]}),"\n"]}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Additional Modules:"})}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsx)(t.li,{children:"submit: Enables transaction submissions to nodes, either through n2c or the submit API."}),"\n"]}),"\n",(0,i.jsxs)(t.p,{children:["Each of the mentioned stores is available as a ",(0,i.jsx)(t.code,{children:"Spring Boot starter"}),". This means that integrating a specific store into your\napplication is as straightforward as adding its Spring Boot starter as a dependency."]}),"\n",(0,i.jsx)(t.h3,{id:"3-aggregates",children:"3. Aggregates"}),"\n",(0,i.jsxs)(t.p,{children:['Aggregates are modules that handle different kind of data aggregation. They are responsible for aggregating data from different stores and persisting them in a persistent store.\nCurrently, the only available aggregate is "',(0,i.jsx)(t.strong,{children:"Account"}),'" (experimental) , which provides account balance related data. It depends on the "utxo" store and the event published by utxo store.']}),"\n",(0,i.jsx)(t.h2,{id:"yaci-store-spring-boot-starters",children:"Yaci Store Spring Boot Starters"}),"\n",(0,i.jsx)(t.p,{children:"Each module in Yaci Store is available as a Spring Boot starter. This means that integrating a specific module into your\napplication is as straightforward as adding its Spring Boot starter as a dependency. This ensures that developers can\neasily integrate Yaci Store into their Spring Boot applications with required modules and minimal configuration."}),"\n",(0,i.jsx)(t.p,{children:(0,i.jsx)(t.strong,{children:"Available Starters:"})}),"\n",(0,i.jsxs)(t.ul,{children:["\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-spring-boot-starter :"})," This is the core starter that includes all the core modules. This starter includes libraries\nrequired to fetch data from the blockchain and publish events."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-utxo-spring-boot-starter :"})," This starter includes the UTXO store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-block-spring-boot-starter :"})," This starter includes the block store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-transaction-spring-boot-starter :"})," This starter includes the transaction store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-assets-spring-boot-starter :"})," This starter includes the assets store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-metadata-spring-boot-starter :"})," This starter includes the metadata store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-script-spring-boot-starter :"})," This starter includes the script store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-staking-spring-boot-starter :"})," This starter includes the staking store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-mir-spring-boot-starter :"})," This starter includes the mir store and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-protocolparams-spring-boot-starter :"})," This starter includes the protocol params module and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-account-spring-boot-starter :"})," This starter includes the account aggregate and related configuration including db migration scripts."]}),"\n",(0,i.jsxs)(t.li,{children:[(0,i.jsx)(t.strong,{children:"yaci-store-submit-spring-boot-starter :"})," This starter includes the submit module and related configuration including db migration scripts. Submit module is used to submit transactions to nodes."]}),"\n"]})]})}let l={MDXContent:function(){let e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{},{wrapper:t}=Object.assign({},(0,o.a)(),e.components);return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(_createMdxContent,{...e})}):_createMdxContent(e)},pageOpts:{filePath:"pages/overview.mdx",route:"/overview",timestamp:1715132296e3,pageMap:[{kind:"Meta",data:{overview:"Overview",design:"Design",build_run:"Build & Run",usage:{title:"Usage"},stores:"Stores",about:{title:"About",type:"page"}}},{kind:"MdxPage",name:"about",route:"/about"},{kind:"MdxPage",name:"build_run",route:"/build_run"},{kind:"MdxPage",name:"design",route:"/design"},{kind:"MdxPage",name:"overview",route:"/overview"},{kind:"Folder",name:"stores",route:"/stores",children:[{kind:"MdxPage",name:"overview",route:"/stores/overview"},{kind:"Meta",data:{overview:"Overview"}}]},{kind:"Folder",name:"usage",route:"/usage",children:[{kind:"Meta",data:{"getting-started-as-library":"As Library","getting-started-out-of-box":"Out of Box Indexers","aggregation-app-getting-started":"Aggregation App (Account Balance)"}},{kind:"MdxPage",name:"aggregation-app-getting-started",route:"/usage/aggregation-app-getting-started"},{kind:"MdxPage",name:"getting-started-as-library",route:"/usage/getting-started-as-library"},{kind:"MdxPage",name:"getting-started-out-of-box",route:"/usage/getting-started-out-of-box"}]}],flexsearch:{codeblocks:!0},title:"Overview",headings:d},pageNextRoute:"/overview",nextraLayout:s.ZP,themeConfig:a.Z};t.default=(0,r.j)(l)},8426:function(e,t,n){"use strict";var i=n(5893);n(7294);let r={logo:(0,i.jsx)("span",{children:(0,i.jsx)("b",{children:"Yaci Store"})}),project:{link:"https://github.com/bloxbean/yaci-store"},chat:{link:"https://discord.gg/JtQ54MSw6p"},docsRepositoryBase:"https://github.com/bloxbean/yaci-store/tree/main/docs",footer:{text:"\xa9 2024 BloxBean project"},useNextSeoProps:()=>({titleTemplate:"%s – Yaci Store"}),head:(0,i.jsxs)(i.Fragment,{children:[(0,i.jsx)("meta",{property:"description",content:"Yaci Store - A modular Java library for developers who are keen on building their own custom indexer for Cardano"}),(0,i.jsx)("meta",{property:"og:title",content:"Yaci Store - A modular Java library for developers who are keen on building their own custom indexer for Cardano"}),(0,i.jsx)("meta",{property:"og:description",content:"Yaci Store - A modular Java library for developers who are keen on building their own custom indexer for Cardano"})]})};t.Z=r},5789:function(){}},function(e){e.O(0,[774,890,888,179],function(){return e(e.s=2414)}),_N_E=e.O()}]);