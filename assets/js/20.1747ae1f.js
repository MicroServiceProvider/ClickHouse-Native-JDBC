(window.webpackJsonp=window.webpackJsonp||[]).push([[20],{195:function(e,t,r){"use strict";r.r(t);var a=r(3),n=function(e){e.options.__data__block__={mermaid_1a962850:"sequenceDiagram\nClient -> Server: Open socket Connection\nServer --\x3e Client: Ok, got a new client connection\nNote right of Server: Connection established\nClient -> Server: Send Hello Request\nServer --\x3e Client: Hello response\nNote left of Client: I got server infos\n",mermaid_382ee16c:"sequenceDiagram\nClient -> Server: Send DataRequest Request\nNote right of Server: Oh, a new query just comes, I will handle that query.\nServer --\x3e Client: DataResponse\nNote left of Client: I got response data now\nNote left of Client: I will deserialize them to the ResultSets.\n",mermaid_382ee188:"sequenceDiagram\nClient -> Server: Send insert query to the server (which called by PreparedStatement)\nNote right of Server: Oh, a new prepare insert just comes, I'll look at the table schemas.\nServer --\x3e Client: DataResponse (Empty Block, which is also called sampleBlock)\nNote right of Server: State: Waiting for inserts.\nNote left of Client: I got a block now, and I know the names and types of this table.\nNote left of Client: Write the data to the blocks (when we can `setObject` in JDBC)\nClient -> Server: send a large block by dataRequest\nNote right of Server: A Block just comes, I'll insert them to the table\nClient -> Server: send a empty block to end the inserts\nNote right of Server: A empty block just comes, which means the client finish the inserts.\nNote right of Server: State: Idle.\n"}},s=Object(a.a)({},(function(){var e=this,t=e.$createElement,r=e._self._c||t;return r("ContentSlotsDistributor",{attrs:{"slot-key":e.$parent.slotKey}},[r("h1",{attrs:{id:"clickhouse-客户端-服务端通信协议"}},[r("a",{staticClass:"header-anchor",attrs:{href:"#clickhouse-客户端-服务端通信协议"}},[e._v("#")]),e._v(" ClickHouse 客户端-服务端通信协议")]),e._v(" "),r("h2",{attrs:{id:"建立连接"}},[r("a",{staticClass:"header-anchor",attrs:{href:"#建立连接"}},[e._v("#")]),e._v(" 建立连接")]),e._v(" "),r("Mermaid",{attrs:{id:"mermaid_1a962850",graph:e.$dataBlock.mermaid_1a962850}}),r("h2",{attrs:{id:"发请请求"}},[r("a",{staticClass:"header-anchor",attrs:{href:"#发请请求"}},[e._v("#")]),e._v(" 发请请求")]),e._v(" "),r("ul",[r("li",[r("p",[e._v("There are many kinds of requests/response, the above "),r("code",[e._v("hello")]),e._v(" is one of them.")])]),e._v(" "),r("li",[r("p",[e._v("You can find all the request/response type in "),r("code",[e._v("com.github.housepower.jdbc.protocol")]),e._v(" package.")])])]),e._v(" "),r("h2",{attrs:{id:"查询"}},[r("a",{staticClass:"header-anchor",attrs:{href:"#查询"}},[e._v("#")]),e._v(" 查询")]),e._v(" "),r("p",[e._v("After the connection established and hello request/response, we can send plain sql strings to query the data.")]),e._v(" "),r("Mermaid",{attrs:{id:"mermaid_382ee16c",graph:e.$dataBlock.mermaid_382ee16c}}),r("h2",{attrs:{id:"插入"}},[r("a",{staticClass:"header-anchor",attrs:{href:"#插入"}},[e._v("#")]),e._v(" 插入")]),e._v(" "),r("p",[e._v("The plain sql querys which send query sql to the server, but it's not efficient for batch inserts. ClickHouse provide another type of data request for batch inserts that we can send blocks to the server directly.")]),e._v(" "),r("Mermaid",{attrs:{id:"mermaid_382ee188",graph:e.$dataBlock.mermaid_382ee188}})],1)}),[],!1,null,null,null);"function"==typeof n&&n(s);t.default=s.exports}}]);