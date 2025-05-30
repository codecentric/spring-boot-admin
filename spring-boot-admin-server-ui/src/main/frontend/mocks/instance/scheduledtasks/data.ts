export const scheduledtasksResponse = {
  cron: [
    {
      runnable: {
        target: 'com.example.Processor.processOrders',
      },
      expression: '0 0 0/3 1/1 * ?',
      nextExecution: {
        time: '2025-05-22T20:59:59.999087966Z',
      },
    },
  ],
  fixedDelay: [
    {
      runnable: {
        target: 'com.example.Processor.purge',
      },
      initialDelay: 0,
      interval: 5000,
      nextExecution: {
        time: '2025-05-22T20:03:39.767908889Z',
      },
      lastExecution: {
        time: '2025-05-22T20:03:34.761508282Z',
        status: 'SUCCESS',
      },
    },
  ],
  fixedRate: [
    {
      runnable: {
        target: 'com.example.Processor.retrieveIssues',
      },
      initialDelay: 10000,
      interval: 3000,
      nextExecution: {
        time: '2025-05-22T20:03:44.755151650Z',
      },
    },
  ],
  custom: [
    {
      runnable: {
        target: 'com.example.Processor$CustomTriggeredRunnable@6e5b7446',
      },
      trigger: 'com.example.Processor$CustomTrigger@513ad29e',
      lastExecution: {
        exception: {
          message: 'Failed while running custom task',
          type: 'java.lang.IllegalStateException',
        },
        time: '2025-05-22T20:03:34.807437342Z',
        status: 'ERROR',
      },
    },
  ],
};
