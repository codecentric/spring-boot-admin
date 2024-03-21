export const memoryMaxResponse = {
  name: 'jvm.memory.max',
  description:
    'The maximum amount of memory in bytes that can be used for memory management',
  baseUnit: 'bytes',
  measurements: [
    {
      statistic: 'VALUE',
      value: 8589934590,
    },
  ],
  availableTags: [
    {
      tag: 'id',
      values: ['G1 Old Gen', 'G1 Survivor Space', 'G1 Eden Space'],
    },
  ],
};

export const memoryUsedResponse = {
  name: 'jvm.memory.used',
  description: 'The amount of used memory',
  baseUnit: 'bytes',
  measurements: [
    {
      statistic: 'VALUE',
      value: 115390832,
    },
  ],
  availableTags: [
    {
      tag: 'id',
      values: ['G1 Survivor Space', 'G1 Old Gen', 'G1 Eden Space', 'Metaspace'],
    },
  ],
};

export const memoryCommittedResponse = {
  name: 'jvm.memory.committed',
  description:
    'The amount of memory in bytes that is committed for the Java virtual machine to use',
  baseUnit: 'bytes',
  measurements: [
    {
      statistic: 'VALUE',
      value: 197132288,
    },
  ],
  availableTags: [
    {
      tag: 'id',
      values: ['G1 Survivor Space', 'G1 Old Gen', 'G1 Eden Space'],
    },
  ],
};
