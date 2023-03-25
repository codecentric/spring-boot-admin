export const jolokiaList = {
  request: { type: 'list' },
  value: {
    'jdk.management.jfr': {
      'type=FlightRecorder': {
        op: {
          getRecordingOptions: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: 'javax.management.openmbean.TabularData',
            desc: 'getRecordingOptions',
          },
          takeSnapshot: { args: [], ret: 'long', desc: 'takeSnapshot' },
          closeRecording: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: 'void',
            desc: 'closeRecording',
          },
          newRecording: { args: [], ret: 'long', desc: 'newRecording' },
          setRecordingSettings: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'javax.management.openmbean.TabularData',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'setRecordingSettings',
          },
          openStream: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'javax.management.openmbean.TabularData',
                desc: 'p1',
              },
            ],
            ret: 'long',
            desc: 'openStream',
          },
          cloneRecording: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'boolean',
                desc: 'p1',
              },
            ],
            ret: 'long',
            desc: 'cloneRecording',
          },
          setRecordingOptions: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'javax.management.openmbean.TabularData',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'setRecordingOptions',
          },
          copyTo: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'copyTo',
          },
          startRecording: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: 'void',
            desc: 'startRecording',
          },
          closeStream: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: 'void',
            desc: 'closeStream',
          },
          getRecordingSettings: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: 'javax.management.openmbean.TabularData',
            desc: 'getRecordingSettings',
          },
          setPredefinedConfiguration: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'setPredefinedConfiguration',
          },
          readStream: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: '[B',
            desc: 'readStream',
          },
          setConfiguration: {
            args: [
              { name: 'p0', type: 'long', desc: 'p0' },
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'setConfiguration',
          },
          stopRecording: {
            args: [{ name: 'p0', type: 'long', desc: 'p0' }],
            ret: 'boolean',
            desc: 'stopRecording',
          },
        },
        attr: {
          EventTypes: {
            rw: false,
            type: '[Ljavax.management.openmbean.CompositeData;',
            desc: 'EventTypes',
          },
          Recordings: {
            rw: false,
            type: '[Ljavax.management.openmbean.CompositeData;',
            desc: 'Recordings',
          },
          Configurations: {
            rw: false,
            type: '[Ljavax.management.openmbean.CompositeData;',
            desc: 'Configurations',
          },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'jdk.management.jfr.FlightRecorderMXBeanImpl',
        desc: 'Information on the management interface of the MBean',
      },
    },
    'java.util.logging': {
      'type=Logging': {
        op: {
          getLoggerLevel: {
            args: [
              {
                name: 'p0',
                type: 'java.lang.String',
                desc: 'p0',
              },
            ],
            ret: 'java.lang.String',
            desc: 'getLoggerLevel',
          },
          getParentLoggerName: {
            args: [{ name: 'p0', type: 'java.lang.String', desc: 'p0' }],
            ret: 'java.lang.String',
            desc: 'getParentLoggerName',
          },
          setLoggerLevel: {
            args: [
              { name: 'p0', type: 'java.lang.String', desc: 'p0' },
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'setLoggerLevel',
          },
        },
        attr: {
          LoggerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'LoggerNames',
          },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.ManagementFactoryHelper$PlatformLoggingImpl',
        desc: 'Information on the management interface of the MBean',
      },
    },
    'java.nio': {
      'name=direct,type=BufferPool': {
        attr: {
          TotalCapacity: {
            rw: false,
            type: 'long',
            desc: 'TotalCapacity',
          },
          MemoryUsed: { rw: false, type: 'long', desc: 'MemoryUsed' },
          Count: { rw: false, type: 'long', desc: 'Count' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.ManagementFactoryHelper$1',
        desc: 'Information on the management interface of the MBean',
      },
      'name=mapped,type=BufferPool': {
        attr: {
          TotalCapacity: {
            rw: false,
            type: 'long',
            desc: 'TotalCapacity',
          },
          MemoryUsed: { rw: false, type: 'long', desc: 'MemoryUsed' },
          Count: { rw: false, type: 'long', desc: 'Count' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.ManagementFactoryHelper$1',
        desc: 'Information on the management interface of the MBean',
      },
      "name=mapped - 'non-volatile memory',type=BufferPool": {
        attr: {
          TotalCapacity: {
            rw: false,
            type: 'long',
            desc: 'TotalCapacity',
          },
          MemoryUsed: { rw: false, type: 'long', desc: 'MemoryUsed' },
          Count: { rw: false, type: 'long', desc: 'Count' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.ManagementFactoryHelper$1',
        desc: 'Information on the management interface of the MBean',
      },
    },
    DefaultDomain: {
      'application=': {
        attr: {
          SnapshotAsJson: {
            rw: false,
            type: 'java.lang.String',
            desc: 'Attribute exposed for management',
          },
        },
        class: 'org.springframework.context.support.LiveBeansView',
        desc: 'Information on the management interface of the MBean',
      },
    },
    'org.springframework.boot': {
      'type=Endpoint,name=Scheduledtasks': {
        op: {
          scheduledTasks: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke scheduledTasks for endpoint scheduledtasks',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint scheduledtasks',
      },
      'type=Endpoint,name=Loggers': {
        op: {
          configureLogLevel: {
            args: [
              {
                name: 'name',
                type: 'java.lang.String',
                desc: null,
              },
              { name: 'configuredLevel', type: 'java.lang.String', desc: null },
            ],
            ret: 'java.util.Map',
            desc: 'Invoke configureLogLevel for endpoint loggers',
          },
          loggers: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke loggers for endpoint loggers',
          },
          loggerLevels: {
            args: [{ name: 'name', type: 'java.lang.String', desc: null }],
            ret: 'java.util.Map',
            desc: 'Invoke loggerLevels for endpoint loggers',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint loggers',
      },
      'type=Endpoint,name=Mappings': {
        op: {
          mappings: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke mappings for endpoint mappings',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint mappings',
      },
      'type=Endpoint,name=Features': {
        op: {
          features: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke features for endpoint features',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint features',
      },
      'type=Endpoint,name=Info': {
        op: {
          info: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke info for endpoint info',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint info',
      },
      'type=Endpoint,name=Env': {
        op: {
          environment: {
            args: [
              {
                name: 'pattern',
                type: 'java.lang.String',
                desc: null,
              },
            ],
            ret: 'java.util.Map',
            desc: 'Invoke environment for endpoint env',
          },
          environmentEntry: {
            args: [{ name: 'toMatch', type: 'java.lang.String', desc: null }],
            ret: 'java.util.Map',
            desc: 'Invoke environmentEntry for endpoint env',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint env',
      },
      'type=Endpoint,name=Caches': {
        op: {
          clearCaches: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke clearCaches for endpoint caches',
          },
          cache: {
            args: [
              { name: 'cache', type: 'java.lang.String', desc: null },
              {
                name: 'cacheManager',
                type: 'java.lang.String',
                desc: null,
              },
            ],
            ret: 'java.util.Map',
            desc: 'Invoke cache for endpoint caches',
          },
          caches: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke caches for endpoint caches',
          },
          clearCache: {
            args: [
              {
                name: 'cache',
                type: 'java.lang.String',
                desc: null,
              },
              { name: 'cacheManager', type: 'java.lang.String', desc: null },
            ],
            ret: 'java.util.Map',
            desc: 'Invoke clearCache for endpoint caches',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint caches',
      },
      'type=Endpoint,name=Beans': {
        op: {
          beans: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke beans for endpoint beans',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint beans',
      },
      'type=Endpoint,name=Refresh': {
        op: {
          refresh: {
            args: [],
            ret: 'java.util.List',
            desc: 'Invoke refresh for endpoint refresh',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint refresh',
      },
      'type=Endpoint,name=Flyway': {
        op: {
          flywayBeans: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke flywayBeans for endpoint flyway',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint flyway',
      },
      'type=Endpoint,name=Threaddump': {
        op: {
          threadDump: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke threadDump for endpoint threaddump',
          },
          textThreadDump: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Invoke textThreadDump for endpoint threaddump',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint threaddump',
      },
      'type=Endpoint,name=Metrics': {
        op: {
          metric: {
            args: [
              {
                name: 'requiredMetricName',
                type: 'java.lang.String',
                desc: null,
              },
              { name: 'tag', type: 'java.util.List', desc: null },
            ],
            ret: 'java.util.Map',
            desc: 'Invoke metric for endpoint metrics',
          },
          listNames: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke listNames for endpoint metrics',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint metrics',
      },
      'type=Endpoint,name=Configprops': {
        op: {
          configurationPropertiesWithPrefix: {
            args: [
              {
                name: 'prefix',
                type: 'java.lang.String',
                desc: null,
              },
            ],
            ret: 'java.util.Map',
            desc: 'Invoke configurationPropertiesWithPrefix for endpoint configprops',
          },
          configurationProperties: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke configurationProperties for endpoint configprops',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint configprops',
      },
      'type=Endpoint,name=Startup': {
        op: {
          startup: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke startup for endpoint startup',
          },
          startupSnapshot: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke startupSnapshot for endpoint startup',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint startup',
      },
      'type=Admin,name=SpringApplication': {
        op: {
          getProperty: {
            args: [
              {
                name: 'p0',
                type: 'java.lang.String',
                desc: 'p0',
              },
            ],
            ret: 'java.lang.String',
            desc: 'getProperty',
          },
          shutdown: { args: [], ret: 'void', desc: 'shutdown' },
        },
        attr: {
          Ready: { rw: false, type: 'boolean', desc: 'Ready' },
          EmbeddedWebApplication: {
            rw: false,
            type: 'boolean',
            desc: 'EmbeddedWebApplication',
          },
        },
        class:
          'org.springframework.boot.admin.SpringApplicationAdminMXBeanRegistrar$SpringApplicationAdmin',
        desc: 'Information on the management interface of the MBean',
      },
      'type=Endpoint,name=Liquibase': {
        op: {
          liquibaseBeans: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke liquibaseBeans for endpoint liquibase',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint liquibase',
      },
      'type=Endpoint,name=Conditions': {
        op: {
          applicationConditionEvaluation: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke applicationConditionEvaluation for endpoint conditions',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint conditions',
      },
      'type=Endpoint,name=Health': {
        op: {
          health: {
            args: [],
            ret: 'java.util.Map',
            desc: 'Invoke health for endpoint health',
          },
          healthForPath: {
            args: [{ name: 'path', type: 'java.lang.Object', desc: null }],
            ret: 'java.util.Map',
            desc: 'Invoke healthForPath for endpoint health',
          },
        },
        class: 'org.springframework.boot.actuate.endpoint.jmx.EndpointMBean',
        desc: 'MBean operations for endpoint health',
      },
    },
    JMImplementation: {
      'type=MBeanServerDelegate': {
        attr: {
          ImplementationName: {
            rw: false,
            type: 'java.lang.String',
            desc: 'The JMX implementation name (the name of this product)',
          },
          MBeanServerId: {
            rw: false,
            type: 'java.lang.String',
            desc: 'The MBean server agent identification',
          },
          ImplementationVersion: {
            rw: false,
            type: 'java.lang.String',
            desc: 'The JMX implementation version (the version of this product).',
          },
          SpecificationVersion: {
            rw: false,
            type: 'java.lang.String',
            desc: 'The version of the JMX specification implemented by this product.',
          },
          SpecificationVendor: {
            rw: false,
            type: 'java.lang.String',
            desc: 'The vendor of the JMX specification implemented by this product.',
          },
          SpecificationName: {
            rw: false,
            type: 'java.lang.String',
            desc: 'The full name of the JMX specification implemented by this product.',
          },
          ImplementationVendor: {
            rw: false,
            type: 'java.lang.String',
            desc: 'the JMX implementation vendor (the vendor of this product).',
          },
        },
        class: 'javax.management.MBeanServerDelegate',
        desc: 'Represents  the MBean server from the management point of view.',
      },
    },
    'java.lang': {
      'name=G1 Survivor Space,type=MemoryPool': {
        op: {
          resetPeakUsage: { args: [], ret: 'void', desc: 'resetPeakUsage' },
        },
        attr: {
          Usage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'Usage',
          },
          UsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'UsageThresholdCount',
          },
          MemoryManagerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryManagerNames',
          },
          UsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdSupported',
          },
          UsageThreshold: { rw: true, type: 'long', desc: 'UsageThreshold' },
          CollectionUsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'CollectionUsageThresholdCount',
          },
          PeakUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'PeakUsage',
          },
          UsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdExceeded',
          },
          CollectionUsageThreshold: {
            rw: true,
            type: 'long',
            desc: 'CollectionUsageThreshold',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          Type: { rw: false, type: 'java.lang.String', desc: 'Type' },
          CollectionUsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdSupported',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'CollectionUsage',
          },
          CollectionUsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdExceeded',
          },
        },
        class: 'sun.management.MemoryPoolImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'type=Threading': {
        op: {
          getThreadCpuTime: [
            {
              args: [{ name: 'p0', type: '[J', desc: 'p0' }],
              ret: '[J',
              desc: 'getThreadCpuTime',
            },
            {
              args: [{ name: 'p0', type: 'long', desc: 'p0' }],
              ret: 'long',
              desc: 'getThreadCpuTime',
            },
          ],
          getThreadInfo: [
            {
              args: [{ name: 'p0', type: 'long', desc: 'p0' }],
              ret: 'javax.management.openmbean.CompositeData',
              desc: 'getThreadInfo',
            },
            {
              args: [{ name: 'p0', type: '[J', desc: 'p0' }],
              ret: '[Ljavax.management.openmbean.CompositeData;',
              desc: 'getThreadInfo',
            },
            {
              args: [
                { name: 'p0', type: '[J', desc: 'p0' },
                {
                  name: 'p1',
                  type: 'boolean',
                  desc: 'p1',
                },
                { name: 'p2', type: 'boolean', desc: 'p2' },
                { name: 'p3', type: 'int', desc: 'p3' },
              ],
              ret: '[Ljavax.management.openmbean.CompositeData;',
              desc: 'getThreadInfo',
            },
            {
              args: [
                { name: 'p0', type: '[J', desc: 'p0' },
                {
                  name: 'p1',
                  type: 'boolean',
                  desc: 'p1',
                },
                { name: 'p2', type: 'boolean', desc: 'p2' },
              ],
              ret: '[Ljavax.management.openmbean.CompositeData;',
              desc: 'getThreadInfo',
            },
            {
              args: [
                { name: 'p0', type: '[J', desc: 'p0' },
                { name: 'p1', type: 'int', desc: 'p1' },
              ],
              ret: '[Ljavax.management.openmbean.CompositeData;',
              desc: 'getThreadInfo',
            },
            {
              args: [
                { name: 'p0', type: 'long', desc: 'p0' },
                { name: 'p1', type: 'int', desc: 'p1' },
              ],
              ret: 'javax.management.openmbean.CompositeData',
              desc: 'getThreadInfo',
            },
          ],
          findDeadlockedThreads: {
            args: [],
            ret: '[J',
            desc: 'findDeadlockedThreads',
          },
          getThreadAllocatedBytes: [
            {
              args: [{ name: 'p0', type: '[J', desc: 'p0' }],
              ret: '[J',
              desc: 'getThreadAllocatedBytes',
            },
            {
              args: [{ name: 'p0', type: 'long', desc: 'p0' }],
              ret: 'long',
              desc: 'getThreadAllocatedBytes',
            },
          ],
          getThreadUserTime: [
            {
              args: [{ name: 'p0', type: '[J', desc: 'p0' }],
              ret: '[J',
              desc: 'getThreadUserTime',
            },
            {
              args: [{ name: 'p0', type: 'long', desc: 'p0' }],
              ret: 'long',
              desc: 'getThreadUserTime',
            },
          ],
          findMonitorDeadlockedThreads: {
            args: [],
            ret: '[J',
            desc: 'findMonitorDeadlockedThreads',
          },
          resetPeakThreadCount: {
            args: [],
            ret: 'void',
            desc: 'resetPeakThreadCount',
          },
          dumpAllThreads: [
            {
              args: [
                { name: 'p0', type: 'boolean', desc: 'p0' },
                {
                  name: 'p1',
                  type: 'boolean',
                  desc: 'p1',
                },
              ],
              ret: '[Ljavax.management.openmbean.CompositeData;',
              desc: 'dumpAllThreads',
            },
            {
              args: [
                { name: 'p0', type: 'boolean', desc: 'p0' },
                {
                  name: 'p1',
                  type: 'boolean',
                  desc: 'p1',
                },
                { name: 'p2', type: 'int', desc: 'p2' },
              ],
              ret: '[Ljavax.management.openmbean.CompositeData;',
              desc: 'dumpAllThreads',
            },
          ],
        },
        attr: {
          ThreadAllocatedMemorySupported: {
            rw: false,
            type: 'boolean',
            desc: 'ThreadAllocatedMemorySupported',
          },
          ThreadContentionMonitoringEnabled: {
            rw: true,
            type: 'boolean',
            desc: 'ThreadContentionMonitoringEnabled',
          },
          CurrentThreadAllocatedBytes: {
            rw: false,
            type: 'long',
            desc: 'CurrentThreadAllocatedBytes',
          },
          TotalStartedThreadCount: {
            rw: false,
            type: 'long',
            desc: 'TotalStartedThreadCount',
          },
          CurrentThreadCpuTimeSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CurrentThreadCpuTimeSupported',
          },
          CurrentThreadUserTime: {
            rw: false,
            type: 'long',
            desc: 'CurrentThreadUserTime',
          },
          PeakThreadCount: { rw: false, type: 'int', desc: 'PeakThreadCount' },
          AllThreadIds: { rw: false, type: '[J', desc: 'AllThreadIds' },
          ThreadAllocatedMemoryEnabled: {
            rw: true,
            type: 'boolean',
            desc: 'ThreadAllocatedMemoryEnabled',
          },
          CurrentThreadCpuTime: {
            rw: false,
            type: 'long',
            desc: 'CurrentThreadCpuTime',
          },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          ThreadContentionMonitoringSupported: {
            rw: false,
            type: 'boolean',
            desc: 'ThreadContentionMonitoringSupported',
          },
          ThreadCpuTimeSupported: {
            rw: false,
            type: 'boolean',
            desc: 'ThreadCpuTimeSupported',
          },
          ThreadCount: { rw: false, type: 'int', desc: 'ThreadCount' },
          ThreadCpuTimeEnabled: {
            rw: true,
            type: 'boolean',
            desc: 'ThreadCpuTimeEnabled',
          },
          ObjectMonitorUsageSupported: {
            rw: false,
            type: 'boolean',
            desc: 'ObjectMonitorUsageSupported',
          },
          SynchronizerUsageSupported: {
            rw: false,
            type: 'boolean',
            desc: 'SynchronizerUsageSupported',
          },
          DaemonThreadCount: {
            rw: false,
            type: 'int',
            desc: 'DaemonThreadCount',
          },
        },
        class: 'com.sun.management.internal.HotSpotThreadImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=CodeCache,type=MemoryPool': {
        op: {
          resetPeakUsage: { args: [], ret: 'void', desc: 'resetPeakUsage' },
        },
        attr: {
          Usage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'Usage',
          },
          UsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'UsageThresholdCount',
          },
          MemoryManagerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryManagerNames',
          },
          UsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdSupported',
          },
          UsageThreshold: { rw: true, type: 'long', desc: 'UsageThreshold' },
          CollectionUsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'CollectionUsageThresholdCount',
          },
          PeakUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'PeakUsage',
          },
          UsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdExceeded',
          },
          CollectionUsageThreshold: {
            rw: true,
            type: 'long',
            desc: 'CollectionUsageThreshold',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          Type: { rw: false, type: 'java.lang.String', desc: 'Type' },
          CollectionUsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdSupported',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'CollectionUsage',
          },
          CollectionUsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdExceeded',
          },
        },
        class: 'sun.management.MemoryPoolImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'type=Memory': {
        op: { gc: { args: [], ret: 'void', desc: 'gc' } },
        attr: {
          ObjectPendingFinalizationCount: {
            rw: false,
            type: 'int',
            desc: 'ObjectPendingFinalizationCount',
          },
          Verbose: { rw: true, type: 'boolean', desc: 'Verbose' },
          HeapMemoryUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'HeapMemoryUsage',
          },
          NonHeapMemoryUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'NonHeapMemoryUsage',
          },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.MemoryImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=Metaspace,type=MemoryPool': {
        op: {
          resetPeakUsage: { args: [], ret: 'void', desc: 'resetPeakUsage' },
        },
        attr: {
          Usage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'Usage',
          },
          UsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'UsageThresholdCount',
          },
          MemoryManagerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryManagerNames',
          },
          UsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdSupported',
          },
          UsageThreshold: { rw: true, type: 'long', desc: 'UsageThreshold' },
          CollectionUsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'CollectionUsageThresholdCount',
          },
          PeakUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'PeakUsage',
          },
          UsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdExceeded',
          },
          CollectionUsageThreshold: {
            rw: true,
            type: 'long',
            desc: 'CollectionUsageThreshold',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          Type: { rw: false, type: 'java.lang.String', desc: 'Type' },
          CollectionUsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdSupported',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'CollectionUsage',
          },
          CollectionUsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdExceeded',
          },
        },
        class: 'sun.management.MemoryPoolImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=G1 Eden Space,type=MemoryPool': {
        op: {
          resetPeakUsage: { args: [], ret: 'void', desc: 'resetPeakUsage' },
        },
        attr: {
          Usage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'Usage',
          },
          UsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'UsageThresholdCount',
          },
          MemoryManagerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryManagerNames',
          },
          UsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdSupported',
          },
          UsageThreshold: { rw: true, type: 'long', desc: 'UsageThreshold' },
          CollectionUsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'CollectionUsageThresholdCount',
          },
          PeakUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'PeakUsage',
          },
          UsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdExceeded',
          },
          CollectionUsageThreshold: {
            rw: true,
            type: 'long',
            desc: 'CollectionUsageThreshold',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          Type: { rw: false, type: 'java.lang.String', desc: 'Type' },
          CollectionUsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdSupported',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'CollectionUsage',
          },
          CollectionUsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdExceeded',
          },
        },
        class: 'sun.management.MemoryPoolImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'type=OperatingSystem': {
        attr: {
          OpenFileDescriptorCount: {
            rw: false,
            type: 'long',
            desc: 'OpenFileDescriptorCount',
          },
          CommittedVirtualMemorySize: {
            rw: false,
            type: 'long',
            desc: 'CommittedVirtualMemorySize',
          },
          FreePhysicalMemorySize: {
            rw: false,
            type: 'long',
            desc: 'FreePhysicalMemorySize',
          },
          SystemLoadAverage: {
            rw: false,
            type: 'double',
            desc: 'SystemLoadAverage',
          },
          Arch: { rw: false, type: 'java.lang.String', desc: 'Arch' },
          ProcessCpuLoad: { rw: false, type: 'double', desc: 'ProcessCpuLoad' },
          FreeSwapSpaceSize: {
            rw: false,
            type: 'long',
            desc: 'FreeSwapSpaceSize',
          },
          TotalPhysicalMemorySize: {
            rw: false,
            type: 'long',
            desc: 'TotalPhysicalMemorySize',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          TotalSwapSpaceSize: {
            rw: false,
            type: 'long',
            desc: 'TotalSwapSpaceSize',
          },
          TotalMemorySize: { rw: false, type: 'long', desc: 'TotalMemorySize' },
          ProcessCpuTime: { rw: false, type: 'long', desc: 'ProcessCpuTime' },
          MaxFileDescriptorCount: {
            rw: false,
            type: 'long',
            desc: 'MaxFileDescriptorCount',
          },
          SystemCpuLoad: { rw: false, type: 'double', desc: 'SystemCpuLoad' },
          Version: { rw: false, type: 'java.lang.String', desc: 'Version' },
          AvailableProcessors: {
            rw: false,
            type: 'int',
            desc: 'AvailableProcessors',
          },
          CpuLoad: { rw: false, type: 'double', desc: 'CpuLoad' },
          FreeMemorySize: { rw: false, type: 'long', desc: 'FreeMemorySize' },
        },
        class: 'com.sun.management.internal.OperatingSystemImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=CodeCacheManager,type=MemoryManager': {
        attr: {
          MemoryPoolNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryPoolNames',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.MemoryManagerImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=G1 Old Gen,type=MemoryPool': {
        op: {
          resetPeakUsage: { args: [], ret: 'void', desc: 'resetPeakUsage' },
        },
        attr: {
          Usage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'Usage',
          },
          UsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'UsageThresholdCount',
          },
          MemoryManagerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryManagerNames',
          },
          UsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdSupported',
          },
          UsageThreshold: { rw: true, type: 'long', desc: 'UsageThreshold' },
          CollectionUsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'CollectionUsageThresholdCount',
          },
          PeakUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'PeakUsage',
          },
          UsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdExceeded',
          },
          CollectionUsageThreshold: {
            rw: true,
            type: 'long',
            desc: 'CollectionUsageThreshold',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          Type: { rw: false, type: 'java.lang.String', desc: 'Type' },
          CollectionUsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdSupported',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'CollectionUsage',
          },
          CollectionUsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdExceeded',
          },
        },
        class: 'sun.management.MemoryPoolImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=Compressed Class Space,type=MemoryPool': {
        op: {
          resetPeakUsage: { args: [], ret: 'void', desc: 'resetPeakUsage' },
        },
        attr: {
          Usage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'Usage',
          },
          UsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'UsageThresholdCount',
          },
          MemoryManagerNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryManagerNames',
          },
          UsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdSupported',
          },
          UsageThreshold: { rw: true, type: 'long', desc: 'UsageThreshold' },
          CollectionUsageThresholdCount: {
            rw: false,
            type: 'long',
            desc: 'CollectionUsageThresholdCount',
          },
          PeakUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'PeakUsage',
          },
          UsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'UsageThresholdExceeded',
          },
          CollectionUsageThreshold: {
            rw: true,
            type: 'long',
            desc: 'CollectionUsageThreshold',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          Type: { rw: false, type: 'java.lang.String', desc: 'Type' },
          CollectionUsageThresholdSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdSupported',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionUsage: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'CollectionUsage',
          },
          CollectionUsageThresholdExceeded: {
            rw: false,
            type: 'boolean',
            desc: 'CollectionUsageThresholdExceeded',
          },
        },
        class: 'sun.management.MemoryPoolImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=G1 Old Generation,type=GarbageCollector': {
        attr: {
          MemoryPoolNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryPoolNames',
          },
          LastGcInfo: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'LastGcInfo',
          },
          CollectionTime: { rw: false, type: 'long', desc: 'CollectionTime' },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionCount: { rw: false, type: 'long', desc: 'CollectionCount' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'com.sun.management.internal.GarbageCollectorExtImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'type=ClassLoading': {
        attr: {
          UnloadedClassCount: {
            rw: false,
            type: 'long',
            desc: 'UnloadedClassCount',
          },
          LoadedClassCount: {
            rw: false,
            type: 'int',
            desc: 'LoadedClassCount',
          },
          Verbose: { rw: true, type: 'boolean', desc: 'Verbose' },
          TotalLoadedClassCount: {
            rw: false,
            type: 'long',
            desc: 'TotalLoadedClassCount',
          },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.ClassLoadingImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=G1 Young Generation,type=GarbageCollector': {
        attr: {
          MemoryPoolNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryPoolNames',
          },
          LastGcInfo: {
            rw: false,
            type: 'javax.management.openmbean.CompositeData',
            desc: 'LastGcInfo',
          },
          CollectionTime: { rw: false, type: 'long', desc: 'CollectionTime' },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          CollectionCount: { rw: false, type: 'long', desc: 'CollectionCount' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'com.sun.management.internal.GarbageCollectorExtImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'type=Compilation': {
        attr: {
          CompilationTimeMonitoringSupported: {
            rw: false,
            type: 'boolean',
            desc: 'CompilationTimeMonitoringSupported',
          },
          TotalCompilationTime: {
            rw: false,
            type: 'long',
            desc: 'TotalCompilationTime',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.CompilationImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'name=Metaspace Manager,type=MemoryManager': {
        attr: {
          MemoryPoolNames: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'MemoryPoolNames',
          },
          Valid: { rw: false, type: 'boolean', desc: 'Valid' },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'sun.management.MemoryManagerImpl',
        desc: 'Information on the management interface of the MBean',
      },
      'type=Runtime': {
        attr: {
          SpecVendor: {
            rw: false,
            type: 'java.lang.String',
            desc: 'SpecVendor',
          },
          ClassPath: { rw: false, type: 'java.lang.String', desc: 'ClassPath' },
          InputArguments: {
            rw: false,
            type: '[Ljava.lang.String;',
            desc: 'InputArguments',
          },
          Uptime: { rw: false, type: 'long', desc: 'Uptime' },
          VmName: { rw: false, type: 'java.lang.String', desc: 'VmName' },
          StartTime: { rw: false, type: 'long', desc: 'StartTime' },
          VmVersion: { rw: false, type: 'java.lang.String', desc: 'VmVersion' },
          SpecName: { rw: false, type: 'java.lang.String', desc: 'SpecName' },
          Pid: { rw: false, type: 'long', desc: 'Pid' },
          ManagementSpecVersion: {
            rw: false,
            type: 'java.lang.String',
            desc: 'ManagementSpecVersion',
          },
          Name: { rw: false, type: 'java.lang.String', desc: 'Name' },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
          VmVendor: { rw: false, type: 'java.lang.String', desc: 'VmVendor' },
          LibraryPath: {
            rw: false,
            type: 'java.lang.String',
            desc: 'LibraryPath',
          },
          BootClassPath: {
            rw: false,
            type: 'java.lang.String',
            desc: 'BootClassPath',
          },
          SpecVersion: {
            rw: false,
            type: 'java.lang.String',
            desc: 'SpecVersion',
          },
          SystemProperties: {
            rw: false,
            type: 'javax.management.openmbean.TabularData',
            desc: 'SystemProperties',
          },
          BootClassPathSupported: {
            rw: false,
            type: 'boolean',
            desc: 'BootClassPathSupported',
          },
        },
        class: 'sun.management.RuntimeImpl',
        desc: 'Information on the management interface of the MBean',
      },
    },
    'org.springframework.cloud.context.properties': {
      'name=configurationPropertiesRebinder,type=ConfigurationPropertiesRebinder':
        {
          op: {
            getNeverRefreshable: {
              args: [],
              ret: 'java.util.Set',
              desc: 'getNeverRefreshable',
            },
            getBeanNames: {
              args: [],
              ret: 'java.util.Set',
              desc: 'getBeanNames',
            },
            rebind: [
              {
                args: [
                  { name: 'name', type: 'java.lang.String', desc: 'name' },
                ],
                ret: 'boolean',
                desc: 'rebind',
              },
              { args: [], ret: 'void', desc: 'rebind' },
            ],
          },
          attr: {
            NeverRefreshable: {
              rw: false,
              type: 'java.util.Set',
              desc: 'neverRefreshable',
            },
            BeanNames: { rw: false, type: 'java.util.Set', desc: 'beanNames' },
          },
          class:
            'org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder',
          desc: '',
        },
    },
    'org.springframework.cloud.context.scope.refresh': {
      'name=refreshScope,type=RefreshScope': {
        op: {
          refreshAll: {
            args: [],
            ret: 'void',
            desc: 'Dispose of the current instance of all beans in this scope and force a refresh on next method execution.',
          },
          refresh: {
            args: [{ name: 'name', type: 'java.lang.String', desc: 'name' }],
            ret: 'boolean',
            desc: 'Dispose of the current instance of bean name provided and force a refresh on next method execution.',
          },
        },
        class: 'org.springframework.cloud.context.scope.refresh.RefreshScope',
        desc: '',
      },
    },
    'com.sun.management': {
      'type=HotSpotDiagnostic': {
        op: {
          setVMOption: {
            args: [
              {
                name: 'p0',
                type: 'java.lang.String',
                desc: 'p0',
              },
              { name: 'p1', type: 'java.lang.String', desc: 'p1' },
            ],
            ret: 'void',
            desc: 'setVMOption',
          },
          getVMOption: {
            args: [{ name: 'p0', type: 'java.lang.String', desc: 'p0' }],
            ret: 'javax.management.openmbean.CompositeData',
            desc: 'getVMOption',
          },
          dumpHeap: {
            args: [
              { name: 'p0', type: 'java.lang.String', desc: 'p0' },
              {
                name: 'p1',
                type: 'boolean',
                desc: 'p1',
              },
            ],
            ret: 'void',
            desc: 'dumpHeap',
          },
        },
        attr: {
          DiagnosticOptions: {
            rw: false,
            type: '[Ljavax.management.openmbean.CompositeData;',
            desc: 'DiagnosticOptions',
          },
          ObjectName: {
            rw: false,
            type: 'javax.management.ObjectName',
            desc: 'ObjectName',
          },
        },
        class: 'com.sun.management.internal.HotSpotDiagnostic',
        desc: 'Information on the management interface of the MBean',
      },
      'type=DiagnosticCommand': {
        op: {
          vmUptime: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print VM uptime.',
          },
          jfrDump: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Copies contents of a JFR recording to file. Either the name or the recording id must be specified.',
          },
          jfrStart: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Starts a new JFR recording',
          },
          threadPrint: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print all threads with stacktraces.',
          },
          jfrStop: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Stops a JFR recording',
          },
          vmCds: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Dump a static or dynamic shared archive including all shareable classes',
          },
          compilerCodelist: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print all compiled methods in code cache that are alive',
          },
          vmEvents: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print VM event logs',
          },
          jfrCheck: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Checks running JFR recording(s)',
          },
          vmSymboltable: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Dump symbol table.',
          },
          gcRun: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Call java.lang.System.gc().',
          },
          vmClassloaders: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Prints classloader hierarchy.',
          },
          vmMetaspace: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Prints the statistics for the metaspace',
          },
          compilerDirectivesPrint: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print all active compiler directives.',
          },
          vmSetFlag: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Sets VM flag option using the provided value.',
          },
          compilerDirectivesAdd: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Add compiler directives from file.',
          },
          vmDynlibs: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print loaded dynamic libraries.',
          },
          vmPrintTouchedMethods: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print all methods that have ever been touched during the lifetime of this JVM.',
          },
          compilerCodecache: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print code cache layout and bounds.',
          },
          vmNativeMemory: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print native memory usage',
          },
          gcClassHistogram: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Provide statistics about the Java heap usage.',
          },
          gcRunFinalization: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Call java.lang.System.runFinalization().',
          },
          jvmtiDataDump: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Signal the JVM to do a data-dump request for JVMTI.',
          },
          gcFinalizerInfo: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Provide information about Java finalization queue.',
          },
          vmStringtable: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Dump string table.',
          },
          help: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: "For more information about a specific command use 'help <command>'. With no argument this will show a list of available commands. 'help all' will show help for all commands.",
          },
          jfrConfigure: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Configure JFR',
          },
          vmSystemProperties: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print system properties.',
          },
          compilerDirectivesClear: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Remove all compiler directives.',
          },
          vmSystemdictionary: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Prints the statistics for dictionary hashtable sizes and bucket length',
          },
          vmClassloaderStats: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print statistics about all ClassLoaders.',
          },
          compilerDirectivesRemove: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Remove latest added compiler directive.',
          },
          gcHeapInfo: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Provide generic Java heap information.',
          },
          compilerCodeHeapAnalytics: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print CodeHeap analytics',
          },
          vmVersion: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print JVM version information.',
          },
          vmInfo: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print information about JVM environment and status.',
          },
          compilerQueue: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print methods queued for compilation.',
          },
          vmFlags: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print VM flag options and their current values.',
          },
          vmLog: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Lists current log configuration, enables/disables/configures a log output, or rotates all logs.',
          },
          jvmtiAgentLoad: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Load JVMTI native agent.',
          },
          vmClassHierarchy: {
            args: [
              {
                name: 'arguments',
                type: '[Ljava.lang.String;',
                desc: 'Array of Diagnostic Commands Arguments and Options',
              },
            ],
            ret: 'java.lang.String',
            desc: 'Print a list of all loaded classes, indented to show the class hiearchy. The name of each class is followed by the ClassLoaderData* of its ClassLoader, or "null" if loaded by the bootstrap class loader.',
          },
          vmCommandLine: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Print the command line used to start this VM instance.',
          },
        },
        class: 'com.sun.management.internal.DiagnosticCommandImpl',
        desc: 'Diagnostic Commands',
      },
    },
    jmx4perl: {
      'type=Config': {
        op: {
          setHistoryEntriesForOperation: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'int', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          setHistoryLimitForOperation: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'int', desc: '' },
              { name: 'p5', type: 'long', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          resetDebugInfo: {
            args: [],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          resetHistoryEntries: {
            args: [],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          setHistoryEntriesForAttribute: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'java.lang.String', desc: '' },
              { name: 'p5', type: 'int', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          setHistoryLimitForAttribute: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'java.lang.String', desc: '' },
              {
                name: 'p5',
                type: 'int',
                desc: '',
              },
              { name: 'p6', type: 'long', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          debugInfo: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Operation exposed for management',
          },
        },
        attr: {
          HistorySize: {
            rw: false,
            type: 'int',
            desc: 'Attribute exposed for management',
          },
          MaxDebugEntries: {
            rw: true,
            type: 'int',
            desc: 'Attribute exposed for management',
          },
          HistoryMaxEntries: {
            rw: true,
            type: 'int',
            desc: 'Attribute exposed for management',
          },
          Debug: {
            rw: true,
            type: 'boolean',
            desc: 'Attribute exposed for management',
          },
        },
        class: 'org.jolokia.backend.Config',
        desc: 'Information on the management interface of the MBean',
      },
    },
    'com.zaxxer.hikari': {
      'name=dataSource,type=HikariDataSource': {
        attr: {
          MaxLifetime: {
            rw: true,
            type: 'long',
            desc: 'MaxLifetime',
          },
          ConnectionTimeout: {
            rw: true,
            type: 'long',
            desc: 'ConnectionTimeout',
          },
          MaximumPoolSize: { rw: true, type: 'int', desc: 'MaximumPoolSize' },
          PoolName: { rw: false, type: 'java.lang.String', desc: 'PoolName' },
          Username: { rw: false, type: 'java.lang.String', desc: 'Username' },
          IdleTimeout: { rw: true, type: 'long', desc: 'IdleTimeout' },
          LeakDetectionThreshold: {
            rw: true,
            type: 'long',
            desc: 'LeakDetectionThreshold',
          },
          ValidationTimeout: {
            rw: true,
            type: 'long',
            desc: 'ValidationTimeout',
          },
          Catalog: { rw: true, type: 'java.lang.String', desc: 'Catalog' },
          Password: { rw: false, type: 'java.lang.String', desc: 'Password' },
          MinimumIdle: { rw: true, type: 'int', desc: 'MinimumIdle' },
        },
        class: 'com.zaxxer.hikari.HikariDataSource',
        desc: 'Information on the management interface of the MBean',
      },
    },
    'com.codecentric.boot.sample': {
      'name=stringMapManagedBean.StringSetter,type=StringMapManagedBean.StringSetter':
        {
          op: {
            getConfigKeys: {
              args: [],
              ret: 'java.util.List',
              desc: 'getConfigKeys',
            },
            setTest: {
              args: [{ name: 'test', type: 'java.lang.String', desc: 'test' }],
              ret: 'void',
              desc: 'Set the value',
            },
            getTest: {
              args: [],
              ret: 'java.lang.String',
              desc: 'Get the value',
            },
          },
          attr: {
            Test: { rw: true, type: 'java.lang.String', desc: 'Get the value' },
            ConfigKeys: {
              rw: false,
              type: 'java.util.List',
              desc: 'configKeys',
            },
          },
          class:
            'com.codecentric.boot.sample.StringMapManagedBean$StringSetter',
          desc: 'String Setter MBean inside other MBEAN',
        },
      'name=stringMapManagedBean,type=StringMapManagedBean': {
        op: {
          getSize: {
            args: [],
            ret: 'int',
            desc: 'getSize',
          },
          setTest: {
            args: [{ name: 'test', type: 'int', desc: 'test' }],
            ret: 'void',
            desc: 'Set the value of the test instance variable',
          },
          get: {
            args: [{ name: 'key', type: 'java.lang.String', desc: 'key' }],
            ret: 'java.lang.String',
            desc: 'get',
          },
          getTest: {
            args: [],
            ret: 'int',
            desc: 'Get the value of the test instance variable',
          },
          put: {
            args: [
              { name: 'key', type: 'java.lang.String', desc: 'key' },
              {
                name: 'value',
                type: 'java.lang.String',
                desc: 'value',
              },
            ],
            ret: 'java.lang.String',
            desc: 'PUT DESCRIPTION',
          },
        },
        attr: {
          Test: {
            rw: true,
            type: 'int',
            desc: 'Get the value of the test instance variable',
          },
          Size: { rw: false, type: 'int', desc: 'size' },
        },
        class: 'com.codecentric.boot.sample.StringMapManagedBean',
        desc: '',
      },
    },
    'org.springframework.cloud.context.environment': {
      'name=environmentManager,type=EnvironmentManager': {
        op: {
          getProperty: {
            args: [
              {
                name: 'name',
                type: 'java.lang.String',
                desc: 'name',
              },
            ],
            ret: 'java.lang.Object',
            desc: 'getProperty',
          },
          setProperty: {
            args: [
              { name: 'name', type: 'java.lang.String', desc: 'name' },
              {
                name: 'value',
                type: 'java.lang.String',
                desc: 'value',
              },
            ],
            ret: 'void',
            desc: 'setProperty',
          },
          reset: { args: [], ret: 'java.util.Map', desc: 'reset' },
        },
        class:
          'org.springframework.cloud.context.environment.EnvironmentManager',
        desc: '',
      },
    },
    jolokia: {
      'type=Discovery': {
        op: {
          lookupAgentsWithTimeout: {
            args: [{ name: 'p1', type: 'int', desc: '' }],
            ret: 'java.util.List',
            desc: 'Operation exposed for management',
          },
          lookupAgentsWithTimeoutAndMulticastAddress: {
            args: [
              {
                name: 'p1',
                type: 'int',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              { name: 'p3', type: 'int', desc: '' },
            ],
            ret: 'java.util.List',
            desc: 'Operation exposed for management',
          },
          lookupAgents: {
            args: [],
            ret: 'java.util.List',
            desc: 'Operation exposed for management',
          },
        },
        class: 'org.jolokia.discovery.JolokiaDiscovery',
        desc: 'Information on the management interface of the MBean',
      },
      'type=ServerHandler': {
        op: {
          mBeanServersInfo: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Operation exposed for management',
          },
        },
        class: 'org.jolokia.backend.MBeanServerHandler',
        desc: 'Information on the management interface of the MBean',
      },
      'type=Config': {
        op: {
          setHistoryEntriesForOperation: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'int', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          setHistoryLimitForOperation: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'int', desc: '' },
              { name: 'p5', type: 'long', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          resetDebugInfo: {
            args: [],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          resetHistoryEntries: {
            args: [],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          setHistoryEntriesForAttribute: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'java.lang.String', desc: '' },
              { name: 'p5', type: 'int', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          setHistoryLimitForAttribute: {
            args: [
              {
                name: 'p1',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p2', type: 'java.lang.String', desc: '' },
              {
                name: 'p3',
                type: 'java.lang.String',
                desc: '',
              },
              { name: 'p4', type: 'java.lang.String', desc: '' },
              {
                name: 'p5',
                type: 'int',
                desc: '',
              },
              { name: 'p6', type: 'long', desc: '' },
            ],
            ret: 'void',
            desc: 'Operation exposed for management',
          },
          debugInfo: {
            args: [],
            ret: 'java.lang.String',
            desc: 'Operation exposed for management',
          },
        },
        attr: {
          HistorySize: {
            rw: false,
            type: 'int',
            desc: 'Attribute exposed for management',
          },
          MaxDebugEntries: {
            rw: true,
            type: 'int',
            desc: 'Attribute exposed for management',
          },
          HistoryMaxEntries: {
            rw: true,
            type: 'int',
            desc: 'Attribute exposed for management',
          },
          Debug: {
            rw: true,
            type: 'boolean',
            desc: 'Attribute exposed for management',
          },
        },
        class: 'org.jolokia.backend.Config',
        desc: 'Information on the management interface of the MBean',
      },
    },
  },
  timestamp: 1673711911,
  status: 200,
};
