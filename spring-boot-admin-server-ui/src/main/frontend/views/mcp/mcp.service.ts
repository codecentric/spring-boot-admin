export type ToolParam = {
  name: string;
  type: string;
  required: boolean;
  description: string;
};

export type McpTool = {
  name: string;
  description: string;
  params: ToolParam[];
};

export const CATEGORY_COLORS: Record<string, string> = {
  applications: 'bg-blue-100 text-blue-800',
  health: 'bg-green-100 text-green-800',
  metrics: 'bg-purple-100 text-purple-800',
  env: 'bg-yellow-100 text-yellow-800',
  logs: 'bg-orange-100 text-orange-800',
  operations: 'bg-red-100 text-red-800',
  loggers: 'bg-indigo-100 text-indigo-800',
  threadDump: 'bg-pink-100 text-pink-800',
  httpExchanges: 'bg-teal-100 text-teal-800',
  scheduledTasks: 'bg-cyan-100 text-cyan-800',
  caches: 'bg-lime-100 text-lime-800',
  beans: 'bg-amber-100 text-amber-800',
  other: 'bg-slate-100 text-slate-800',
};

export class McpService {
  static categoryOf(name: string): string {
    if (name.includes('application')) return 'applications';
    if (name.includes('health') || name.includes('status')) return 'health';
    if (name.includes('metric')) return 'metrics';
    if (name.includes('env')) return 'env';
    if (name.includes('log') && !name.includes('logger')) return 'logs';
    if (name.includes('restart') || name.includes('refresh'))
      return 'operations';
    if (name.includes('logger')) return 'loggers';
    if (name.includes('thread')) return 'threadDump';
    if (name.includes('http')) return 'httpExchanges';
    if (name.includes('scheduled')) return 'scheduledTasks';
    if (name.includes('cache')) return 'caches';
    if (name.includes('bean')) return 'beans';
    return 'other';
  }

  static async isAvailable(): Promise<boolean> {
    try {
      await McpService.initializeSession();
      return true;
    } catch {
      return false;
    }
  }

  static async fetchTools(): Promise<McpTool[]> {
    const sessionId = await McpService.initializeSession();

    const toolsRes = await fetch('/mcp', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json, text/event-stream',
        'Mcp-Session-Id': sessionId,
      },
      body: JSON.stringify({
        jsonrpc: '2.0',
        id: 2,
        method: 'tools/list',
        params: {},
      }),
    });

    const json = await McpService.parseResponse(toolsRes);
    const rawTools: any[] = json?.result?.tools ?? [];
    return rawTools.map(McpService.mapTool);
  }

  private static async initializeSession(): Promise<string> {
    const res = await fetch('/mcp', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json, text/event-stream',
      },
      body: JSON.stringify({
        jsonrpc: '2.0',
        id: 1,
        method: 'initialize',
        params: {
          protocolVersion: '2025-03-26',
          capabilities: {},
          clientInfo: { name: 'sba-ui', version: '1.0' },
        },
      }),
    });

    const sessionId = res.headers.get('Mcp-Session-Id');
    if (!sessionId) {
      throw new Error('No Mcp-Session-Id returned from initialize');
    }
    return sessionId;
  }

  private static async parseResponse(res: Response): Promise<any> {
    const contentType = res.headers.get('Content-Type') ?? '';
    if (contentType.includes('text/event-stream')) {
      const text = await res.text();
      const dataLine = text.split('\n').find((l) => l.startsWith('data:'));
      if (!dataLine) throw new Error('No data line in SSE response');
      return JSON.parse(dataLine.slice('data:'.length));
    }
    return res.json();
  }

  private static mapTool(raw: any): McpTool {
    const props = raw.inputSchema?.properties ?? {};
    const required: string[] = raw.inputSchema?.required ?? [];
    const params: ToolParam[] = Object.entries(props).map(
      ([k, v]: [string, any]) => ({
        name: k,
        type: v.type ?? 'string',
        required: required.includes(k),
        description: v.description ?? '',
      }),
    );
    return {
      name: raw.name,
      description: raw.description ?? '',
      params,
    };
  }
}
