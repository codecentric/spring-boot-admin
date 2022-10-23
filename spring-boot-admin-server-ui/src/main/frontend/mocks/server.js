import { setupServer } from 'msw/node';

import mappingsEndpoint from './instance/mappings/index.js';

export const server = setupServer(...[...mappingsEndpoint]);
