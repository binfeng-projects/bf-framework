[#assign className = defaultName  /]
[#assign classNameFull = defaultNameFull  /]
[#assign pkType = idType  /]

[#assign entity = pojoName  /]
[#assign entityFull = pojoNameFull  /]

[#assign dao = core_interfaceName  /]
[#assign daoFull = core_interfaceNameFull  /]

[#assign daoImpl = daoName  /]
[#assign daoImplFull = daoNameFull  /]

[#assign jooqBaseDao = core_jooqbasedaoName  /]
[#assign jooqBaseDaoFull = core_jooqbasedaoNameFull  /]

[#assign convert = core_convertName  /]
[#assign convertFull = core_convertNameFull  /]

[#assign daoProxy = core_daoproxyName  /]
[#assign daoProxyFull = core_daoproxyNameFull  /]

[#assign coreTest = core_testName  /]
[#assign coreTestFull = core_testNameFull  /]

[#assign serverTest = server_testName  /]
[#assign serverTestFull = server_testNameFull  /]

[#assign serverCtl = server_ctlName  /]
[#assign serverCtlFull = server_ctlNameFull  /]

[#assign serverRpc = server_rpcName  /]
[#assign serverRpcFull = server_rpcNameFull  /]

[#assign clientRpc = client_rpcName  /]
[#assign clientRpcFull = client_rpcNameFull  /]

[#assign clientDto = client_dtoName  /]
[#assign clientDtoFull = client_dtoNameFull  /]
[#--
#t 适配上层的命名规则，保证模版中的命名不用改
--]

