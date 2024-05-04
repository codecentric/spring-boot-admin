export const sbomsResponse = {
  ids: ['application', 'system'],
};

export const applicationSbomResponse = {
  bomFormat: 'CycloneDX',
  specVersion: '1.5',
  serialNumber: 'urn:uuid:9f17e432-9d33-3d31-aee7-bbda6211a526',
  version: 1,
  metadata: {
    timestamp: '2024-04-29T14:17:19Z',
    lifecycles: [
      {
        phase: 'build',
      },
    ],
    tools: [
      {
        vendor: 'OWASP Foundation',
        name: 'CycloneDX Maven plugin',
        version: '2.8.0',
        hashes: [
          {
            alg: 'MD5',
            content: '76ffec6a7ddd46b2b24517411874eb99',
          },
          {
            alg: 'SHA-1',
            content: '5b0d5b41975b53be4799b9621b4af0cfc41d44b6',
          },
          {
            alg: 'SHA-256',
            content:
              '6852aa0f4e42a2db745bab80e384951a6a65b9215d041081d675780999027e81',
          },
          {
            alg: 'SHA-512',
            content:
              '417de20fcdcb11c9713bacbd57290d8e68037fdb4553fd31b8cb08bd760ad52dc65ea88ad4be15844ad3fd5a4d3e440d2f70326f2fe1e63ec78e059c9a883f8d',
          },
          {
            alg: 'SHA-384',
            content:
              '5eb755c6492e7a7385fa9a1e1f4517875bcb834b2df437808a37a2d6f5285df428741762305980315a63fcef1406597d',
          },
          {
            alg: 'SHA3-384',
            content:
              '0fe16a47cf7aab0b22251dafcc39939b68e8f1778093309d8d2060b51a08df445a8b8ed5a9561669faf2e55f907c76d8',
          },
          {
            alg: 'SHA3-256',
            content:
              '3e5a1eb5ab7d0797498862794709ff8eaaa071fe4cc9ec77f52db7e2f97ef487',
          },
          {
            alg: 'SHA3-512',
            content:
              '59281a3e29e76270d7f44b40b5b9f05e55f1ae3ec716d80add806f360940809e3813998ac7c5758043b8e248aed73b86e37dc506cdb4cde03c16bb617d8e5a3a',
          },
        ],
      },
    ],
    component: {
      publisher: 'codecentric AG',
      group: 'de.codecentric',
      name: 'spring-boot-admin-sample-servlet',
      version: '3.2.4-SNAPSHOT',
      description: 'Spring Boot Admin Sample Servlet',
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/de.codecentric/spring-boot-admin-sample-servlet@3.2.4-SNAPSHOT?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/codecentric/spring-boot-admin/spring-boot-admin-dependencies/spring-boot-admin-build/spring-boot-admin-samples/spring-boot-admin-sample-servlet/',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/codecentric/spring-boot-admin/spring-boot-admin-dependencies/spring-boot-admin-build/spring-boot-admin-samples/spring-boot-admin-sample-servlet',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/de.codecentric/spring-boot-admin-sample-servlet@3.2.4-SNAPSHOT?type=jar',
    },
    properties: [
      {
        name: 'maven.goal',
        value: 'makeBom',
      },
      {
        name: 'maven.scopes',
        value: 'compile,provided,runtime,system',
      },
    ],
  },
  components: [
    {
      group: 'de.codecentric',
      name: 'spring-boot-admin-sample-custom-ui',
      version: '3.2.4-SNAPSHOT',
      scope: 'required',
      purl: 'pkg:maven/de.codecentric/spring-boot-admin-sample-custom-ui@3.2.4-SNAPSHOT?type=jar',
      type: 'library',
      'bom-ref':
        'pkg:maven/de.codecentric/spring-boot-admin-sample-custom-ui@3.2.4-SNAPSHOT?type=jar',
    },
    {
      group: 'de.codecentric',
      name: 'spring-boot-admin-starter-server',
      version: '3.2.4-SNAPSHOT',
      scope: 'required',
      purl: 'pkg:maven/de.codecentric/spring-boot-admin-starter-server@3.2.4-SNAPSHOT?type=jar',
      type: 'library',
      'bom-ref':
        'pkg:maven/de.codecentric/spring-boot-admin-starter-server@3.2.4-SNAPSHOT?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter-security',
      version: '3.3.0-RC1',
      description: 'Starter for using Spring Security',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '4dbb08add7a37e1e1f8b9a32cad03125',
        },
        {
          alg: 'SHA-1',
          content: 'a67dc8d84c8a4562ad257d906a140b35bbe69ea3',
        },
        {
          alg: 'SHA-256',
          content:
            '5142a739b5dcc1fcf0ae83cd395b35375aee5a49a0b431579ed6b8c44690a93b',
        },
        {
          alg: 'SHA-512',
          content:
            'b3565a006dcb8edd49d77be4cfb65b94c949599e3b4be3eb3291184ac9392ba78c7c9f8fd4316e0d721b92a59d99ac785536ebc8c49419e0029cc9d6f602acdf',
        },
        {
          alg: 'SHA-384',
          content:
            'bddaf7e20a489e635e28d5511e16bd902ff61eb37f1793bde86f016ec7c5ae18eae07c939af5c7e7edae9bb8fc68911b',
        },
        {
          alg: 'SHA3-384',
          content:
            '630b82ecb8da9c548eab50f1e8ec923eeb6d4f3a5543ed1526eab98da7ab8b993df7687fd1d78c7df62a7b5ff8bf0a73',
        },
        {
          alg: 'SHA3-256',
          content:
            '37c888764bafa9f554b4030adebc6ce342a36b9132eab3e7ffeceb97e3b04973',
        },
        {
          alg: 'SHA3-512',
          content:
            '37660fe7bebb9569d62007fc89292620aeb5b860ce5eed515b5d9e0c7aa386d96b14a258d2d064910910b8c2abcc3ad9a0682d7afca76d3b04e33f53e9840924',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter-security@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter-security@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter',
      version: '3.3.0-RC1',
      description:
        'Core starter, including auto-configuration support, logging and YAML',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'dcf654ba1433f982859b6164500f0cc8',
        },
        {
          alg: 'SHA-1',
          content: '1fa34d1ded58f3464160bb4796164eb97c793acc',
        },
        {
          alg: 'SHA-256',
          content:
            '161cfb61381a1569f0d0bc6f5bd61c945fc1453ae68690911052315a4aeed8fe',
        },
        {
          alg: 'SHA-512',
          content:
            '8d94109934e6283ce27527c3502c78f31076047f5ac557c029f14bc6db14b0ba995ff6ca7484d4ec70d3d92e01c6ce1d7fafff5ae5b382b54ab18073755cc97d',
        },
        {
          alg: 'SHA-384',
          content:
            '0cf7e45ac5e200e21c2552e8efe15f583b5482fadacf72d1726c2e0bedc57bbe759adb754ead2a6e29a693e69d4d6458',
        },
        {
          alg: 'SHA3-384',
          content:
            '6a5de8f018765305adc03a0c659da62e8b24c44e4453848d5cfb0ce1cb7e90bc1924a64aded610815f6d42cd870b6e22',
        },
        {
          alg: 'SHA3-256',
          content:
            '5dd5e08afa4f155445dcd7c39f64e53c92aeca801d0a39e0044d19fcfcb83ba7',
        },
        {
          alg: 'SHA3-512',
          content:
            '932b5627ad1f29802a1854e78ddb35e8fbe5a41884de4943203203640a7dabdd23ace6670204496152b7c92e45441770dcee9b6be5931f9f5eee5341c9fcc5c8',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot',
      version: '3.3.0-RC1',
      description: 'Spring Boot',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '6670303cfb0ff56e5ac1d42a5dc1a159',
        },
        {
          alg: 'SHA-1',
          content: 'b861466d5af7da9954661f20f851405aa5f8f5d0',
        },
        {
          alg: 'SHA-256',
          content:
            'c6197c0ca9cdc8bb1aa47b5194c79d41fb08b4a12db20954faca2b796e395265',
        },
        {
          alg: 'SHA-512',
          content:
            '71932836039ea36ed5c9f00a83b83718083db69806425d49258c3b4c0047d60d7f9d6c2b416cc3fc9f4974496d5bf173fb734f1fc2ca44ba5496ade3edf85a05',
        },
        {
          alg: 'SHA-384',
          content:
            '09b2a5ccea74fea8933f525c919d1294689d1f3df7bc78ef0faff1429cc94edc519b8555acfe03162b677eba17b8cbce',
        },
        {
          alg: 'SHA3-384',
          content:
            '86f7da509f3f00ff3eeba112fd2427000499b850c0bcc28bd8496fd49c5bcf7e4c8b6226e1a1e21a79b43207a06a80a8',
        },
        {
          alg: 'SHA3-256',
          content:
            '59aae76d129902824c07e70e811087b084f74bbd1b651d0cb5befb8c9ef37d61',
        },
        {
          alg: 'SHA3-512',
          content:
            'dcbf37d6c552fd0d2dae949e603f18181b6c58ebfaf3a51da680e44da77c79d470f237b256fbc53defcc651cd6fc319d42615af6752c735427975b3fa3827de6',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-autoconfigure',
      version: '3.3.0-RC1',
      description: 'Spring Boot AutoConfigure',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'c57c22c83a87833977f3a9d60f538580',
        },
        {
          alg: 'SHA-1',
          content: '8f2eac2c0527567fbe3149dcda0d86c56fcf658e',
        },
        {
          alg: 'SHA-256',
          content:
            '0c48949b7ee05da897ccb5f06180bf495372163badd4c089fbdf1b3c1a0437bd',
        },
        {
          alg: 'SHA-512',
          content:
            'ca39d678883d7c78d32eec49df582a2c9e3ce0423e22df33b6a2e19027fd180ed4f3bb3dc814e671c5d149f11fa971e77a064a327c2d4555972c66bc787f415d',
        },
        {
          alg: 'SHA-384',
          content:
            '7394307a140b1344157a016c88d31512ab2794481020b928a62def6a1acc4429028846997d1114182d6381e15ef678d1',
        },
        {
          alg: 'SHA3-384',
          content:
            'be85aa9dd5b4135f5ca46e744cecfd4d7754530dbdabc0baef61fe4dc02fc44eb1f658151fdffe6ba0dcba2b5dbbca3d',
        },
        {
          alg: 'SHA3-256',
          content:
            'b65e1946b8d0d64a949ed174107973227dc00cc14d9b745b09d157472b5ceb02',
        },
        {
          alg: 'SHA3-512',
          content:
            '79d79d1026d80dcdc3b967815a39b792e183666e16e55f2a86429b913e276a82c18b4bb2308786a002a68ffb2b89c706b610399d32c5d66750af949be485fad9',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-autoconfigure@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-autoconfigure@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter-logging',
      version: '3.3.0-RC1',
      description: 'Starter for logging using Logback. Default logging starter',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'e1110a14ce2877746f805cba59913bc5',
        },
        {
          alg: 'SHA-1',
          content: 'df66b2674eca6a51aadcaf89f8a94f7d60f1e56f',
        },
        {
          alg: 'SHA-256',
          content:
            'fba9b83103dafe5885fd22244f33a8afbec8fe5f94f9f09c19889aaeb638cd6e',
        },
        {
          alg: 'SHA-512',
          content:
            '8880d8536b0a322373dcc4496c00f44c3257ad5008e8f886f29ee54bcce4380bb9ba43f9a0d7d610ebe4ecd3117b1fd7fca3513895f14bed48ecabd7d7514aa0',
        },
        {
          alg: 'SHA-384',
          content:
            '9429194771ea17146bb151442bf72bc3e238f382c6cb76aadfede5cfd5fbbe9f1616a6cf5676ceecb07e04373cb3f5cb',
        },
        {
          alg: 'SHA3-384',
          content:
            '4db4bb42dda7fcdafa74e3543f853b4098efde28ff9d09f706932bb94826dbcf66d2afff86b7d526b8e780a292adb193',
        },
        {
          alg: 'SHA3-256',
          content:
            '4ff08b5b2abd9ddf507b5586bda88f7b2bce600143421b7d215f506d85f72345',
        },
        {
          alg: 'SHA3-512',
          content:
            '03a275dec748ce967180e40a31e76050ccdf83826335dc1b430b7576ebf86cc2952deff8b46a276edc60d437f373dbbdca6c3789102f7be5c8a19655fd39bf56',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter-logging@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter-logging@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'QOS.ch',
      group: 'ch.qos.logback',
      name: 'logback-classic',
      version: '1.5.6',
      description: 'logback-classic module',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '83cff9a718cf3449f75d2bda0b9276c6',
        },
        {
          alg: 'SHA-1',
          content: 'afc75d260d838a3bddfb8f207c2805ed7d1b34f9',
        },
        {
          alg: 'SHA-256',
          content:
            '6115c6cac5ed1d9db810d14f2f7f4dd6a9f21f0acbba8016e4daaca2ba0f5eb8',
        },
        {
          alg: 'SHA-512',
          content:
            '9e3e227c0effccfd4938558f374877cda7c08c3abf3960bfcb6c7eb2bfdbb49d163484f7120c176b1eaef56e83d7e8921d8c19394a91c52d5bdbcbef660d3ec1',
        },
        {
          alg: 'SHA-384',
          content:
            '1c311989271a0dce0bcb38177fac80721100ed472fe4ec9746e56223e44dbfdbfbeb5b9c3e6b4ab635a5ca2635066bac',
        },
        {
          alg: 'SHA3-384',
          content:
            '0951ec84907202404fdbfb8c7cf8e4ced6aaf4e6fa8850b75a3692f51403508d2150a2c510744bb007bb31350d9ad2f4',
        },
        {
          alg: 'SHA3-256',
          content:
            '9d4d9f5794f39d2e2e83c8f8bae91636bc674d7d355cc450f239dea2905000cd',
        },
        {
          alg: 'SHA3-512',
          content:
            'a3e1e03e9f3def61d619f86ee1126fc6ffa66a9c60624bf120f9beddbb1095195fbc3fddbc488f5393aea19cd5f4bf5dc47939e2daba15b24aacae392969aa70',
        },
      ],
      licenses: [
        {
          license: {
            id: 'EPL-1.0',
          },
        },
        {
          license: {
            name: 'GNU Lesser General Public License',
            url: 'http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html',
          },
        },
      ],
      purl: 'pkg:maven/ch.qos.logback/logback-classic@1.5.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://logback.qos.ch/logback-classic',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/qos-ch/logback/logback-classic',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/ch.qos.logback/logback-classic@1.5.6?type=jar',
    },
    {
      publisher: 'QOS.ch',
      group: 'ch.qos.logback',
      name: 'logback-core',
      version: '1.5.6',
      description: 'logback-core module',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'd0634e717a5e885c6b7eeb1bcfac5b61',
        },
        {
          alg: 'SHA-1',
          content: '41cbe874701200c5624c19e0ab50d1b88dfcc77d',
        },
        {
          alg: 'SHA-256',
          content:
            '898c7d120199f37e1acc8118d97ab15a4d02b0e72e27ba9f05843cb374e160c6',
        },
        {
          alg: 'SHA-512',
          content:
            '44601eea5e12b2ca4a707cb43a04d863e0c5dedaf690a4d95772de725ea4097dcf4058d6449971253487803fdb6d534f107b2c7f17c7ffca5a4811e9bac71fdf',
        },
        {
          alg: 'SHA-384',
          content:
            'becf9e457234636944263217e9aaa883eb669f5126e02038afe1e74a8e76dc90edd49b41c5d989b30bfda55f955c42f0',
        },
        {
          alg: 'SHA3-384',
          content:
            '8569d0f5bc0bb3ed3b4d4734ae52a88927fa61975c82e2769c18c9a9191659ee91ba095d8fdae7028cfd72455a3d994f',
        },
        {
          alg: 'SHA3-256',
          content:
            'bde00a4a3cc9ab6e6aa4e76c85be1b13cc182a3db0bbc2fb2fe9e6ac2b2af3f3',
        },
        {
          alg: 'SHA3-512',
          content:
            'c092d071084f6b4bab96ba5863fdf1c728b81bb789cc5b5c1ca3f7245303d45f1c59be84a7d06a7590f6362ea5dfe08d52ca108a296e338917282cda63608db1',
        },
      ],
      licenses: [
        {
          license: {
            id: 'EPL-1.0',
          },
        },
        {
          license: {
            name: 'GNU Lesser General Public License',
            url: 'http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html',
          },
        },
      ],
      purl: 'pkg:maven/ch.qos.logback/logback-core@1.5.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://logback.qos.ch/logback-core',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/qos-ch/logback/logback-core',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/ch.qos.logback/logback-core@1.5.6?type=jar',
    },
    {
      publisher: 'The Apache Software Foundation',
      group: 'org.apache.logging.log4j',
      name: 'log4j-to-slf4j',
      version: '2.23.1',
      description: 'The Apache Log4j binding between Log4j 2 API and SLF4J.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'd60143628bb91f9dfa0148c213388b39',
        },
        {
          alg: 'SHA-1',
          content: '425ad1eb8a39904d2830e907a324e956fb456520',
        },
        {
          alg: 'SHA-256',
          content:
            '7937a84055156910234e3b42868f55e68ff4b7becbb6ffd10146f72f5bf54dd5',
        },
        {
          alg: 'SHA-512',
          content:
            '86c4dce96d5a929b3adbf2283f7188660831b02f9b04eee55010d132cb50f5677b7bf30c478b432fa2053eb11dbf6744351ce60271bb5e0da3a3f555ed50ad0c',
        },
        {
          alg: 'SHA-384',
          content:
            '3d1423da6781764d19ea13c447da9ec5b9bccec4603dbd710b8e4f26fc53d3051a4d3082973a6b20b5edc024f2d4b4b4',
        },
        {
          alg: 'SHA3-384',
          content:
            '9c05c76f928c4ce7b1ced6a8642257a9036c7fa66fa9655964bc7e37d98a2443da550b0b62be7d3caa357ca714b6ad3b',
        },
        {
          alg: 'SHA3-256',
          content:
            '71f4969e9e3580f190e3194adc07afec56b676a4de3294600e09570497d8c573',
        },
        {
          alg: 'SHA3-512',
          content:
            '483c0ea25d108c651dd80d0b694e13084ea78d64831dbd4435117c2d612f2c25d6fe5ee2e6cd5acafed65aab475890529e3b0201adf9d7e366d4449737dd6d3b',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
            url: 'https://www.apache.org/licenses/LICENSE-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.logging.log4j/log4j-to-slf4j@2.23.1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://logging.apache.org/log4j/2.x/log4j/log4j-to-slf4j/',
        },
        {
          type: 'build-system',
          url: 'https://github.com/apache/logging-log4j2/actions',
        },
        {
          type: 'distribution',
          url: 'https://logging.apache.org/logging-parent/latest/#distribution',
        },
        {
          type: 'distribution-intake',
          url: 'https://repository.apache.org/service/local/staging/deploy/maven2',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/apache/logging-log4j2/issues',
        },
        {
          type: 'mailing-list',
          url: 'https://lists.apache.org/list.html?log4j-user@logging.apache.org',
        },
        {
          type: 'vcs',
          url: 'https://github.com/apache/logging-log4j2',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.logging.log4j/log4j-to-slf4j@2.23.1?type=jar',
    },
    {
      publisher: 'The Apache Software Foundation',
      group: 'org.apache.logging.log4j',
      name: 'log4j-api',
      version: '2.23.1',
      description: 'The Apache Log4j API',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'bee2e2dcbeeb983bdb6b71c9c3476b6a',
        },
        {
          alg: 'SHA-1',
          content: '9c15c29c526d9c6783049c0a77722693c66706e1',
        },
        {
          alg: 'SHA-256',
          content:
            '92ec1fd36ab3bc09de6198d2d7c0914685c0f7127ea931acc32fd2ecdd82ea89',
        },
        {
          alg: 'SHA-512',
          content:
            '2a296246b0059ff5fe5c26e2ba3f48aa99e38d7658d613fbd02f32c6d4262f93a67525e6cc4d767fa5c2ab0e39e70bb3c0d3966d38ea4f01608588c084af3162',
        },
        {
          alg: 'SHA-384',
          content:
            '3937cb646009763a94b199a0d6c0065441b9914e2b25e3d58db523874ea760276b445ff300015948d3a813217e0ee404',
        },
        {
          alg: 'SHA3-384',
          content:
            '16ea3301ca37fbede2927399209b403066621789c4f1bee531b5153f27b652458900697fb828170d541a5f3b82e77fb7',
        },
        {
          alg: 'SHA3-256',
          content:
            '0a3dfffc0f362b0a86ad0cd8b36da313c7500a8bdecb0ad7e628c2637d933548',
        },
        {
          alg: 'SHA3-512',
          content:
            '2e230994b8cb7442a2073d60f89c27703ffc78b613dec7891bbfa42e91c95ed684b387ed65df3ee48559ccc06e6877462748f7e2ef985082c8db0741feb576a8',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
            url: 'https://www.apache.org/licenses/LICENSE-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.logging.log4j/log4j-api@2.23.1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://logging.apache.org/log4j/2.x/log4j/log4j-api/',
        },
        {
          type: 'build-system',
          url: 'https://github.com/apache/logging-log4j2/actions',
        },
        {
          type: 'distribution',
          url: 'https://logging.apache.org/logging-parent/latest/#distribution',
        },
        {
          type: 'distribution-intake',
          url: 'https://repository.apache.org/service/local/staging/deploy/maven2',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/apache/logging-log4j2/issues',
        },
        {
          type: 'mailing-list',
          url: 'https://lists.apache.org/list.html?log4j-user@logging.apache.org',
        },
        {
          type: 'vcs',
          url: 'https://github.com/apache/logging-log4j2',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.apache.logging.log4j/log4j-api@2.23.1?type=jar',
    },
    {
      publisher: 'QOS.ch',
      group: 'org.slf4j',
      name: 'jul-to-slf4j',
      version: '2.0.13',
      description: 'JUL to SLF4J bridge',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'd44cfe5a86dae2488e228cac617c6f0e',
        },
        {
          alg: 'SHA-1',
          content: 'a3bcd9d9dd50c71ce69f06b1fd05e40fdeff6ba5',
        },
        {
          alg: 'SHA-256',
          content:
            'fa5ed8f23df2158d0d4d5c82f85cae289d36cc3cd7b7497deff5a37b0b7d7878',
        },
        {
          alg: 'SHA-512',
          content:
            '0cdd6a11e82b740ac3b720e916f7abd9f081d2b0aec27962f3c2d0e7693640dc4be7cc055a4a0e64c34b5258db4483d79a7595411fe9c748fc914334e47a9b5c',
        },
        {
          alg: 'SHA-384',
          content:
            '4c425ac29e0f96343aa1e388cd96f2dec2ac5ea18979f5b8e744cc444ace195ce3cc43234810cfba535151255488d3e9',
        },
        {
          alg: 'SHA3-384',
          content:
            'fba337469cb78c6764a598e6fed47dcdeeee1a04da2018c28b7108261c7331a3bd7285fafbf658823cec47377438fb24',
        },
        {
          alg: 'SHA3-256',
          content:
            '6e9d4f6c4b6c3e9ce8757b32f514759761732da3ebad187abbf5aef0b6c584cf',
        },
        {
          alg: 'SHA3-512',
          content:
            '566dff26f42114e7f145d8669b5afe92c8a93f027f8c1b02b5ac03716145e8638efc83a8f1d1739f25c024c365eab34183dda6c80a6f42d882db3c4b5c4e0220',
        },
      ],
      licenses: [
        {
          license: {
            id: 'MIT',
            url: 'https://opensource.org/licenses/MIT',
          },
        },
      ],
      purl: 'pkg:maven/org.slf4j/jul-to-slf4j@2.0.13?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://www.slf4j.org',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/qos-ch/slf4j/slf4j-parent/jul-to-slf4j',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.slf4j/jul-to-slf4j@2.0.13?type=jar',
    },
    {
      publisher: 'Eclipse Foundation',
      group: 'jakarta.annotation',
      name: 'jakarta.annotation-api',
      version: '2.1.1',
      description: 'Jakarta Annotations API',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '5dac2f68e8288d0add4dc92cb161711d',
        },
        {
          alg: 'SHA-1',
          content: '48b9bda22b091b1f48b13af03fe36db3be6e1ae3',
        },
        {
          alg: 'SHA-256',
          content:
            '5f65fdaf424eee2b55e1d882ba9bb376be93fb09b37b808be6e22e8851c909fe',
        },
        {
          alg: 'SHA-512',
          content:
            'eabe8b855b735663684052ec4cc357cc737936fa57cebf144eb09f70b3b6c600db7fa6f1c93a4f36c5994b1b37dad2dfcec87a41448872e69552accfd7f52af6',
        },
        {
          alg: 'SHA-384',
          content:
            '798597a6b80b423844d70609c54b00d725a357031888da7e5c3efd3914d1770be69aa7135de13ddb89a4420a5550e35b',
        },
        {
          alg: 'SHA3-384',
          content:
            '9629b8ca82f61674f5573723bbb3c137060e1442062eb52fa9c90fc8f57ea7d836eb2fb765d160ec8bf300bcb6b820be',
        },
        {
          alg: 'SHA3-256',
          content:
            'f71ffc2a2c2bd1a00dfc00c4be67dbe5f374078bd50d5b24c0b29fbcc6634ecb',
        },
        {
          alg: 'SHA3-512',
          content:
            'aa4e29025a55878db6edb0d984bd3a0633f3af03fa69e1d26c97c87c6d29339714003c96e29ff0a977132ce9c2729d0e27e36e9e245a7488266138239bdba15e',
        },
      ],
      licenses: [
        {
          license: {
            id: 'EPL-2.0',
          },
        },
        {
          license: {
            id: 'GPL-2.0-with-classpath-exception',
          },
        },
      ],
      purl: 'pkg:maven/jakarta.annotation/jakarta.annotation-api@2.1.1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://projects.eclipse.org/projects/ee4j.ca',
        },
        {
          type: 'distribution-intake',
          url: 'https://jakarta.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/eclipse-ee4j/common-annotations-api/issues',
        },
        {
          type: 'mailing-list',
          url: 'https://dev.eclipse.org/mhonarc/lists/ca-dev',
        },
        {
          type: 'vcs',
          url: 'https://github.com/eclipse-ee4j/common-annotations-api',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/jakarta.annotation/jakarta.annotation-api@2.1.1?type=jar',
    },
    {
      group: 'org.yaml',
      name: 'snakeyaml',
      version: '2.2',
      description: 'YAML 1.1 parser and emitter for Java',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'd78aacf5f2de5b52f1a327470efd1ad7',
        },
        {
          alg: 'SHA-1',
          content: '3af797a25458550a16bf89acc8e4ab2b7f2bfce0',
        },
        {
          alg: 'SHA-256',
          content:
            '1467931448a0817696ae2805b7b8b20bfb082652bf9c4efaed528930dc49389b',
        },
        {
          alg: 'SHA-512',
          content:
            '11547e75cc80bee26f532e2598bc6e4ffa802941496dc0d8ce017f1b15e01ebbb80e91ed17d1047916e32bf2fc58da532bc71a1dfe93afccc277a296d86634ba',
        },
        {
          alg: 'SHA-384',
          content:
            'dae0cb1a7ab9ccc75413f46f18ae160e12e91dfef0c17a07ea547a365e9fb422c071aa01579f2a320f15ce6ee4c29038',
        },
        {
          alg: 'SHA3-384',
          content:
            '654b418f330fa02f1111a20c27395ec5c7f463907ae44f60057c94da04f81e815cf1c3959f005026381ef79030049694',
        },
        {
          alg: 'SHA3-256',
          content:
            '2c4deb8d79876b80b210ef72dc5de2b19607e50fbe3abf09a4324576ca0881fc',
        },
        {
          alg: 'SHA3-512',
          content:
            '0d9be5610b2bcb6bb7562ee8bcc0d68f81d3771958ce9299c5e57e8ec952c96906d711587b7f72936328c72fb41687b4f908c4de3070b78cc1f3e257cf4b715e',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.yaml/snakeyaml@2.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://bitbucket.org/snakeyaml/snakeyaml',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://bitbucket.org/snakeyaml/snakeyaml/issues',
        },
        {
          type: 'vcs',
          url: 'https://bitbucket.org/snakeyaml/snakeyaml/src',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.yaml/snakeyaml@2.2?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-aop',
      version: '6.1.6',
      description: 'Spring AOP',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '7dd149c85f55789d005cf7ee5e1bc666',
        },
        {
          alg: 'SHA-1',
          content: '4958f52cb9fcb3adf7e836304550de5431a9347e',
        },
        {
          alg: 'SHA-256',
          content:
            '32ec3db2653d84e5adbb4aa932c8f825d684d6f588b90a4b8674df419e2375c2',
        },
        {
          alg: 'SHA-512',
          content:
            '957dfd69a39d60ca283c2a1f6a08b5ca24d2ad8fb5ef2173b07fbdc6c3b8ee0679aea8f6780e1d426d1d97555f4de27b4c7118183fea0d38b4515207885b0770',
        },
        {
          alg: 'SHA-384',
          content:
            'e9863cbc573cc4a4f990fe6d7b8d288ac358acda6ff4b33d88e15ec50e7910b64a1a1f7297665666ff2858edef852916',
        },
        {
          alg: 'SHA3-384',
          content:
            'f55459c986cf14feead7c0fa71b78893e9eb810875069d5b60623f3c63f761e344e7068a4b805b52422d8a31e72e73d1',
        },
        {
          alg: 'SHA3-256',
          content:
            '922bece712b5b0617966424355a53da7fe1fff5ebd21a512e0b814e210328fd8',
        },
        {
          alg: 'SHA3-512',
          content:
            'c626119f8af6b2cd5c914629d1d29d6d4bb5f14731e2ccb54114a92486ac41d52d8469bb17e457deedebc8ab3928765e10b69e67c42ec2d74b2e5bc4028dc355',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-beans',
      version: '6.1.6',
      description: 'Spring Beans',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '70aeef1e6e39b2a0b6c9afa1bf81d4e8',
        },
        {
          alg: 'SHA-1',
          content: '332d80ff134420db4ebf7614758e6a02a9bd3c41',
        },
        {
          alg: 'SHA-256',
          content:
            'c3040d1418ef964eb78a39204e04b6685bc840bd010818200f286159be983f30',
        },
        {
          alg: 'SHA-512',
          content:
            'c1e905c6e5bc9fa0925b14c81dd1c0c8c987b50ab4aa74de392c5fbba8cacb25f8ead28e4c01715c6719c4483f819e9338411173a11f7cad008219c9fa626f94',
        },
        {
          alg: 'SHA-384',
          content:
            'a6a0a95420b5e3068ad656c6aa14b565b0305244af4f21029590bd6187b68c2962eb33bacbc53358f1bb4df0adee54d0',
        },
        {
          alg: 'SHA3-384',
          content:
            'c85604e1ff89af420e618f8e4710465e82a3604d0fc930a3ce14171ab997f84bd5be4353b96fc4e14d0fb6764325a010',
        },
        {
          alg: 'SHA3-256',
          content:
            '0b8c554ab50fa0204f3d7a7b23f717b8384aafa8dfe0a421e95fd1de0080e3a4',
        },
        {
          alg: 'SHA3-512',
          content:
            '35648bafc0695efcc0c36ac7189f8b3f907a48fd7a683c9f6f77f6679b083a4531cb8dee501e468841a3cda232e68f0976e2ae9cb65f198ccde5c2028d0e56b8',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.security',
      name: 'spring-security-config',
      version: '6.3.0-RC1',
      description: 'Spring Security',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '6ba304080954b3d8111e68de29ac5051',
        },
        {
          alg: 'SHA-1',
          content: 'b57a66e8644efa48846b0777038ff5f4b3e311f3',
        },
        {
          alg: 'SHA-256',
          content:
            '1f345acb23cac48452b0803183026581f83828b477600d4f91c7e9422eafef23',
        },
        {
          alg: 'SHA-512',
          content:
            '17d18e20affc4e785fd4a0d126ba331142f68de719fb6290345efe1212e147ccab9ac4e64a0ebf5f3f5c2da128a633a7d5935c70915db5e8d8a1eec74692afb4',
        },
        {
          alg: 'SHA-384',
          content:
            '72f0a72e7a399bd62508ba2ee588028e8c5f2df40294d64f351dcecfc8e2450818b47eeef83b22c5dba64f20b0d0fb32',
        },
        {
          alg: 'SHA3-384',
          content:
            '3689d2ecc77e0fa6a4a5d7206fda00b56718db7d78a2f7300845390a3728b887a53eafcb7a10213e1aeec3aa150e3323',
        },
        {
          alg: 'SHA3-256',
          content:
            '0c53615a418816c5c44ec36588773ebc302aee9e3fc4ffe909cc654775ea82e7',
        },
        {
          alg: 'SHA3-512',
          content:
            '1a9002e646d8e9a0860b1da43eee6fd6f0bc34fa7adc475869009cd22d363e4e29cb215e22d9d7b3da8e611d0ef01ca264f577dbf5dd44df6712b9851c92806c',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.security/spring-security-config@6.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-security',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-security/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-security',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.security/spring-security-config@6.3.0-RC1?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.security',
      name: 'spring-security-core',
      version: '6.3.0-RC1',
      description: 'Spring Security',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'a21081caf802eff1ae6f7df885d2c746',
        },
        {
          alg: 'SHA-1',
          content: 'a24da460c3ad36fa03c380ba4f5a79e379c4fc88',
        },
        {
          alg: 'SHA-256',
          content:
            '7e9bd0fc450dd1e6f1b02fb7b5cff14204d3171dc86f41301b2a80208c5ac95a',
        },
        {
          alg: 'SHA-512',
          content:
            '71c8cded26e3408fb9e2552c96d9b3ac018169db3fde4d8a65132a36d7e711704cb476e32e70def34f2bd54dbc7d6cff52fb987c37a6e84d6409f512c5763cc5',
        },
        {
          alg: 'SHA-384',
          content:
            '33aab0fd0914443b9fbbcb4ac066007c1f1a6cf2cd0dbb53fc1484c76130d3b014a92726320c7c48149a12481210bcdb',
        },
        {
          alg: 'SHA3-384',
          content:
            '3265d45b944a80891195c96c07770b9fa99a4f8dbaab3ab0c39a0f1faff2bd33b2555ecf40283dbf33949e4397c89736',
        },
        {
          alg: 'SHA3-256',
          content:
            'a836031ffb32131bb88b6abc05281525c4a8797b330abb266ed0e2587d713f2b',
        },
        {
          alg: 'SHA3-512',
          content:
            'd05e3fe14397303c85362b16840a0973e9ae91d27fde9ecd0e042eafac104fa4784d3d71c71e403a04f19e98dce4db0d99d950c28ff1330a3a59c6eb76f5cfde',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.security/spring-security-core@6.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-security',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-security/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-security',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.security/spring-security-core@6.3.0-RC1?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.security',
      name: 'spring-security-web',
      version: '6.3.0-RC1',
      description: 'Spring Security',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'e6bafd90bc08d472ccccc904f91f336f',
        },
        {
          alg: 'SHA-1',
          content: '7ab833ae6faad0a6ae971c8e919ed80a8fb8bf9c',
        },
        {
          alg: 'SHA-256',
          content:
            '0d71b6a6c2fbdfcdab3b1e8d0b7e67d2313bbe807012ba6f88f8f4245d7c2e53',
        },
        {
          alg: 'SHA-512',
          content:
            '65534406625518c123fd61e4e6543aa732010ea552879a6fa39997941a2913cc58a81b5fa6bc0d2eb825198719d890960639c9079586f11e7febaa775a98efcd',
        },
        {
          alg: 'SHA-384',
          content:
            '31dd5f20a0225419cd0378d7e3a0823bdeb6ebe801a186eeb7bea82eb3f0eeb34252ac3b48ba9dafb498499d80b52762',
        },
        {
          alg: 'SHA3-384',
          content:
            '6d4398f5cea31a754bf916082df2d946d4b3db1495acbfc64c9f5a53a8653dba8d28bcdc88dbf896bf4efc4b47f3ec02',
        },
        {
          alg: 'SHA3-256',
          content:
            '09082e1b2b1238f8a8f4e6a2ce343c1fd5aaeeba3acbb97fcefb3aeb64a0e75f',
        },
        {
          alg: 'SHA3-512',
          content:
            '391534ae0377951c76099f264667655748480a9b1afcf5febbea98f786b6dffe4a88753b697bf08d4db4e49b7416278dd4e469313202985e4cf59305bc55f6c8',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.security/spring-security-web@6.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-security',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-security/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-security',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.security/spring-security-web@6.3.0-RC1?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-expression',
      version: '6.1.6',
      description: 'Spring Expression Language (SpEL)',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '80acfe831814a3712ae046de5baa2fe9',
        },
        {
          alg: 'SHA-1',
          content: '9c3d7f0e17a919a4ea9f087e4e2140ad39776bc8',
        },
        {
          alg: 'SHA-256',
          content:
            '6e53929ab7abda1a43b7d81cefcc441d187eb41aab493d2f61cc6161512c2d97',
        },
        {
          alg: 'SHA-512',
          content:
            '96c4712e5673fb30c55aea7cc7a5d29027780f71b7c0db8d539d95ff7a371cbe16013f59dda4163f5ceda2d09897b5486871cdb3bb22d11fa4858aad0c5aa8b2',
        },
        {
          alg: 'SHA-384',
          content:
            'da97153d3e4d5665240206d2d93ac2b2edef8be11fe1da0504a41345d58fe3072779922b283c28f62865453269a0b488',
        },
        {
          alg: 'SHA3-384',
          content:
            'ea503cc1a439b0eef0dbf2e131eb3a29ce2f8414afe515211ad08fb233089c051e2a2dd1fd0dfa8ff5d6d5210ee27673',
        },
        {
          alg: 'SHA3-256',
          content:
            'e28bb3971e4e14716bac8c0ff013d2ac3aa66feb357569e0b746d4752d82d175',
        },
        {
          alg: 'SHA3-512',
          content:
            '3de81987e28908779b9d7d5714b2c98b905bd045724452c8b84c1814d2d3f0d0ba086cca6564476db41f200182e26332e45a4b278a78fd1b545d1a02a235ca93',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter-web',
      version: '3.3.0-RC1',
      description:
        'Starter for building web, including RESTful, applications using Spring MVC. Uses Tomcat as the default embedded container',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '45bcad7dddc72b1cb3f89ec71022ed39',
        },
        {
          alg: 'SHA-1',
          content: '8be23edf4e71e1d631d8c406e08d06ad9744e7fe',
        },
        {
          alg: 'SHA-256',
          content:
            'e589ae916b15a648f3ad157d669b8d509ad4c128832adf71c31760b46235c3aa',
        },
        {
          alg: 'SHA-512',
          content:
            '846d72082be7e8fd2062769604cae15f92b94a274b9056bd74e6303c20d5531c427148954a68bc0479e4b126e970eb03545c5acbf229bce90fda99a7cfdb9414',
        },
        {
          alg: 'SHA-384',
          content:
            'd3ade9fdbeb94be500f41ba447f5d5854af1bd0135297fca735b8f085ae2513668da1a366e5c9e1d236bf9b0328e73fe',
        },
        {
          alg: 'SHA3-384',
          content:
            '5a03502b89c530e1399618afc2f0b6d619c7cf1a2a59d55d1148bdec518ebb3e85bb92f8bc4552a7db991083be8ac8df',
        },
        {
          alg: 'SHA3-256',
          content:
            '20570b3c35f0f5a7126d21fa4a8ddb0ec42f94834f6866ce94925d43931ee85b',
        },
        {
          alg: 'SHA3-512',
          content:
            '74ba8db1c56f0f36314ccfb5399395988450151f6b9cb4131506bcbb0038aa05445db441a96afe5c1e1131fde5e1c299d09ca55758f805bc0fa39f438d33713b',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter-web@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter-web@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter-json',
      version: '3.3.0-RC1',
      description: 'Starter for reading and writing json',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '1eda6c496208f03d0cf14805de435376',
        },
        {
          alg: 'SHA-1',
          content: 'c063d6d99f359b1e16596dee791800d51298b27f',
        },
        {
          alg: 'SHA-256',
          content:
            '1065123cf78e26eb8c94e1cb145f045b9fc35bfd97fc2dac947fb62aecbf15e8',
        },
        {
          alg: 'SHA-512',
          content:
            'e1019534961d270d89217180e761c86330c3f85412168ae084cdd1ae25139e6a558be89002ffd263a99ffb5ddb3074efd921f1f5755f35d6cb47cdd1ccdfebe9',
        },
        {
          alg: 'SHA-384',
          content:
            '74ceda16372f1eb59c16e3a26d0887d2493f148f0ac9601f6636354fb62def879461a72f17d5af62cc365a7d64201446',
        },
        {
          alg: 'SHA3-384',
          content:
            'e7e34d626727b3d4cef1599d49f71e3930dfba60abb9b8b33caabc38c962f4f9f3694b759e0854769a624ce5c4654db8',
        },
        {
          alg: 'SHA3-256',
          content:
            'ac8220d453e7752437d04bffb7b0750567e99860f1d93f2fbe8a3c50798df24e',
        },
        {
          alg: 'SHA3-512',
          content:
            'dfecf210a3b18c555b3e6e8b2287a91c6c0c7771a430a31a72c798f8a867c5601e0eeeb8d2bec17f71ee59711c5534b41d7c6b03eda280ae311294636da8e26a',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter-json@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter-json@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'FasterXML',
      group: 'com.fasterxml.jackson.datatype',
      name: 'jackson-datatype-jdk8',
      version: '2.17.0',
      description:
        'Add-on module for Jackson (https://github.com/FasterXML/jackson) to support JDK 8 data types.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '65e3b1936136e16cefbe9059c01c4505',
        },
        {
          alg: 'SHA-1',
          content: '95519a116d909faec29da76cf6b944b4a84c2c26',
        },
        {
          alg: 'SHA-256',
          content:
            'b090239968a0ae5a172472f4014dbd97133af9426d91bf4805a6ba5fd90d80f1',
        },
        {
          alg: 'SHA-512',
          content:
            '6d7ee0d139fd9f7c24f14cb4bf231c1d9070c785d607b9a7be2f46297985ee8a7f184f9bf0b3a150d6b4a168175352cf8479c0e411342393af6bb259fdf0ec42',
        },
        {
          alg: 'SHA-384',
          content:
            'd734ba8f8892dd41f63a2faec072cd3b57abf6a8e461c3e04880c285fc13103b50adbade060b387659a49f8d380f4b9c',
        },
        {
          alg: 'SHA3-384',
          content:
            'bb64b906356ef4839cd988be6de66eb7fe1f89e6e6fb1cee3f11097eab26532dbdd791cf99ebf4f9ebc6a3ff975183d7',
        },
        {
          alg: 'SHA3-256',
          content:
            '4ae3e4ad6652e7c2c363ca3d9e6c41871d31531aac7f2a4f50b8d62bff4b8b94',
        },
        {
          alg: 'SHA3-512',
          content:
            'b368861aa6108fc6cc6138863d901d9aca49b16baebeb20db8df7e4451f971f1debae8751c4df27c83565d8e6e7dea21a9209b1c9c07a535b888bb965492ac56',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8@2.17.0?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/FasterXML/jackson-modules-java8/jackson-datatype-jdk8',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/FasterXML/jackson-modules-java8/issues',
        },
        {
          type: 'vcs',
          url: 'http://github.com/FasterXML/jackson-modules-java8/jackson-datatype-jdk8',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8@2.17.0?type=jar',
    },
    {
      publisher: 'FasterXML',
      group: 'com.fasterxml.jackson.datatype',
      name: 'jackson-datatype-jsr310',
      version: '2.17.0',
      description:
        'Add-on module to support JSR-310 (Java 8 Date & Time API) data types.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'b60f65312afa00f61e0950c3b5fbff88',
        },
        {
          alg: 'SHA-1',
          content: '3fab507bba9d477e52ed2302dc3ddbd23cbae339',
        },
        {
          alg: 'SHA-256',
          content:
            '94ea2f224e36632c02db1e668127c3018cacb859afc15ddf6f4c585917a93396',
        },
        {
          alg: 'SHA-512',
          content:
            'bda1467594001aa22d7d622a5dcbb27a8aea54427d6e77dd7c03fb34ec8f4051b976f92c425d047045a0a1f48e23853b81d01a6a25ab0bf9fd479c05e91b5594',
        },
        {
          alg: 'SHA-384',
          content:
            'a94642eed5de82b126672562f03c00e8e1668b8a0df388b8f52e19cfa79e5d2665f2160737026acd1c5d1d7fb7bf2423',
        },
        {
          alg: 'SHA3-384',
          content:
            'f6ca2b5923378c65b91e9f6b5a7c8268f1c19413cb3803355272dc63a7092ae5c453a84b7041d83423dae4fec96f11da',
        },
        {
          alg: 'SHA3-256',
          content:
            '8703a4132f8ae6f48e37eb55da1bacff6c7e098d5e75c93cf19d72080b5afad7',
        },
        {
          alg: 'SHA3-512',
          content:
            '69cc3cfccc7f130cdb67a14e2448c288cf8917e599c046fe9427176135f8e337706032dc313c06ab3ae8548f6a83ab60f6a79d9ec1bc753dc9606302f76e0aef',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310@2.17.0?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/FasterXML/jackson-modules-java8/jackson-datatype-jsr310',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/FasterXML/jackson-modules-java8/issues',
        },
        {
          type: 'vcs',
          url: 'http://github.com/FasterXML/jackson-modules-java8/jackson-datatype-jsr310',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310@2.17.0?type=jar',
    },
    {
      publisher: 'FasterXML',
      group: 'com.fasterxml.jackson.module',
      name: 'jackson-module-parameter-names',
      version: '2.17.0',
      description:
        'Add-on module for Jackson (https://github.com/FasterXML/jackson) to support introspection of method/constructor parameter names, without having to add explicit property name annotation.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '9f22e80510b61baa23689159ec2293cd',
        },
        {
          alg: 'SHA-1',
          content: '59340d6d12c15bcc465a91a4b9a2a93a920c4212',
        },
        {
          alg: 'SHA-256',
          content:
            '1fd79421bb95c74cc4c44c1ae4910e9253f255f248d34c3f9e5b2abeb2145b6a',
        },
        {
          alg: 'SHA-512',
          content:
            'c6d6efabc8e7212ecfe5c3c77b1dd72b8bdae0aa62f08b2a6179aec1bfb56e910240933db0a2bbf62fdc5bfb54ea52b709327e1e1b472c34a1615cdf0c2a350a',
        },
        {
          alg: 'SHA-384',
          content:
            'bb1466be83197443274697476b338f2878325651e8a6799cfd0f491ca3764713dec2b321a22f61346d256d859498792a',
        },
        {
          alg: 'SHA3-384',
          content:
            'c39bfb70ed88a7a3f45697491fb7e4b3da7bd906008ea00f80b113bc6ebb667fc0982388c6964512bfbf85591354d207',
        },
        {
          alg: 'SHA3-256',
          content:
            'cb3a1f00581b629b760e5af30bb0343c09c80d8eb98e409eaa31555a50782af1',
        },
        {
          alg: 'SHA3-512',
          content:
            'd93aea5c736b4f2062e1175c203aa641443824f77923a8a8a4f65a47cb47cb1aab079c06c2f460ad85d908a51e008d1db45d3947866d401e4f20ef63a4aa1882',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/com.fasterxml.jackson.module/jackson-module-parameter-names@2.17.0?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/FasterXML/jackson-modules-java8/jackson-module-parameter-names',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/FasterXML/jackson-modules-java8/issues',
        },
        {
          type: 'vcs',
          url: 'http://github.com/FasterXML/jackson-modules-java8/jackson-module-parameter-names',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/com.fasterxml.jackson.module/jackson-module-parameter-names@2.17.0?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter-tomcat',
      version: '3.3.0-RC1',
      description:
        'Starter for using Tomcat as the embedded servlet container. Default servlet container starter used by spring-boot-starter-web',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'aa98610a3e8b6a74c67467bbbef82875',
        },
        {
          alg: 'SHA-1',
          content: 'cda596da92cdf96bbb042f6c7c93a6d803c3a6d9',
        },
        {
          alg: 'SHA-256',
          content:
            '7a08fb133c7ce64d845fbe3c602757fcb482bc71f90198990af7ed5f452c9a10',
        },
        {
          alg: 'SHA-512',
          content:
            '37c5ecbb41156a72db314065f3473fbc2c72a87b1d5afe114bfba446c66d64c9b9f9e8013bdc141cb1c0f1bcc5a56908620566cbed6c3650f4c18b775e8f2e5c',
        },
        {
          alg: 'SHA-384',
          content:
            '848b8eed45c458edc89dee5faa8219ecedbe5abf78be3094a0955caa013db5409c33ff14960e044c8fe2bd4d57cf2323',
        },
        {
          alg: 'SHA3-384',
          content:
            '2d05d05a755042668cff311be9391c53333695936094cb958cc01be39e767383e7f2402b12a9bc5540b49119e496ab0f',
        },
        {
          alg: 'SHA3-256',
          content:
            '2453b3c286f8e1e7a9e72c596fa80884bc5047b0a7a869ed31dab3583da54593',
        },
        {
          alg: 'SHA3-512',
          content:
            'd746199f73100208ebfafb6094ab7a2548d5554f89cd16747b69ec200cfca9582eebfc7d93f74c1582fb8b8cd35c0aa13e2f3cc67da79c79c6e78614eae43939',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter-tomcat@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter-tomcat@3.3.0-RC1?type=jar',
    },
    {
      group: 'org.apache.tomcat.embed',
      name: 'tomcat-embed-core',
      version: '10.1.20',
      description: 'Core Tomcat implementation',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '27154be1577cf6e837e8537d359d5da1',
        },
        {
          alg: 'SHA-1',
          content: 'ba0dc784e12086f83d8e1d5a10443b166abf5780',
        },
        {
          alg: 'SHA-256',
          content:
            'd4b04888ede3a3232dc1798f3ae2ed7265f9b0dcf631b4bf16f50b7ed90ba361',
        },
        {
          alg: 'SHA-512',
          content:
            '2b0cf032cbe0195e7621b4e0e97766382cefb5b4185eb89ec1818823c9ced75e94a321e9451f929784734d32b2ddc93b75a9ba635d88d3d3a26a349b2921f098',
        },
        {
          alg: 'SHA-384',
          content:
            'f64c3a561b496481ba1c26307f4b44f5f0742f954fbda9528bcd0fcb9385b86c6fb0107fceb319218a2cd842e492ffb0',
        },
        {
          alg: 'SHA3-384',
          content:
            'ce4860079353fcbe5ba07028179ee84272118233d94a91f78a8bb5b85bf277788221d52bff0206ba778b60dd7146250e',
        },
        {
          alg: 'SHA3-256',
          content:
            '265a4f17f7c3854f907d81905780a5a3e704a96b05e7fec5b9ee4c0a2ccd2f39',
        },
        {
          alg: 'SHA3-512',
          content:
            '60e4b7f62dcc53d7fce264e2f56eae21eeb1442076b20da6a2617e84657726625e12f10223c20127509cc8a7ecd95da338989e530cb09e315c8cd7846006a23d',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@10.1.20?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://tomcat.apache.org/',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@10.1.20?type=jar',
    },
    {
      group: 'org.apache.tomcat.embed',
      name: 'tomcat-embed-el',
      version: '10.1.20',
      description: 'Core Tomcat implementation',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '818fdd8c396fedf7117842cd10169d23',
        },
        {
          alg: 'SHA-1',
          content: 'cc1a42b8228699e92c8eba0187eccf54bf892802',
        },
        {
          alg: 'SHA-256',
          content:
            'cac3dd6fa016dd85f5751438274c2f35955ce3024fec59d6d2fb0fe7c847c2d2',
        },
        {
          alg: 'SHA-512',
          content:
            'd17c25d7884a9286843eb809dbff11212dea47946f57d41eae48af2c89f20c21426680b1ba962e2260a0e5fbd5dc6543ff4d90df574a1be12789f5a42eb2ac96',
        },
        {
          alg: 'SHA-384',
          content:
            '20f108b7e2fa1d03df021578cbaefb7f41dadd91cbc2a4c8c0f9ab149fa89e040bc7242c46bf6528d9fe119bce979273',
        },
        {
          alg: 'SHA3-384',
          content:
            '8824efde0b5c6f5c619f2e96f1c4b2e37975d05f77bbc9ea51389f5be31869257251d098b60c26ddf5099e5c02762a6a',
        },
        {
          alg: 'SHA3-256',
          content:
            '8b9caed961e5b913bad0bb6b73c98bd8b24670eaa1fa9915eb79c5b57686f8a2',
        },
        {
          alg: 'SHA3-512',
          content:
            'e444662720754ad71ac02b03a6e943985fa6ee671c2e9bb5042e465fd03f479860a791994af3d195838f66db9859fae1319910a3e8969d59b862bcd94567bb12',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.tomcat.embed/tomcat-embed-el@10.1.20?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://tomcat.apache.org/',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-el@10.1.20?type=jar',
    },
    {
      group: 'org.apache.tomcat.embed',
      name: 'tomcat-embed-websocket',
      version: '10.1.20',
      description: 'Core Tomcat implementation',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'fca3c1e1675701e2dfb01f4e3b8bd2c3',
        },
        {
          alg: 'SHA-1',
          content: '21502adffaf9e6e4bc2b63a557e348d7f6c0faf7',
        },
        {
          alg: 'SHA-256',
          content:
            'e2ec1b5f17c8ae01dcf9b487221f50217c9a03293c0fa98eecdcecd702b4617a',
        },
        {
          alg: 'SHA-512',
          content:
            'a9bd9b831058a46faa808e6aa5625a4b3a43c287de5d7be96b831f57f2cfa08dbca357b1a3edf54f94555e2d9e526ae69dcb172c1cb5fd713c67f50230ee5f03',
        },
        {
          alg: 'SHA-384',
          content:
            '7eb531e48659fe2c79a27087fb62636946c1b06f2996f146791c2d7160e94735da031112c6b76e536ecf4e47c06a74a2',
        },
        {
          alg: 'SHA3-384',
          content:
            '9b62ab366557ac14e52d31dcb81d242ec7c577c9db3ca5203718784d11cea74604be66b8ae88e69ff4927d3504695d82',
        },
        {
          alg: 'SHA3-256',
          content:
            '186766a966f4f58833760027095108ba435094664417d3ea93e77e11bbfa753a',
        },
        {
          alg: 'SHA3-512',
          content:
            '0cc6608af58570bbbe8f86b9a24a5a2c1eb1f7953a8e96ed0389f24b4c81327e5e3973c39d95032d3b36fc8b10ea9ac032117b7a5a1fabd97dcc9c4a7ab9e784',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.tomcat.embed/tomcat-embed-websocket@10.1.20?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://tomcat.apache.org/',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-websocket@10.1.20?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-web',
      version: '6.1.6',
      description: 'Spring Web',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'bea30302bcb6ef493a8123e4a40ae6a2',
        },
        {
          alg: 'SHA-1',
          content: '49a32e3497fe39550da3b280bda5d9933ae2d51d',
        },
        {
          alg: 'SHA-256',
          content:
            '0f33f5530ef848063958b4b437e3df3119c04a92aea58f9e37fc46948cbbde8e',
        },
        {
          alg: 'SHA-512',
          content:
            '7dcd359d76d3d924305130f9cb9f0758f9a7b5574fa737cccd44629bb6bb946bebec3a9323d6a84b4ca4bb5083ce1f99bd7ba78682f8751ad4185d32cda604c2',
        },
        {
          alg: 'SHA-384',
          content:
            '549fef67d6afd82c177548429390d959986064c051103edc063d691e10ff663d5743fab4fd1cfdeaf203cb925acf2c3a',
        },
        {
          alg: 'SHA3-384',
          content:
            'aaf3913b5df31048b841c305b29ec6be978c615846f36eaa4f5e8e278c4af6db96c7e441c7eeb9828c3728b40e5fe5e7',
        },
        {
          alg: 'SHA3-256',
          content:
            '387f0d730eff538a56c7e0da1c75e512e888f73a1373c91e24ab5019ee969902',
        },
        {
          alg: 'SHA3-512',
          content:
            '01856c9595cdc7aa2501f82f7482a710aad3050098114e185c2a05a7fcdc2a06a6ca932540ae0a4ff84c084a621b913b50dd29eb894376707554134be9cacdff',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
    },
    {
      group: 'io.micrometer',
      name: 'micrometer-observation',
      version: '1.13.0-RC1',
      description: 'Module containing Observation related code',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '465af4c7b39389b4b64ee821c58391c4',
        },
        {
          alg: 'SHA-1',
          content: 'a9977cbdfad0d4271377c197e4273260e7d05200',
        },
        {
          alg: 'SHA-256',
          content:
            '30cf97a63e36e1016e6e482ecf7569abf0711f34991e30c8a6180b7c27961890',
        },
        {
          alg: 'SHA-512',
          content:
            'ebbca3cfe52d16124a079d95db429e3a82fe5ae7d0b09380db1d47fdec2207848ac4192afb06852e13d08cf042b10e2ab2da179eb0f3b0e382e32db3b5487bbc',
        },
        {
          alg: 'SHA-384',
          content:
            '1c06ea65a6e4e7fa4226db67817769aa347be244d268941351b1972dd7e92efa3b9d78f357a06e8a7f940fb59cbab34c',
        },
        {
          alg: 'SHA3-384',
          content:
            '7aad6d20027ff90e9d02e1840387a72716e7ee1264a08a0cfe95bd6ea4da038d49577821b1823cd2e26145330a37c2df',
        },
        {
          alg: 'SHA3-256',
          content:
            'db7a863b261f3b4eed015bcde2989acd4ea71de29e0ace7049e1de43a0c9ffad',
        },
        {
          alg: 'SHA3-512',
          content:
            '49aeec650a29421aeffb7233f1da579b9cb35d362130a52f19366cbdbb790ba8edf9309b4bf508bd96d69caa9ae094a7eb9c5a934607b437b73c7635188b6d81',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/io.micrometer/micrometer-observation@1.13.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/micrometer-metrics/micrometer',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/io.micrometer/micrometer-observation@1.13.0-RC1?type=jar',
    },
    {
      group: 'io.micrometer',
      name: 'micrometer-commons',
      version: '1.13.0-RC1',
      description: 'Module containing common code',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'e4d7efe7ad5ffd085e174a976307c86f',
        },
        {
          alg: 'SHA-1',
          content: '63f08fc273b44773c417014a8f1897502137bcbe',
        },
        {
          alg: 'SHA-256',
          content:
            '3cfe50d4f7cb3d223def27f83397d879ed66214a40fadb3d1da1970faf083e16',
        },
        {
          alg: 'SHA-512',
          content:
            'b011d2ed89193195554bf08199c5f06cb74207c1be3916cc2783050ef259710049fc571720f113439fd26f00a648ebf3ce70114c21e6a97422daef0e33981777',
        },
        {
          alg: 'SHA-384',
          content:
            '7987407064defaf0218c13217fd2d4573161e25fc5b2dee1f1d020dd15c34c82eddebb533ca45a91c53cb3fa0c39d9e2',
        },
        {
          alg: 'SHA3-384',
          content:
            'b66eda2bcf16c13c3d7a54975e0dccbb9c27bf891b170db953e1229db145753fc9a63222fce7abdfab3540b684c56198',
        },
        {
          alg: 'SHA3-256',
          content:
            'e5fe43477ea8e5b35feca18e3c52337f6bba9439af00bc055fe75201c4491275',
        },
        {
          alg: 'SHA3-512',
          content:
            '881fc90acd90b72db2d8cd6fe13ac0d8018e8450d739178a58c56c923fc74430ff7baad68e3dcea2b72563e00d0f350315f3e52202b876fc9438b3c8b20f648e',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/io.micrometer/micrometer-commons@1.13.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/micrometer-metrics/micrometer',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/io.micrometer/micrometer-commons@1.13.0-RC1?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-webmvc',
      version: '6.1.6',
      description: 'Spring Web MVC',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '9a174b724dac50617689536e658be1e4',
        },
        {
          alg: 'SHA-1',
          content: 'ef1f76db6d94bac428839cb91fa59235c8356e56',
        },
        {
          alg: 'SHA-256',
          content:
            '22f21b895c1d85d54d2357498b7aa2ea4e0e05646a360b98a1b67515f17666a9',
        },
        {
          alg: 'SHA-512',
          content:
            'f5dad19b989c885241365657a20ab68c1ccef9a827d28f0594dc0e7502cb1a8f52673d68c85e9e241ca5d349ca853606033fc6ae68a37a848d53be154c20960f',
        },
        {
          alg: 'SHA-384',
          content:
            '7ab7a42380d3635158a20bea9702f4f6c32259a95611627956638fd220a163cbc8234dd1e7d2a9c6c61cd9897594cd0f',
        },
        {
          alg: 'SHA3-384',
          content:
            'f924555166988fb9c980bfffea0b7b3c1fc55b309aa2de8492302eae281632de76c616e35e5cd6e3b7d029220aa1b896',
        },
        {
          alg: 'SHA3-256',
          content:
            '5fd7c65d3a4a7476af53d09e8948d9ffbd64bdc4684ced7b1aba62031caa1a46',
        },
        {
          alg: 'SHA3-512',
          content:
            'dd705a6dfa4ad993fa4734da16522c9e5eb590ec210cdb14df179026193e631afd38ced4b122803441e5bb483fcb238dd316f45d4ac81f20b042ab700003c7da',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-webmvc@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-webmvc@6.1.6?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.cloud',
      name: 'spring-cloud-starter',
      version: '4.1.2',
      description: 'Spring Cloud Starter',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '61f6ed7c42e186cd32941cb34e886eac',
        },
        {
          alg: 'SHA-1',
          content: '757a6f0ecdb191fb04c0aed2055e91f50f89231d',
        },
        {
          alg: 'SHA-256',
          content:
            '4d154b87d2601eceefd7eddfce08092c3e6c7cdf8ecef49809d56f14aaae7686',
        },
        {
          alg: 'SHA-512',
          content:
            '0f618660887c0da46552ff99ff48a80609d6ec2df03c9a9db3c28a85c65eabe020bc810f6753e3f0c036a13975746905e4684250b48dddaa08e3ea039e219e56',
        },
        {
          alg: 'SHA-384',
          content:
            'a17cb39994d1bee8f0e58feb3f451c71640991f8946decb018b22dd0ba9371e82d4828fc1a0fcd85fc96e6f60b86bf6e',
        },
        {
          alg: 'SHA3-384',
          content:
            '6ff22a32d93e94f994f7da8aaa6db6eb06bc5f468ce6b1b8f8783435e34b86d6960e06ea69fb774f9057cf98544be0a1',
        },
        {
          alg: 'SHA3-256',
          content:
            '5b01491c4ce024e79378fe7c6f7be0dbb81128a2d2e2083bfedd330a8fab9e6f',
        },
        {
          alg: 'SHA3-512',
          content:
            '7e71ec57c00b33e0934c4f40c23434a5a3b0ac4571c796cb441aa04b51c6267cb8a4096d8932466e7b6556c359dd4a5cca3abdaaffe4bbfd7424dd9b2bcc2557',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.cloud/spring-cloud-starter@4.1.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://projects.spring.io/spring-cloud',
        },
        {
          type: 'distribution',
          url: 'https://github.com/spring-cloud',
        },
        {
          type: 'distribution-intake',
          url: 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-cloud/spring-cloud-commons/spring-cloud-starter',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.cloud/spring-cloud-starter@4.1.2?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.cloud',
      name: 'spring-cloud-context',
      version: '4.1.2',
      description: 'Spring Cloud Context',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '791fc874f57106f112882318c640db8c',
        },
        {
          alg: 'SHA-1',
          content: '069d9edfe8c4b4037653d28b29f3184afc573603',
        },
        {
          alg: 'SHA-256',
          content:
            'c8594e855931216df95433735750b7a229bc1d52f60299041b38f9998f57b06c',
        },
        {
          alg: 'SHA-512',
          content:
            '5f234a5a762287e64c951269a37aca9c1cb5dea90c5d0cd84b0e49beb5d8ad87fd83480f854371ab136195354f953dc4eb6646da4186f17e54ceec58fbb1e07a',
        },
        {
          alg: 'SHA-384',
          content:
            'd4b15bf701af1a7614c433aa792f9945541eb6089fef2933b23271867bbaca479396e1643a08ea4b37c332dda98f9354',
        },
        {
          alg: 'SHA3-384',
          content:
            '6bcda9d7275df6d7fdbf7c2aada404b13696bfa3430d98e9dc61ae2700691cd89f727a48d5a121fc607819425df86d9b',
        },
        {
          alg: 'SHA3-256',
          content:
            '00fcba56a744c1ded7a5e67caae7d8ef02af961fb4da0b8ca1b2f6e91af7a169',
        },
        {
          alg: 'SHA3-512',
          content:
            '32fb2b12b806c016e2a52b04ea207fe6310f54be28cfd9e0b1f608cccf78ae0bea2baf1995b7632d322c8e89aa134d9b2d9f99a7729f68c8a755e09f16d3b8a2',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.cloud/spring-cloud-context@4.1.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://projects.spring.io/spring-cloud/spring-cloud-context/',
        },
        {
          type: 'distribution',
          url: 'https://github.com/spring-cloud',
        },
        {
          type: 'distribution-intake',
          url: 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-cloud/spring-cloud-commons/spring-cloud-context',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.cloud/spring-cloud-context@4.1.2?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.security',
      name: 'spring-security-crypto',
      version: '6.3.0-RC1',
      description: 'Spring Security',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '313c83d23c769416f746767f03484b3e',
        },
        {
          alg: 'SHA-1',
          content: '13b842fc45bf78c54f42151774789f63a4c7395f',
        },
        {
          alg: 'SHA-256',
          content:
            '4961c7f1da2c362444a9a047bd4d26513f5b9d90b623cc9e6d9c5a95052e6ded',
        },
        {
          alg: 'SHA-512',
          content:
            'ed03b0027d869f4aa0381f01c5826a9385500ee081b91a844b7b72e80e83bf5a4106327a348f6809c005bec578c7dbe1ba22e29c6320dc0896d08547b9c42b3b',
        },
        {
          alg: 'SHA-384',
          content:
            '6008c6d0a0bb6ac403484d01ea7b99c88d437c8f3033d7caedfe9e63eca8ffc204ca0813c9e03e7fa5c1317bc4fed979',
        },
        {
          alg: 'SHA3-384',
          content:
            'c8d8dc5a5097bc9d9a301eeabdbf54c73640e7e4d2c390257c1eeb0aace8793f71ef4c6cc715590f55dc745f322af61d',
        },
        {
          alg: 'SHA3-256',
          content:
            '1ba8a3ca028e574f4ae1455c05c5b12401ae129dcce8d2770736c9f0091d08d3',
        },
        {
          alg: 'SHA3-512',
          content:
            '019d9a59c2e0bd995367417662210e061c6f5540859ae32fdcd5602ff334b446afb7b71d537302577500182a912130c600657c3bebf1c313ad8b795ef05b3f98',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.security/spring-security-crypto@6.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-security',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-security/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-security',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.security/spring-security-crypto@6.3.0-RC1?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.cloud',
      name: 'spring-cloud-commons',
      version: '4.1.2',
      description: 'Spring Cloud Commons',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'e8b1acf9de6507b9f6900dfe6ee6a137',
        },
        {
          alg: 'SHA-1',
          content: '84377482af72a3ef008b6c981e77897a04ae20aa',
        },
        {
          alg: 'SHA-256',
          content:
            'e4e1e4d511422b9d79cb26cb7597fbee0a2d25e28a34527fc7a24f5e7900f084',
        },
        {
          alg: 'SHA-512',
          content:
            'edf6ee48a22e532ceeff37e9e7536dca0f0fd30dd136c844fa39308929c9cb87a66259c5fe86d89c5e91bbdbcd5c7d7b8d23e0dae12510fa040363d3e1f97810',
        },
        {
          alg: 'SHA-384',
          content:
            '8674cd5c6931066ec77972f0176fb1b16be8a3607092286fd0f1183ea11abdead5d80ef31666a07679e042d2fdf1ba15',
        },
        {
          alg: 'SHA3-384',
          content:
            'f60ce002a0d45e08b87854c95ccd67b1c4e6d7297da5f7f28072b903f73038bb24c0a06c6012002e4a8fc5b81ce54a51',
        },
        {
          alg: 'SHA3-256',
          content:
            'ef84cc527220b23fea91cce6b1c6619a0d8d1d89a65bf5f6fd453cb408e64e39',
        },
        {
          alg: 'SHA3-512',
          content:
            'd1fe7229c4a75eccc49ea12ee61e7f5105949f26aa8604ddfda8549b2ca2f4b0ec034f54fc7051ff705c15f324ca7f10016d4514fd0fac32067f334be472efa7',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.cloud/spring-cloud-commons@4.1.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://projects.spring.io/spring-cloud/spring-cloud-commons/',
        },
        {
          type: 'distribution',
          url: 'https://github.com/spring-cloud',
        },
        {
          type: 'distribution-intake',
          url: 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-cloud/spring-cloud-commons/spring-cloud-commons',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.cloud/spring-cloud-commons@4.1.2?type=jar',
    },
    {
      publisher: 'SpringSource',
      group: 'org.springframework.security',
      name: 'spring-security-rsa',
      version: '1.1.2',
      description:
        'Spring Security RSA is a small utility library for RSA ciphers. It belongs to the family of Spring Security crypto libraries that handle encoding and decoding text as a general, useful thing to be able to do.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '44755711c5e1d2bd12ea7bd669aff3a5',
        },
        {
          alg: 'SHA-1',
          content: 'ca388d615a60199186ec248ac2a9806a76db4014',
        },
        {
          alg: 'SHA-256',
          content:
            '6483d1ece7049e58c85b2904c1030d653840516e7b80bb4d4c00dbbb95a2c564',
        },
        {
          alg: 'SHA-512',
          content:
            'b204ac9aac553d1243889305d600a6ee79737e482e7c8b51833183a555a03c37c1631635a39b4ab442a74097881b9d7f1618517dce1d9a99318b2699cf16047b',
        },
        {
          alg: 'SHA-384',
          content:
            'f7be923a5b035df4f15caa1e8a9fa7dee58bbb14a9c93f348349b0c798ea9c0f907bde5654a9de5b84ff6afa9c355d0f',
        },
        {
          alg: 'SHA3-384',
          content:
            'cd9271f4c75cb2c9da22cf4322205fab4dba6f17e114dba7f2b4d7cb53545b5e8ca2f38db8a378d4b0107fc573ebd1d2',
        },
        {
          alg: 'SHA3-256',
          content:
            '830f5c790d73c1390b1ab2f64a453e44a5ad20217aae0ed3b2f9c23cc8209463',
        },
        {
          alg: 'SHA3-512',
          content:
            '2d1c4292f43052e08a0783dc1f9b723cfbc56662f67da5fba38f9b579aab60ba6ce7a871e19c3aa732cd28135e22be20c90c251aedb2b1cbaca3be2ec47ed684',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.security/spring-security-rsa@1.1.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://github.com/spring-projects/spring-security-oauth',
        },
        {
          type: 'distribution-intake',
          url: 'https://repo.spring.io/libs-release-local',
        },
        {
          type: 'vcs',
          url: 'http://github.com/spring-projects/spring-security-rsa',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.security/spring-security-rsa@1.1.2?type=jar',
    },
    {
      group: 'org.bouncycastle',
      name: 'bcprov-jdk18on',
      version: '1.77',
      description:
        'The Bouncy Castle Crypto package is a Java implementation of cryptographic algorithms. This jar contains JCE provider and lightweight API for the Bouncy Castle Cryptography APIs for JDK 1.8 and up.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'ca01387064e08db12e1345b474521ff1',
        },
        {
          alg: 'SHA-1',
          content: '2cc971b6c20949c1ff98d1a4bc741ee848a09523',
        },
        {
          alg: 'SHA-256',
          content:
            'dabb98c24d72c9b9f585633d1df9c5cd58d9ad373d0cd681367e6a603a495d58',
        },
        {
          alg: 'SHA-512',
          content:
            '56c359f1370131f91eaeae3ec1d44884358f4296defd8d7516c7b81b9b66158454a667eb1c859d8ad919faee074ae3ecb4ceba2a39a3dd799bef9ada2d8c3da3',
        },
        {
          alg: 'SHA-384',
          content:
            '2951d9bb941e960287cc4a8947a0239ccfb9bd5058002bd5b9fe045b0bb22e4b23f31357f65211c191384cedf3ef3555',
        },
        {
          alg: 'SHA3-384',
          content:
            'b02af7de4704cf8f93fcd876055595bd9d117afd5eecf0fa883c43e30a285cbbd71473dd9197d6bb41f2b7702bc2620f',
        },
        {
          alg: 'SHA3-256',
          content:
            '6e69119cc95e642da12dcb0043589137bc7b36ba11ff3299598aaa510b8f0c03',
        },
        {
          alg: 'SHA3-512',
          content:
            'da87498233675c659ed554261a641aeb2eecc83df76864f199fee9d5c63564c2fe9465baf86d9ac9e409bba74a4de1e7197eda8736852f4f4a729301ea8c9233',
        },
      ],
      licenses: [
        {
          license: {
            name: 'Bouncy Castle Licence',
            url: 'https://www.bouncycastle.org/licence.html',
          },
        },
      ],
      purl: 'pkg:maven/org.bouncycastle/bcprov-jdk18on@1.77?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://www.bouncycastle.org/java.html',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/bcgit/bc-java/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/bcgit/bc-java',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.bouncycastle/bcprov-jdk18on@1.77?type=jar',
    },
    {
      publisher: 'VMware, Inc.',
      group: 'org.springframework.boot',
      name: 'spring-boot-starter-mail',
      version: '3.3.0-RC1',
      description:
        "Starter for using Java Mail and Spring Framework's email sending support",
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '408a1076991aedc862f183cf7cd072ec',
        },
        {
          alg: 'SHA-1',
          content: 'b93d26b0a94995568fdf7a81616d8cf6238f7a76',
        },
        {
          alg: 'SHA-256',
          content:
            '77698db1dafe474e9fc430f6f8532bf80ae7d74f6b8d3d26a28f112875f3dd88',
        },
        {
          alg: 'SHA-512',
          content:
            '521352975f69713d274e94d8adf7e327a48c48047243839e31b5a5767f9a67cb7f725e00dc4b390732bcf82f8f0470a0051e431563046cc9f861e8ffc94e6808',
        },
        {
          alg: 'SHA-384',
          content:
            '511c84f78c10532dfe1ed1fbfc2ec7fd4e263fcd349e6d4c37d67b16cf811ca25192b30c4cef2bad5f4935cc0f0bff3e',
        },
        {
          alg: 'SHA3-384',
          content:
            'c5e2c1b5a331d1a1032b6674fd76d86e7de43e2611c40ba1bb7c1cf668439725216b553d51d7467c8ac6899bf44af875',
        },
        {
          alg: 'SHA3-256',
          content:
            '7ceb912921ce9f446be9eb0ac542a253690570dbce25fecc8ad85afb445faf96',
        },
        {
          alg: 'SHA3-512',
          content:
            '8fc7aa1eddeacbc592018db332c3be52ecfc13bb793f930903e009197082ce4dd4ae2546ad9a3597d50552d28aa23b766aa837f684afe9b02208908967ce5f8f',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.boot/spring-boot-starter-mail@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-boot',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-boot/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-boot',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.boot/spring-boot-starter-mail@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-context-support',
      version: '6.1.6',
      description: 'Spring Context Support',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '8d5862cfaec6d2d9b6292992317859cb',
        },
        {
          alg: 'SHA-1',
          content: '7cc404d7f0c6e1b1ecfa30080fe194b52867d6b2',
        },
        {
          alg: 'SHA-256',
          content:
            '0f62f0fef682b9ca10e83a292d01e934f0cb38207b07521bcb57e71050523fef',
        },
        {
          alg: 'SHA-512',
          content:
            '82f8fe4eb3bd776c0ea866eb5b11a3b5a1fbbc83348abe6d7c22fb5767fcac1ef4955969378d7a63059df4445fac059724e6f175099932cae95c381fa28bc3b0',
        },
        {
          alg: 'SHA-384',
          content:
            '0b7e95883e480c6bac9358517568c0bfc1f1fd1abf329f4b6c870ab0c7a24810d3f6c2beda6a19e8830393cc6dc8ec58',
        },
        {
          alg: 'SHA3-384',
          content:
            '79310e3758ea66e467fbb1908c8018bdb4eeec3ec52b3274965808fd252803b466214199e9aa357048aec0b958276853',
        },
        {
          alg: 'SHA3-256',
          content:
            '450c4fb5ae0ac74f111274395746f8eaf7ee68a7bdcc6c58582ead738653bda9',
        },
        {
          alg: 'SHA3-512',
          content:
            '14ff0935a90a396cf9f8597a7fb60a51441ae8773fc23acff541cf8cd758e1d364880b0cf1b543ce8f67d9c8b309f991484a7fa8c581eb69784ac137e0c1a71e',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-context-support@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework/spring-context-support@6.1.6?type=jar',
    },
    {
      publisher: 'Eclipse Foundation',
      group: 'org.eclipse.angus',
      name: 'jakarta.mail',
      version: '2.0.3',
      description: 'Angus Mail default provider',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '33043478b24ab3845a3bf702c18f9226',
        },
        {
          alg: 'SHA-1',
          content: '3dea6aeee9603f573687b0d4da5dc1316d921bb7',
        },
        {
          alg: 'SHA-256',
          content:
            'efb946424933806bc6f8136752d22fdb3ba887ea0527ff849c474e51f7b3715e',
        },
        {
          alg: 'SHA-512',
          content:
            '6e4abfa2efb985e2bb77e248c8788ea8dfd1fa2a3f88337399a0e4d0c748777100fb6b17c4d1f1e25ce2595fbbec78625f527b21a4986d4d16ea2132847fea51',
        },
        {
          alg: 'SHA-384',
          content:
            '9ab6e99b3d90c3ce031923dbed8ba018c6ae951d2b82d83b9918bc97da06b2096a74a47ae72f4be65bc60471745566b6',
        },
        {
          alg: 'SHA3-384',
          content:
            'c408a694e3c7db7e500f361ce0f999c905d27ba98d1fc4a1597904af2e5ae83689d5792b1a21369c440ef81f262a3c63',
        },
        {
          alg: 'SHA3-256',
          content:
            '8106e97a462f4fd311b9d5a1a3dbd1fa0e6df2ae3d1944240d8015bec1264c9a',
        },
        {
          alg: 'SHA3-512',
          content:
            '03a3e514d879d6533181f6e99cb234bc4c39cc67e19053ad33fd92547c3e41b8a744293b4bd90d6e89aa129c410b5fe819e6c852b360cb8d9f2a1bbe92126916',
        },
      ],
      licenses: [
        {
          license: {
            id: 'EPL-2.0',
          },
        },
        {
          license: {
            id: 'GPL-2.0-with-classpath-exception',
          },
        },
        {
          license: {
            id: 'BSD-3-Clause',
          },
        },
      ],
      purl: 'pkg:maven/org.eclipse.angus/jakarta.mail@2.0.3?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://eclipse-ee4j.github.io/angus-mail/jakarta.mail',
        },
        {
          type: 'distribution-intake',
          url: 'https://jakarta.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/eclipse-ee4j/angus-mail/issues',
        },
        {
          type: 'mailing-list',
          url: 'https://dev.eclipse.org/mhonarc/lists/jakarta.ee-community/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/eclipse-ee4j/angus-mail/jakarta.mail',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.eclipse.angus/jakarta.mail@2.0.3?type=jar',
    },
    {
      publisher: 'Eclipse Foundation',
      group: 'jakarta.activation',
      name: 'jakarta.activation-api',
      version: '2.1.3',
      description: 'Jakarta Activation API 2.1 Specification',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '76e7b680375ea9f40f3ddbd702efcd25',
        },
        {
          alg: 'SHA-1',
          content: 'fa165bd70cda600368eee31555222776a46b881f',
        },
        {
          alg: 'SHA-256',
          content:
            '01b176d718a169263e78290691fc479977186bcc6b333487325084d6586f4627',
        },
        {
          alg: 'SHA-512',
          content:
            'aaabd4d6085a07035eaaae7b5a81aef429fea76e7fe1c8d29971e6595f0adad6bcf1088cff8a1c8936d739b0e3fce4b845323032f046b7edab2eaebd0e10a2ad',
        },
        {
          alg: 'SHA-384',
          content:
            '4c4e73f59bf09342ca7691fd4855b41d3466da80618a5b7df059a2d89cf6d9779a4af751a6c4a9c48e5025c3ff75f42e',
        },
        {
          alg: 'SHA3-384',
          content:
            '20be816700c87778e9453d41b6d8cb9dc992a092a308a9b7f2dfbf72e2393940a7d666c46625f130a2b57bc414df85ca',
        },
        {
          alg: 'SHA3-256',
          content:
            '8a574b0a249842ea1b397d4cdef9b6d00b34ce8a849ea34184cdf45ac5aafe67',
        },
        {
          alg: 'SHA3-512',
          content:
            '69cfb7dddda70ac1fca272ace0a3d5551b85dd60a6dbaf987ee777fbf573b420d13f06b8990ae70e8fe063f92b78c8a447cf9309ba516a5e993ba2d49cca8d23',
        },
      ],
      licenses: [
        {
          license: {
            id: 'BSD-3-Clause',
          },
        },
      ],
      purl: 'pkg:maven/jakarta.activation/jakarta.activation-api@2.1.3?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/jakartaee/jaf-api',
        },
        {
          type: 'distribution-intake',
          url: 'https://jakarta.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/jakartaee/jaf-api/issues/',
        },
        {
          type: 'mailing-list',
          url: 'https://dev.eclipse.org/mhonarc/lists/jakarta.ee-community/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/jakartaee/jaf-api',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/jakarta.activation/jakarta.activation-api@2.1.3?type=jar',
    },
    {
      publisher: 'Eclipse Foundation',
      group: 'org.eclipse.angus',
      name: 'angus-activation',
      version: '2.0.2',
      description: 'Angus Activation Registries Implementation',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '42bba74155dc773eca277ee7a16f74be',
        },
        {
          alg: 'SHA-1',
          content: '41f1e0ddd157c856926ed149ab837d110955a9fc',
        },
        {
          alg: 'SHA-256',
          content:
            '6dd3bcffc22bce83b07376a0e2e094e4964a3195d4118fb43e380ef35436cc1e',
        },
        {
          alg: 'SHA-512',
          content:
            '1482c759843c23e0343ca554194862d53ac18a04ab4691b3bf05145abb77283617022a895c5ba2e33f62b77c2cfb906b90d0cb690623621b11f35194b54b1180',
        },
        {
          alg: 'SHA-384',
          content:
            '0263b0f42e56f9cbf4a2446c26a29d6397477561c2149f7b7d0e62fb28ab4315d50faf4e96aff088d3ac204b16f90892',
        },
        {
          alg: 'SHA3-384',
          content:
            'e77e5bf8be9f98ed06a652e2317253bb29e8f79b26910075332823987b2e1bd3dfbb2d7aeb5a57a454c8632241abcc0a',
        },
        {
          alg: 'SHA3-256',
          content:
            '41d7d300d1399e4706a0ead464e13702d85023598a0a81899e40ee8eed847826',
        },
        {
          alg: 'SHA3-512',
          content:
            'dbdcb824069f0dcf9f9d362b8db7c2efa77f28d77e07c204a28e56b79ebfc478d9c5f9e5f01c7269d3afc0db0e6126d74237cc5a51b5e9ec6b6664580a06de8c',
        },
      ],
      licenses: [
        {
          license: {
            id: 'BSD-3-Clause',
          },
        },
      ],
      purl: 'pkg:maven/org.eclipse.angus/angus-activation@2.0.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/eclipse-ee4j/angus-activation/angus-activation',
        },
        {
          type: 'distribution-intake',
          url: 'https://jakarta.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/eclipse-ee4j/angus-activation/issues/',
        },
        {
          type: 'mailing-list',
          url: 'https://dev.eclipse.org/mhonarc/lists/jakarta.ee-community/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/eclipse-ee4j/angus-activation/angus-activation',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.eclipse.angus/angus-activation@2.0.2?type=jar',
    },
    {
      group: 'de.codecentric',
      name: 'spring-boot-admin-starter-client',
      version: '3.2.4-SNAPSHOT',
      scope: 'required',
      purl: 'pkg:maven/de.codecentric/spring-boot-admin-starter-client@3.2.4-SNAPSHOT?type=jar',
      type: 'library',
      'bom-ref':
        'pkg:maven/de.codecentric/spring-boot-admin-starter-client@3.2.4-SNAPSHOT?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.session',
      name: 'spring-session-core',
      version: '3.3.0-RC1',
      description: 'Spring Session',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'ccb0d954843dc820edeeeaf5152bbd2a',
        },
        {
          alg: 'SHA-1',
          content: '7a795db51bf3380c327f945a5addaf949e5edfa8',
        },
        {
          alg: 'SHA-256',
          content:
            '4a63d762c211e49ae21a13cc56acd1c25ac10882f481a261323c9f0b7397fc0a',
        },
        {
          alg: 'SHA-512',
          content:
            '809bc5e693b3bf2db798ec040dd0cc5b9c5d33ae43cf536333c2efadd33a4559b7d01aec8d2b939113ce4e18f7e4c548e0ee346236a1999fdcac5d2ae448ca2b',
        },
        {
          alg: 'SHA-384',
          content:
            'f5ba3c2d09289e1a6dd773eea4aa75ba2cf60209db4304eb3f05508b596e6ca5e18fcab02dbd5621a272fd1781d30f11',
        },
        {
          alg: 'SHA3-384',
          content:
            'a827feca6a8b8bc82f9bed8485c9cf3a1130e3d404bd4ba9798ad6a6ab15409f495b4bdf8cab95274151a6e9d8092f92',
        },
        {
          alg: 'SHA3-256',
          content:
            '903ab26a5e3269afbf5a94456db29bc01afda1273b29abcafd8b88f9bff6e17d',
        },
        {
          alg: 'SHA3-512',
          content:
            '63efe99ccbb959d5a66a177108949e4c07b329af11fdcfe270f01fe42ef221b33e8d44af12e88c606616bfe7c46d67b04c137a6bcda8ae00c497200d777abe3e',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.session/spring-session-core@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-session',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-session/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-session',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.session/spring-session-core@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-jcl',
      version: '6.1.6',
      description: 'Spring Commons Logging Bridge',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'acf8ba19c939bace96969efc2c5a6c2b',
        },
        {
          alg: 'SHA-1',
          content: '84cb19b30b22feca73c2ac005ca849c5890935a3',
        },
        {
          alg: 'SHA-256',
          content:
            'bfbd972fbd94dfb40cc2b19de21b769e8157497cf55555523a0e01b468b8e9b8',
        },
        {
          alg: 'SHA-512',
          content:
            'ae26ff2bdbb1928eb4349a3b0375376c64ba1e528006f826cf37b92af1552532cf5efaa49a115665dd2e426ea3cb2ee12e7120615766c41c2c7aab9f510240dc',
        },
        {
          alg: 'SHA-384',
          content:
            'db3928758d60ffd564c54a9ca173405910166e30e1dd799499019b85fac437aae515b50da9cd8f6c17eea94078c88c18',
        },
        {
          alg: 'SHA3-384',
          content:
            '9423a4259723be7647a237a150d22521dc412b3519f5d607e2a83dfbda4c26ff98e34b386ddbfcdc15aa5dd31375da4d',
        },
        {
          alg: 'SHA3-256',
          content:
            '9921f099221d6690b3bab7582b53d254c17560922c591f70e608d144fce564fb',
        },
        {
          alg: 'SHA3-512',
          content:
            '8aff52cc2ced97617ae8df8597045eee432a92a07cf34ccf03c7d3b8ecba455c86b16752a38a841a3cdaf050b19c0eebba447bc854caa92824564f67a304397f',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-jcl@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-jcl@6.1.6?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.session',
      name: 'spring-session-jdbc',
      version: '3.3.0-RC1',
      description: 'Spring Session and Spring JDBC integration',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'd950e3c7212c0994d66c4f6dabe6df06',
        },
        {
          alg: 'SHA-1',
          content: '1f633f053c2005b540513a1295ea4d38a57fb1cb',
        },
        {
          alg: 'SHA-256',
          content:
            'd2f3a9caa0f5812d412d20423852d50208292a0fe7e56777f8332f1b8b989201',
        },
        {
          alg: 'SHA-512',
          content:
            '4593eb3c20d6fe41b3c5566bfbb989c0c563dd81b0a796428c7e598da972d7a15aa9e55dd774c5b6e74e004e4c4bac9df03e9306be3dc20894df2eccbf09b600',
        },
        {
          alg: 'SHA-384',
          content:
            '708c0bdc7f191aaa9f005d29dfcbc53635666b7ac670082a270b77bff878ea2c90f874c02af8b0e2f86638df1febf579',
        },
        {
          alg: 'SHA3-384',
          content:
            '3e563a19f6bde339bff361595b2d3ac6ed6e627a384068ad5cf3636f2b1b77b8cc8d00453e980f5afc3da31977ad373c',
        },
        {
          alg: 'SHA3-256',
          content:
            '1de39e1781ec3f371b49acdee594256ec48e2a05d8fa5d71642a74427a5d23fe',
        },
        {
          alg: 'SHA3-512',
          content:
            '74699f35b8d1275420b989b1fa0b191e4013247d6a2e4309e6f1051692ea335643648142f63e390265c6d3807aef0ae0bdccaba97d50d56819423e31b9a6d910',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.session/spring-session-jdbc@3.3.0-RC1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io/projects/spring-session',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-session/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-session',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.session/spring-session-jdbc@3.3.0-RC1?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-context',
      version: '6.1.6',
      description: 'Spring Context',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'bff5b9db23e0dfe995e1f4a4a160c5d0',
        },
        {
          alg: 'SHA-1',
          content: '2be30298638975efaf7fff22f1570d79b2679814',
        },
        {
          alg: 'SHA-256',
          content:
            '452f82d693ada09ebd54666de9c1ad561cb77a1e9574e2076637c08d0b1393ce',
        },
        {
          alg: 'SHA-512',
          content:
            '70edce09aad5fe76ce0d98a6562a14d77d12c7f7fdedcdf3ca1f6fdc356f05de87bc3310268caca1ff7afef8b759a9a40c1ef78e88aa699b9f1eb76acbc968f5',
        },
        {
          alg: 'SHA-384',
          content:
            'f4072a357ee4e1c403cb0f6d4638f93ba85b611a0d27eafb481dc21933060116a20723dd9cc584559782c5ac0fc9ff37',
        },
        {
          alg: 'SHA3-384',
          content:
            '41424e53a13405ec20d5e21e32c078960bf456030bf268d8484c4a5390784b083b93b4524b86fe497e6af3c9c598d260',
        },
        {
          alg: 'SHA3-256',
          content:
            'b2fcf0269d3a9309ed817775b81ad748338dd071a1b51e77fe095f41806a0aa7',
        },
        {
          alg: 'SHA3-512',
          content:
            '563b6d0faff0662d57c1619e83135facc819abfb97d91f2cda542a1b034e6d521950fc09457b3b897ea8b20f96cc83379e0479837553a8434a9d7a3c01ed03ba',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-jdbc',
      version: '6.1.6',
      description: 'Spring JDBC',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'c8c19b4161ee251c20a0b6eba94ed825',
        },
        {
          alg: 'SHA-1',
          content: '3f8a440a49c15264ff438598b715bd00c5a88109',
        },
        {
          alg: 'SHA-256',
          content:
            '4f575ff3515214853590f07ccbdac48947a4bd1246596017fb048ab77d0290ae',
        },
        {
          alg: 'SHA-512',
          content:
            '3c513ec606ab67e69c1bb4b49b07a31c41cda60a064b0b83e30f0b2cb40ee10ade84f8e18f2c37d8c10d45a5fa4c0054d827219ad3aa0efb2f158d32928c3d4b',
        },
        {
          alg: 'SHA-384',
          content:
            'f414896caf82ba89de3dff44b78c9e43b9de9e625c75e4d696c20a8ddf35941d0bb52dbd268ba0896ca87f8b7007e032',
        },
        {
          alg: 'SHA3-384',
          content:
            '13a4c9fc471326b80243ce2d737f9cee633b3798e29888ba24447734f4b5ecbd4a8725e4198956aa7aeae052477ec5ac',
        },
        {
          alg: 'SHA3-256',
          content:
            'fe64c52eed719a2be7a11b7f35c115f68ee6e3d653212c909e1d417fcea16f46',
        },
        {
          alg: 'SHA3-512',
          content:
            'fab938f5c5734258958c30f9a4876d84dc94b1771b5d9ba63c8911176eca110a4b1ffc86ed73f1e5c98671f4fce14798adf387e330b01d3fd8e0c8c8430ad22d',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-jdbc@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-jdbc@6.1.6?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-tx',
      version: '6.1.6',
      description: 'Spring Transaction',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '290de6a7f55216181b65e1f682fd9bc4',
        },
        {
          alg: 'SHA-1',
          content: '4e18554fb6669f266108cc838a4619bbc8f7db8d',
        },
        {
          alg: 'SHA-256',
          content:
            'dc76ab1629b986555fc83a0f83803aa591b91af46e9d187ac9eb999a99898b8f',
        },
        {
          alg: 'SHA-512',
          content:
            '5456bf6a0bada70a25bb477534749f39a27e607d7b55144908c076882da9dc9a8a05e95051675ef635b6a285a91e51860a14d3dba94db110ac3309a040fe5f85',
        },
        {
          alg: 'SHA-384',
          content:
            '72cdfa7450e9706dcfccb735c8dbc7af2588cc566d88e3deb65f3f1c9db889e5d8520cd49a21daee503da3193f058435',
        },
        {
          alg: 'SHA3-384',
          content:
            'cd7807ba85d96ecfb8f019372dd658fc777990578b3e088e059bf37a5518ed126808009ac73d1e41d4030fc3b9524041',
        },
        {
          alg: 'SHA3-256',
          content:
            'd920b7548652e6a43344eb289e5f47b701ce1746f426c09d08d1960b3d6e831c',
        },
        {
          alg: 'SHA3-512',
          content:
            '5099e4245f5172cad173f62506596f09b5096f7777715527b07d4e88005ce4f48d16be35c4ebb4f3629f3a3ec3ff406d2a36e39ec9464e7364751b56ebb3b5fe',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-tx@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-tx@6.1.6?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.cloud',
      name: 'spring-cloud-starter-config',
      version: '4.1.1',
      description: 'Spring Cloud Starter',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'f1e90f5c16f40b9c6aa52273d42a36a5',
        },
        {
          alg: 'SHA-1',
          content: 'c90d7c91c5b422fc416f20a994b303074aa37388',
        },
        {
          alg: 'SHA-256',
          content:
            'b9b6d56cac78c59467001cd0fa904bc9e6dcc3e26e02872e0ac3d29b2c8031c2',
        },
        {
          alg: 'SHA-512',
          content:
            '5ce61142994ba355a8ee8cfe37e0b07a0b2b4be5ff3c39ab430d253a10f5f37cc910eb0eb4bc7f982e64f379012fa7d339d400a3c96e969b03ebc65cee2c0279',
        },
        {
          alg: 'SHA-384',
          content:
            'c3159a313b718fbb558d393768d7aecfd3bd73b89ec246435c6de75b152a27889cd946ef58632155a964732b733f07c4',
        },
        {
          alg: 'SHA3-384',
          content:
            '747a95ac4d7a2f237861a2cdb068fc42092e3a2aecd660e33cf075392360b1043c4919de8fb57cee82634178ede0d0c3',
        },
        {
          alg: 'SHA3-256',
          content:
            '72bab7c132b389f0ec733a0e29a4a0532b8cbddd49e8a923cf02f762fb6fed24',
        },
        {
          alg: 'SHA3-512',
          content:
            'c177c7c5073b1621b93220e790d1402d44ba9339c7468f9515930e96be26672c3e02703de1a61c636b9edee6207466b5c37250a73890290099126a237345decd',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.cloud/spring-cloud-starter-config@4.1.1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://projects.spring.io/spring-cloud',
        },
        {
          type: 'distribution',
          url: 'https://github.com/spring-cloud',
        },
        {
          type: 'distribution-intake',
          url: 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-cloud/spring-cloud-config/spring-cloud-starter-config',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.cloud/spring-cloud-starter-config@4.1.1?type=jar',
    },
    {
      publisher: 'Pivotal Software, Inc.',
      group: 'org.springframework.cloud',
      name: 'spring-cloud-config-client',
      version: '4.1.1',
      description: 'This project is a Spring configuration client.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '02567f0e59035f8b9cf4916468533939',
        },
        {
          alg: 'SHA-1',
          content: 'eb8e3991ab2a7c944a22544dc2b87763554b8409',
        },
        {
          alg: 'SHA-256',
          content:
            '556349cf937fbaf14428f4ee69976cf13c70af7701abb97489395572e5221031',
        },
        {
          alg: 'SHA-512',
          content:
            'f0e9ae214fca3739ec110ad32878de64acf9e45069b58f66a7c31694b932607f5b973f6332e4560a84de5ad8b0d6e1d537662b990aa967ed365f72dab38fb12d',
        },
        {
          alg: 'SHA-384',
          content:
            'cdbf51bcbdc08d8a9fcca1bdcab3c201be85053613fb12b2a866aefea6cf479d8c16aa7ae1b384c15f70f06ab7ad606b',
        },
        {
          alg: 'SHA3-384',
          content:
            '5fd2d1526a1e487bfbaa51143043bfd864854d8317f9d51d861047305c2d4ea9e143cc0d24fad9369ea89a4c2170cade',
        },
        {
          alg: 'SHA3-256',
          content:
            'db1ddb5759a6889da3fc99405529daf4556795b7ea6deac63fca9633f9fe69fc',
        },
        {
          alg: 'SHA3-512',
          content:
            '529441dfcf47e687640b432ad2dff0b365b3bfdfcf2090a8e64d650bca9b618b4b7c97c8f45b04087d711bb09d5477141ca4c53803ebb35ab2bb9ac2d6ac82ca',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework.cloud/spring-cloud-config-client@4.1.1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://spring.io',
        },
        {
          type: 'distribution',
          url: 'https://github.com/spring-cloud',
        },
        {
          type: 'distribution-intake',
          url: 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-cloud/spring-cloud-config/spring-cloud-config-client',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.springframework.cloud/spring-cloud-config-client@4.1.1?type=jar',
    },
    {
      publisher: 'FasterXML',
      group: 'com.fasterxml.jackson.core',
      name: 'jackson-annotations',
      version: '2.17.0',
      description:
        'Core annotations used for value types, used by Jackson data binding package.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '7529e022796db72bc17288e950c24b3f',
        },
        {
          alg: 'SHA-1',
          content: '880a742337010da4c851f843d8cac150e22dff9f',
        },
        {
          alg: 'SHA-256',
          content:
            '8562569a001d46e84ea23802257e33c8f68b24eb47c1e0efd133a0372c512959',
        },
        {
          alg: 'SHA-512',
          content:
            '20104840da168653b27ffcbef6600d29d04b7f315934531f6521b30cfc0438893ac5e3b2476ba03a6a47f3aed8882cc7d5a57b66163ad19aac217a258826e51b',
        },
        {
          alg: 'SHA-384',
          content:
            'c597370368f411e8f63500537a94f503f44f3bbd653c77d39871eb65745ee2a3d8d83bb8c303790c1e26f30e76219000',
        },
        {
          alg: 'SHA3-384',
          content:
            '6c446264fac7209fc435be283dbb6d578ed7328e84756d7e987a0871a9119bf9bffbfe40827e84324ea7924f83aad770',
        },
        {
          alg: 'SHA3-256',
          content:
            '437fa185a964c155377819fed79558491f31a7ee20a60c4624d252f6c6bf75bc',
        },
        {
          alg: 'SHA3-512',
          content:
            'ad18bc120cfafe0ee6c961c5422242361de7b3154d3252a2ffaecf5d981865c141a25fa8706709eabc351d42f3593dd2832219657671f21d9672c2488e5d1bf4',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/com.fasterxml.jackson.core/jackson-annotations@2.17.0?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/FasterXML/jackson',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/FasterXML/jackson-annotations/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/FasterXML/jackson-annotations',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/com.fasterxml.jackson.core/jackson-annotations@2.17.0?type=jar',
    },
    {
      publisher: 'The Apache Software Foundation',
      group: 'org.apache.httpcomponents.client5',
      name: 'httpclient5',
      version: '5.3.1',
      description: 'Apache HttpComponents Client',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'de1810a606b27192cbf5bbad9c25a648',
        },
        {
          alg: 'SHA-1',
          content: '56b53c8f4bcdaada801d311cf2ff8a24d6d96883',
        },
        {
          alg: 'SHA-256',
          content:
            '08346a757c617f6ecc66af9f099260adde1f3a1351fa81cb22fc17482b31f823',
        },
        {
          alg: 'SHA-512',
          content:
            '4c2d75106af8470789f0e08305e64ad86528f2f737da230e561892d33dbca0b6e2dbced2a075f0744cee7801c06ef174481540661b3c9a1bec6d6f93938b05bc',
        },
        {
          alg: 'SHA-384',
          content:
            '27470f74660b89f8a0af562a4edbd244afff4947b0fa7364c61e53ca49713efbca49e661214590f532c4acf9cfd66eac',
        },
        {
          alg: 'SHA3-384',
          content:
            'd25be0f1c5e0c02de0adf7113e591f10bd7fab20c168a20b7d15c859b252a6dc3ae3a24098e838d95c179ab3107f07b6',
        },
        {
          alg: 'SHA3-256',
          content:
            '9e22ce6935e71d12d1be70ef0b7cea9a87191c767de2904cb82fcb6e58d0e9b2',
        },
        {
          alg: 'SHA3-512',
          content:
            '9d36e201e469dd357ef715bba7beba62dbea98daefcea3b793fd285c2ffade97d72b35a07f05015fbc2d5b4fa5db58ff5ecf40e1269582a6c3e53ed62cbf97f4',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.httpcomponents.client5/httpclient5@5.3.1?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://hc.apache.org/httpcomponents-client-5.0.x/5.3.1/httpclient5/',
        },
        {
          type: 'distribution-intake',
          url: 'https://repository.apache.org/service/local/staging/deploy/maven2',
        },
        {
          type: 'issue-tracker',
          url: 'https://issues.apache.org/jira/browse/HTTPCLIENT',
        },
        {
          type: 'mailing-list',
          url: 'https://lists.apache.org/list.html?httpclient-users@hc.apache.org',
        },
        {
          type: 'vcs',
          url: 'https://github.com/apache/httpcomponents-client/tree/5.3.1/httpclient5',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.httpcomponents.client5/httpclient5@5.3.1?type=jar',
    },
    {
      publisher: 'The Apache Software Foundation',
      group: 'org.apache.httpcomponents.core5',
      name: 'httpcore5',
      version: '5.2.4',
      description: 'Apache HttpComponents HTTP/1.1 core components',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '5a3d417ea4e65e0f74194263dc5c6c43',
        },
        {
          alg: 'SHA-1',
          content: '34d8332b975f9e9a8298efe4c883ec43d45b7059',
        },
        {
          alg: 'SHA-256',
          content:
            'a7f62496113f66f9e27c26b84c44f5ce4555c6270083cdf2d45f255336cd52af',
        },
        {
          alg: 'SHA-512',
          content:
            '9fb4134d85e665e15410af005b21cd2f9b5e60d75112945d37b879f96f769a70be034557526ea7d05f8b83dda91c56d00f946763c44a183d7aea2857549b4481',
        },
        {
          alg: 'SHA-384',
          content:
            '8ec2da2fd22a23e9f740589947398a907795ab310d4ed166ecc1448ceea7035a50090cf645dad28f3c84c08599f1e57e',
        },
        {
          alg: 'SHA3-384',
          content:
            'e52f4f1c073ec9d893cffa353a27939054a0b537fe49e4aacfdd6c265dfba037309913a001c025d85ccbb06a1e9e72b0',
        },
        {
          alg: 'SHA3-256',
          content:
            '23fb185f22dac603ba579c4f707671f43b3b08ab049ae519fb492ea0232c5ba9',
        },
        {
          alg: 'SHA3-512',
          content:
            '8371cd7e6f94ccb1590bd90d5868a90a3344123b0e6a4f7113d76b816c16d58efc6f2cab124a45ffd93739990fed05201f294f890aa989345f52e736383b4261',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.httpcomponents.core5/httpcore5@5.2.4?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://hc.apache.org/httpcomponents-core-5.2.x/5.2.4/httpcore5/',
        },
        {
          type: 'distribution-intake',
          url: 'https://repository.apache.org/service/local/staging/deploy/maven2',
        },
        {
          type: 'issue-tracker',
          url: 'https://issues.apache.org/jira/browse/HTTPCORE',
        },
        {
          type: 'mailing-list',
          url: 'https://lists.apache.org/list.html?httpclient-users@hc.apache.org',
        },
        {
          type: 'vcs',
          url: 'https://github.com/apache/httpcomponents-core/tree/5.2.4/httpcore5',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.httpcomponents.core5/httpcore5@5.2.4?type=jar',
    },
    {
      publisher: 'The Apache Software Foundation',
      group: 'org.apache.httpcomponents.core5',
      name: 'httpcore5-h2',
      version: '5.2.4',
      description: 'Apache HttpComponents HTTP/2 Core Components',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'd407b8144029db656ac5ba3d54ef801f',
        },
        {
          alg: 'SHA-1',
          content: '2872764df7b4857549e2880dd32a6f9009166289',
        },
        {
          alg: 'SHA-256',
          content:
            'dc1a95e73eb04db93451533d390ce02c53b301a10dc343d08c862f2934b3d30e',
        },
        {
          alg: 'SHA-512',
          content:
            '72fbee55f173c43d9ffc0cc5a83d59e60be1002c06ab81de39ba700cc30b04e84fdfed73d3a8985d561a1aa8ac3ca905f9259d01b431e1ff14da6fae622f787d',
        },
        {
          alg: 'SHA-384',
          content:
            '2f96537af2866fa96aae46138febe3009dca97cc9b4284cf18510c12d159ad3f5d34c3c9bafc8026215da81520331660',
        },
        {
          alg: 'SHA3-384',
          content:
            '9900a3aeaf434d7f32a7500e29e16d354857ef34e6af3fb7de9e1ab7683b6a1c4bfa9b9f70bb779a8ec8d8be82b6bca4',
        },
        {
          alg: 'SHA3-256',
          content:
            'da34ed59342e368229b74245d2268a457588adea9e276a1ac2fb57419c605f31',
        },
        {
          alg: 'SHA3-512',
          content:
            'ca5b03cf34c7e344fd0b809c582e60f0eaea796372cf68e2e95087ac5943154e51472595f6554b810a5ac4789ba6f7c06cae46437badecbf31c57907123a49fc',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.apache.httpcomponents.core5/httpcore5-h2@5.2.4?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://hc.apache.org/httpcomponents-core-5.2.x/5.2.4/httpcore5-h2/',
        },
        {
          type: 'distribution-intake',
          url: 'https://repository.apache.org/service/local/staging/deploy/maven2',
        },
        {
          type: 'issue-tracker',
          url: 'https://issues.apache.org/jira/browse/HTTPCORE',
        },
        {
          type: 'mailing-list',
          url: 'https://lists.apache.org/list.html?httpclient-users@hc.apache.org',
        },
        {
          type: 'vcs',
          url: 'https://github.com/apache/httpcomponents-core/tree/5.2.4/httpcore5-h2',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/org.apache.httpcomponents.core5/httpcore5-h2@5.2.4?type=jar',
    },
    {
      publisher: 'FasterXML',
      group: 'com.fasterxml.jackson.core',
      name: 'jackson-databind',
      version: '2.17.0',
      description:
        'General data-binding functionality for Jackson: works on core streaming API',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '09dd83868b44c6a3dc48911f4b3bbbc1',
        },
        {
          alg: 'SHA-1',
          content: '7173e9e1d4bc6d7ca03bc4eeedcd548b8b580b34',
        },
        {
          alg: 'SHA-256',
          content:
            'd0ed5b54cb1b0bbb0828e24ce752a43a006dc188b34e3a4ae3238acc7b637418',
        },
        {
          alg: 'SHA-512',
          content:
            'c6b06d4b20941d9e32b462552031e6c98378e5edce57693e55adcc73cf7d5088af5b3a666a59e94a7f0b57066ac694863919f398f28ee0d7ceb362c8c05f7491',
        },
        {
          alg: 'SHA-384',
          content:
            '02875865ef42573114755ab7147d64f8e5a791f2a2b8debe51dda22886370ef34af8c159d8efa8b90735f33f90089187',
        },
        {
          alg: 'SHA3-384',
          content:
            'ee93411dc73337c11d48609fbf79ae606ccd0ab712e3d2c12c91103964182910a810b9fa062a0afc47d19c720c97c5e7',
        },
        {
          alg: 'SHA3-256',
          content:
            'eb5b5dfb8afb2538a2c31caab47d909970bb647763a0eccabaae8e6f0a9ad988',
        },
        {
          alg: 'SHA3-512',
          content:
            '09ce7a8d928d42b221e0f6151653248f78eb68b6228cfc36fabd48ae48a3890f235fb33b82cc2faa24069efdf5a14d7433014e3bfb0d5173047d1911e5a55fe4',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/FasterXML/jackson',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/FasterXML/jackson-databind/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/FasterXML/jackson-databind',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
    },
    {
      publisher: 'FasterXML',
      group: 'com.fasterxml.jackson.core',
      name: 'jackson-core',
      version: '2.17.0',
      description:
        'Core Jackson processing abstractions (aka Streaming API), implementation for JSON',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '3e4b82b6e29693927dd289a344c35e46',
        },
        {
          alg: 'SHA-1',
          content: 'a6e5058ef9720623c517252d17162f845306ff3a',
        },
        {
          alg: 'SHA-256',
          content:
            '55be130f6a68038088a261856c4e383ce79957a0fc1a29ecb213a9efd6ef4389',
        },
        {
          alg: 'SHA-512',
          content:
            '85611fb7687450eb6078855c46d94dabf1cebcf179e23455cd1069aaded3b169112ec5d3d8d8510a7076166dc146e2f684f8527c5ef5b9ed99a7ec91f0825523',
        },
        {
          alg: 'SHA-384',
          content:
            '12bbfe5721ecd374a77ede24cca8a39f1415fd50dd95938c5e3365c703a02b5f6c0e7b10c7b44b7c9c5a874dd0b971c7',
        },
        {
          alg: 'SHA3-384',
          content:
            'd1b1f9c3e53603ccc690d76ac1a90dd1ddf07723f4eb53f58acfa266f17a675b311b0f29b94d9f1bb0ab32ad1a8aea4a',
        },
        {
          alg: 'SHA3-256',
          content:
            '62a76265cdc48c8a7f80c9d5566a179bd796c646b25c5cb937fd0a10cfffff1f',
        },
        {
          alg: 'SHA3-512',
          content:
            '87c42bea365905e9d877bda162c9d79d962a969a53a46861c350b9a9a87d09f4986c6ff67c4f00eba6df8c86f0621cfb52359a91321ace7eec3bb5d7c82feeb9',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/FasterXML/jackson-core',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/FasterXML/jackson-core/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/FasterXML/jackson-core',
        },
      ],
      type: 'library',
      'bom-ref':
        'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
    },
    {
      group: 'net.bytebuddy',
      name: 'byte-buddy',
      version: '1.14.13',
      description:
        'Byte Buddy is a Java library for creating Java classes at run time. This artifact is a build of Byte Buddy with all ASM dependencies repackaged into its own name space.',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '7f4df0c9277f4c1c418a742cc3178ac9',
        },
        {
          alg: 'SHA-1',
          content: '45cf516d9a23485200950549ff72b204c307fc9d',
        },
        {
          alg: 'SHA-256',
          content:
            'ba8254ff6d612af49acee4cac1108453ce3a417efa548b24f2f4f268cd2b441a',
        },
        {
          alg: 'SHA-512',
          content:
            'c7f76ce1bf108c98af398c3b2df01d8bd0a81a8eb6efe669fb23ef3cfb33419fe7f975c2523579f4d48567da7786751a31ef79f41babc391be250da485c93b0e',
        },
        {
          alg: 'SHA-384',
          content:
            '31ea6c6cc36495936761edee2ce2a3ba61edf66e5e2de78d7ae243db03e84cf57081e8434b5c4cafbf042a5ab15799ec',
        },
        {
          alg: 'SHA3-384',
          content:
            '0528e8facb1eb96e7c2f825bbda814432ca7b269de046c9d49eac047762fc5dc0c3d6b91cca08a9853d8744497e28e62',
        },
        {
          alg: 'SHA3-256',
          content:
            'a1da765e093a8b14bb5c2e3eb7012ced676840f8201a3ffaf47c3da4784018fd',
        },
        {
          alg: 'SHA3-512',
          content:
            '05581ee76ccce6fb141e55863db63e03a9885381a185a3413046bf5f40302d8314141ff093f396ef99a3b0948fc58346b0b5c7c139ccae2294cd3040bd0493cd',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/net.bytebuddy/byte-buddy@1.14.13?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://bytebuddy.net/byte-buddy',
        },
        {
          type: 'distribution-intake',
          url: 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/raphw/byte-buddy/issues',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/net.bytebuddy/byte-buddy@1.14.13?type=jar',
    },
    {
      publisher: 'The HSQL Development Group',
      group: 'org.hsqldb',
      name: 'hsqldb',
      version: '2.7.2',
      description: 'HSQLDB - Lightweight 100% Java SQL Database Engine',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: 'dab42304e10a7983af59ce89a8ccee12',
        },
        {
          alg: 'SHA-1',
          content: 'd92d4d2aa515714da2165c9d640d584c2896c9df',
        },
        {
          alg: 'SHA-256',
          content:
            'aa455133e664f6a7e6f30cd0cd4f8ad83dfbd94eb717c438548e446784614a92',
        },
        {
          alg: 'SHA-512',
          content:
            '0b997354bae288f84925cfb0aca0525257225a150a84ecbc687b3c7e6ef38cddbcdf2cf24404ac2bf4990a5d8baad2394c38bb5b299a7cfcbd2982741cd35b20',
        },
        {
          alg: 'SHA-384',
          content:
            'f2d2e3fa488ffc041cef71acb951a9475177974c98a9f6b9f711d0ea67bd6c344dd0601856423fd96e63b080d9a41dfc',
        },
        {
          alg: 'SHA3-384',
          content:
            '84b769ff1bb3ace9e00b9e499405b445304bc55892d093e8306a9f94e100c2fb3aefa569290e74f42b207c980f0baaef',
        },
        {
          alg: 'SHA3-256',
          content:
            'b981fa576356765be24044e6bcc3d273891a8e53a2caa5a4541a6039cfc4ae67',
        },
        {
          alg: 'SHA3-512',
          content:
            '042bfe7d7d30f16c23bb2305e37c526999553c2ba1df39ab7d1108943a4699b84fc9515e5ab272733e39882aac22d59fb1691c95e0d1d99f8bb012ce8caf8aa3',
        },
      ],
      licenses: [
        {
          license: {
            name: 'HSQLDB License, a BSD open source license',
            url: 'http://hsqldb.org/web/hsqlLicense.html',
          },
        },
      ],
      purl: 'pkg:maven/org.hsqldb/hsqldb@2.7.2?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://hsqldb.org',
        },
        {
          type: 'vcs',
          url: 'http://sourceforge.net/p/hsqldb/svn/HEAD/tree/base/tags/2.7.2',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.hsqldb/hsqldb@2.7.2?type=jar',
    },
    {
      publisher: 'QOS.ch',
      group: 'org.slf4j',
      name: 'slf4j-api',
      version: '2.0.13',
      description: 'The slf4j API',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '7f4028aa04f75427327f3f30cd62ba4e',
        },
        {
          alg: 'SHA-1',
          content: '80229737f704b121a318bba5d5deacbcf395bc77',
        },
        {
          alg: 'SHA-256',
          content:
            'e7c2a48e8515ba1f49fa637d57b4e2f590b3f5bd97407ac699c3aa5efb1204a9',
        },
        {
          alg: 'SHA-512',
          content:
            'b4eeb5757118e264ec7f107d879270784357380d6f53471b7874dd7e0166fdf5686a95eb66bab867abbe9536da032ab052e207165211391c293cbf6178431fb6',
        },
        {
          alg: 'SHA-384',
          content:
            'b67cbb4ef32141423000dd4e067bf32e0c1dd2c4689c611522b9fedfc1744513175a22f4b1276f2cec4721c9467cf882',
        },
        {
          alg: 'SHA3-384',
          content:
            '817fc9641f4fc52bfd76006886c6eba975f6f09b2a7cc59334729a8cc033807c8e89be9ec4309acfc16ed65ff6eee018',
        },
        {
          alg: 'SHA3-256',
          content:
            'f26080cceb5a2e605f3844d6dc8dd3f14c543cb14510765d841d71a64fa454dc',
        },
        {
          alg: 'SHA3-512',
          content:
            '00646c78d65ec854e157638f40735f1888aa585ede59915d58386c599c2fe54ec8c1da73284aeff00ce3142165e33c4c995ad39d08843c31e9e4d7e32c746836',
        },
      ],
      licenses: [
        {
          license: {
            id: 'MIT',
            url: 'https://opensource.org/licenses/MIT',
          },
        },
      ],
      purl: 'pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'http://www.slf4j.org',
        },
        {
          type: 'distribution-intake',
          url: 'https://oss.sonatype.org/service/local/staging/deploy/maven2/',
        },
        {
          type: 'vcs',
          url: 'https://github.com/qos-ch/slf4j/slf4j-parent/slf4j-api',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar',
    },
    {
      publisher: 'Spring IO',
      group: 'org.springframework',
      name: 'spring-core',
      version: '6.1.6',
      description: 'Spring Core',
      scope: 'required',
      hashes: [
        {
          alg: 'MD5',
          content: '852be6055a31d2ce17b5d231b17f732e',
        },
        {
          alg: 'SHA-1',
          content: 'dea4b8e110b7b54a02a4907e32dbb0adee8a7168',
        },
        {
          alg: 'SHA-256',
          content:
            'caf51f3d51c5d95e931f411027688f1dde3986d5f2aad67ff1096ddddac36ac5',
        },
        {
          alg: 'SHA-512',
          content:
            '893d9c5956c3005717dc7f09b31908dfed3588f9c81fb6180781ca687f305157cf3481f246d1a493fa348991d41a660b54e5db7ff5a4e4676570062b8c22b38b',
        },
        {
          alg: 'SHA-384',
          content:
            '984ff65f605a97d0ecea2a30986fb6c443deb83c7f58450bc661d9060563e1f199f9d472794520106ccbd23b29de0531',
        },
        {
          alg: 'SHA3-384',
          content:
            'edf8fafbef9d85a15d226dcc85d4fd71c6eaca2cd885867b8a81c37424c9ce024dc16a8f8169a2b2d1214b8b6532d278',
        },
        {
          alg: 'SHA3-256',
          content:
            '71f60a76d6b31290bb2024f17e82e5f920d2dc576a2649c054767ea574baf685',
        },
        {
          alg: 'SHA3-512',
          content:
            'cf4ab0e13b212c22f9cbea6afdf4aac815c62bd904d2f0d91198e29098988495b4038b6b3c56b6cb48d5b26efc39dec92283e067a969be1d75db817949d73cc4',
        },
      ],
      licenses: [
        {
          license: {
            id: 'Apache-2.0',
          },
        },
      ],
      purl: 'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
      externalReferences: [
        {
          type: 'website',
          url: 'https://github.com/spring-projects/spring-framework',
        },
        {
          type: 'issue-tracker',
          url: 'https://github.com/spring-projects/spring-framework/issues',
        },
        {
          type: 'vcs',
          url: 'https://github.com/spring-projects/spring-framework',
        },
      ],
      type: 'library',
      'bom-ref': 'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
    },
  ],
  dependencies: [
    {
      ref: 'pkg:maven/de.codecentric/spring-boot-admin-sample-servlet@3.2.4-SNAPSHOT?type=jar',
      dependsOn: [
        'pkg:maven/de.codecentric/spring-boot-admin-sample-custom-ui@3.2.4-SNAPSHOT?type=jar',
        'pkg:maven/de.codecentric/spring-boot-admin-starter-server@3.2.4-SNAPSHOT?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-starter-security@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-starter-web@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-starter@4.1.2?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-starter-mail@3.3.0-RC1?type=jar',
        'pkg:maven/de.codecentric/spring-boot-admin-starter-client@3.2.4-SNAPSHOT?type=jar',
        'pkg:maven/org.springframework.session/spring-session-core@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.session/spring-session-jdbc@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-starter-config@4.1.1?type=jar',
        'pkg:maven/org.hsqldb/hsqldb@2.7.2?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/de.codecentric/spring-boot-admin-sample-custom-ui@3.2.4-SNAPSHOT?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/de.codecentric/spring-boot-admin-starter-server@3.2.4-SNAPSHOT?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter-security@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
        'pkg:maven/org.springframework.security/spring-security-config@6.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.security/spring-security-web@6.3.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-autoconfigure@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-starter-logging@3.3.0-RC1?type=jar',
        'pkg:maven/jakarta.annotation/jakarta.annotation-api@2.1.1?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.yaml/snakeyaml@2.2?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
      dependsOn: ['pkg:maven/org.springframework/spring-jcl@6.1.6?type=jar'],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-jcl@6.1.6?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
        'pkg:maven/io.micrometer/micrometer-observation@1.13.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
      dependsOn: ['pkg:maven/org.springframework/spring-core@6.1.6?type=jar'],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
      dependsOn: ['pkg:maven/org.springframework/spring-core@6.1.6?type=jar'],
    },
    {
      ref: 'pkg:maven/io.micrometer/micrometer-observation@1.13.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/io.micrometer/micrometer-commons@1.13.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/io.micrometer/micrometer-commons@1.13.0-RC1?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-autoconfigure@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot@3.3.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter-logging@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/ch.qos.logback/logback-classic@1.5.6?type=jar',
        'pkg:maven/org.apache.logging.log4j/log4j-to-slf4j@2.23.1?type=jar',
        'pkg:maven/org.slf4j/jul-to-slf4j@2.0.13?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/ch.qos.logback/logback-classic@1.5.6?type=jar',
      dependsOn: [
        'pkg:maven/ch.qos.logback/logback-core@1.5.6?type=jar',
        'pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/ch.qos.logback/logback-core@1.5.6?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.apache.logging.log4j/log4j-to-slf4j@2.23.1?type=jar',
      dependsOn: [
        'pkg:maven/org.apache.logging.log4j/log4j-api@2.23.1?type=jar',
        'pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.apache.logging.log4j/log4j-api@2.23.1?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.slf4j/jul-to-slf4j@2.0.13?type=jar',
      dependsOn: ['pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar'],
    },
    {
      ref: 'pkg:maven/jakarta.annotation/jakarta.annotation-api@2.1.1?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.yaml/snakeyaml@2.2?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework.security/spring-security-config@6.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.security/spring-security-core@6.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.security/spring-security-core@6.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.security/spring-security-crypto@6.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
        'pkg:maven/io.micrometer/micrometer-observation@1.13.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.security/spring-security-crypto@6.3.0-RC1?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework.security/spring-security-web@6.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.security/spring-security-core@6.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/io.micrometer/micrometer-observation@1.13.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter-web@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-starter-json@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.boot/spring-boot-starter-tomcat@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-webmvc@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter-json@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.module/jackson-module-parameter-names@2.17.0?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
      dependsOn: [
        'pkg:maven/com.fasterxml.jackson.core/jackson-annotations@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
        'pkg:maven/net.bytebuddy/byte-buddy@1.14.13?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/com.fasterxml.jackson.core/jackson-annotations@2.17.0?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/net.bytebuddy/byte-buddy@1.14.13?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jdk8@2.17.0?type=jar',
      dependsOn: [
        'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/com.fasterxml.jackson.datatype/jackson-datatype-jsr310@2.17.0?type=jar',
      dependsOn: [
        'pkg:maven/com.fasterxml.jackson.core/jackson-annotations@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/com.fasterxml.jackson.module/jackson-module-parameter-names@2.17.0?type=jar',
      dependsOn: [
        'pkg:maven/com.fasterxml.jackson.core/jackson-core@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter-tomcat@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/jakarta.annotation/jakarta.annotation-api@2.1.1?type=jar',
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@10.1.20?type=jar',
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-el@10.1.20?type=jar',
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-websocket@10.1.20?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@10.1.20?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.apache.tomcat.embed/tomcat-embed-el@10.1.20?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.apache.tomcat.embed/tomcat-embed-websocket@10.1.20?type=jar',
      dependsOn: [
        'pkg:maven/org.apache.tomcat.embed/tomcat-embed-core@10.1.20?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-webmvc@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-aop@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-expression@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.cloud/spring-cloud-starter@4.1.2?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-context@4.1.2?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-commons@4.1.2?type=jar',
        'pkg:maven/org.springframework.security/spring-security-rsa@1.1.2?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.cloud/spring-cloud-context@4.1.2?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.security/spring-security-crypto@6.3.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.cloud/spring-cloud-commons@4.1.2?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.security/spring-security-crypto@6.3.0-RC1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.security/spring-security-rsa@1.1.2?type=jar',
      dependsOn: ['pkg:maven/org.bouncycastle/bcprov-jdk18on@1.77?type=jar'],
    },
    {
      ref: 'pkg:maven/org.bouncycastle/bcprov-jdk18on@1.77?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework.boot/spring-boot-starter-mail@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot-starter@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-context-support@6.1.6?type=jar',
        'pkg:maven/org.eclipse.angus/jakarta.mail@2.0.3?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-context-support@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.eclipse.angus/jakarta.mail@2.0.3?type=jar',
      dependsOn: [
        'pkg:maven/jakarta.activation/jakarta.activation-api@2.1.3?type=jar',
        'pkg:maven/org.eclipse.angus/angus-activation@2.0.2?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/jakarta.activation/jakarta.activation-api@2.1.3?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.eclipse.angus/angus-activation@2.0.2?type=jar',
      dependsOn: [
        'pkg:maven/jakarta.activation/jakarta.activation-api@2.1.3?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/de.codecentric/spring-boot-admin-starter-client@3.2.4-SNAPSHOT?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.springframework.session/spring-session-core@3.3.0-RC1?type=jar',
      dependsOn: ['pkg:maven/org.springframework/spring-jcl@6.1.6?type=jar'],
    },
    {
      ref: 'pkg:maven/org.springframework.session/spring-session-jdbc@3.3.0-RC1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.session/spring-session-core@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework/spring-context@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-jdbc@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-jdbc@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-tx@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework/spring-tx@6.1.6?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework/spring-beans@6.1.6?type=jar',
        'pkg:maven/org.springframework/spring-core@6.1.6?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.cloud/spring-cloud-starter-config@4.1.1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.cloud/spring-cloud-starter@4.1.2?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-config-client@4.1.1?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.springframework.cloud/spring-cloud-config-client@4.1.1?type=jar',
      dependsOn: [
        'pkg:maven/org.springframework.boot/spring-boot-autoconfigure@3.3.0-RC1?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-commons@4.1.2?type=jar',
        'pkg:maven/org.springframework.cloud/spring-cloud-context@4.1.2?type=jar',
        'pkg:maven/org.springframework/spring-web@6.1.6?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-annotations@2.17.0?type=jar',
        'pkg:maven/com.fasterxml.jackson.core/jackson-databind@2.17.0?type=jar',
        'pkg:maven/org.apache.httpcomponents.client5/httpclient5@5.3.1?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.apache.httpcomponents.client5/httpclient5@5.3.1?type=jar',
      dependsOn: [
        'pkg:maven/org.apache.httpcomponents.core5/httpcore5@5.2.4?type=jar',
        'pkg:maven/org.apache.httpcomponents.core5/httpcore5-h2@5.2.4?type=jar',
        'pkg:maven/org.slf4j/slf4j-api@2.0.13?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.apache.httpcomponents.core5/httpcore5@5.2.4?type=jar',
      dependsOn: [],
    },
    {
      ref: 'pkg:maven/org.apache.httpcomponents.core5/httpcore5-h2@5.2.4?type=jar',
      dependsOn: [
        'pkg:maven/org.apache.httpcomponents.core5/httpcore5@5.2.4?type=jar',
      ],
    },
    {
      ref: 'pkg:maven/org.hsqldb/hsqldb@2.7.2?type=jar',
      dependsOn: [],
    },
  ],
};

export const systemSbomResponse = applicationSbomResponse;
